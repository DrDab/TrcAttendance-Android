package com.team3543.trcattendance_android;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import attendance.Attendant;
import attendance.AttendanceLog;

/**
 *
 *  Copyright (c) 2018 Titan Robotics Club, Victor Du
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

public class DataStore
{

    //
    // The following variables will be displayed in the About page.
    //
    public static final long serialVersionUID = 1L;
    public static final String PROGRAM_TITLE = "Trc Attendance Logger";
    public static final String COPYRIGHT_MSG = "Copyright (c) Titan Robotics Club";
    public static final String PROGRAM_VERSION = "[version 1.0.7a]";
    public static String SESSION_LOG_FILE_NAME = "SessionLog.txt";

    public static File readDirectory = null;

    public static AttendanceLog attendanceLog = null;

    public static ArrayList<Attendant> checkInList = null;
    public static ArrayList<Attendant> checkOutList = null;
    public static ArrayList<Attendant> allAttendants = null;

    public static boolean havePrevAttendants = false;

    public static File toOpen = null;

    public static boolean isOkToEdit = false;
    public static boolean isOkToClose = false;

    public static boolean editPopulated = false;

    public static String[] existingMeetingInfo;

    /**
     * This method reads the session log file if there is one. It will recreate the meeting from the session log.
     *
     * @param sessionLogName specifies the session log file name.
     * @return true if there is a session log file, false otherwise.
     * @throws FileNotFoundException
     */
    public static boolean readExistingSessionLog(String sessionLogName) throws FileNotFoundException
    {
        boolean success = false;

        File sessionLogFile = new File(sessionLogName);
        if (sessionLogFile.exists())
        {
            Scanner sessionLog = new Scanner(sessionLogFile);
            String[] sessionInfo = sessionLog.nextLine().trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            if (sessionInfo.length != 5)
            {
                sessionLog.close();
                throw new IllegalArgumentException("Invalid meeting info.");
            }

            attendanceLog.createSession(sessionInfo);
            
            // onCreateMeeting(sessionInfo[0], sessionInfo[1], sessionInfo[2], sessionInfo[3], sessionInfo[4]);
            // meetingPane.setMeetingInfo(sessionInfo[0], sessionInfo[1], sessionInfo[2], sessionInfo[3], sessionInfo[4]);
            existingMeetingInfo = sessionInfo;
            while (sessionLog.hasNextLine())
            {
                String[] transaction = sessionLog.nextLine().trim().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                Attendant attendant = attendanceLog.findAttendant(
                        transaction[1].substring(1, transaction[1].length() - 1));

                if (transaction[0].equals("CheckIn"))
                {
                    checkInAttendant(attendant, Long.parseLong(transaction[2]), false);
                }
                else if (transaction[0].equals("CheckOut"))
                {
                    checkOutAttendant(attendant, Long.parseLong(transaction[2]), false);
                }
            }
            sessionLog.close();
            DataStore.editPopulated = true;
            success = true;
        }

        return success;
    }   //readExistingSessionLog

    /**
     * This method writes a transaction entry to the session log.
     *
     * @param checkOut specifies true if it is a check-out transaction, false if it is a check-in transaction.
     * @param attendant specifies the attendant.
     * @param timestamp specifies the transaction time.
     */
    public static void logTransaction(boolean checkOut, Attendant attendant, long timestamp)
    {
        File sessionFile = new File(SESSION_LOG_FILE_NAME);
        boolean exist = sessionFile.exists();

        try
        {
            PrintStream sessionLog = new PrintStream(new FileOutputStream(sessionFile, exist));

            if (!exist)
            {
                sessionLog.println(attendanceLog.getCurrentSession());
            }
            sessionLog.printf("%s,\"%s\",%d\n", checkOut? "CheckOut": "CheckIn", attendant, timestamp);
            sessionLog.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }   //logTransaction

    public static long getEpochTime()
    {
        return System.currentTimeMillis();
    }

    /**
     * This method will dynamically set the directory for individual phones in preparation for file I/O.
     */
    public static void initIO()
    {
        readDirectory = new File(Environment.getExternalStorageDirectory(), "TrcAttendance");
        if (!readDirectory.exists())
        {
            readDirectory.mkdir();
        }
        File readFile = new File(readDirectory, "SessionLog.txt");
        SESSION_LOG_FILE_NAME = readFile.toString();
    }   //initIO

    public static boolean fileExists(String name)
    {
        File test = new File(readDirectory, name);
        if (test.exists() && !test.isDirectory())
        {
            return true;
        }
        return false;
    }   //fileExists

    /**
     * This method will create a new CSV file, load the file into the attendance library and initiate the ArrayLists
     * used to track participants.
     * @param name
     */
    public static void newCSV(String name)
    {
        havePrevAttendants = false;
        checkInList = new ArrayList<Attendant>();
        checkOutList = new ArrayList<Attendant>();
        allAttendants = new ArrayList<Attendant>();
        File tmpRef = null;
        try
        {
            tmpRef = new File(readDirectory, name);
            attendanceLog = new AttendanceLog(tmpRef, true);
            isOkToClose = true;
            isOkToEdit = true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        DataStore.editPopulated = false;
    }   //newCSV

    /**
     * This method will load an existing CSV file into the attendance library, and populate the ArrayLists
     * containing the attendant names.
     *
     * @throws FileNotFoundException if the CSV file referenced does not exist.
     * @param name
     */
    public static void loadCSV(String name)
    {
        havePrevAttendants = false;
        checkInList = new ArrayList<Attendant>();
        checkOutList = new ArrayList<Attendant>();
        allAttendants = new ArrayList<Attendant>();
        try
        {
            attendanceLog = new AttendanceLog(new File(name), false);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        for(int i = 0; i < attendanceLog.attendantsList.size(); i++)
        {
            Attendant lol = attendanceLog.attendantsList.get(i);
            allAttendants.add(lol);
            checkInList.add(lol);
        }
        havePrevAttendants = true;
        isOkToClose = true;
        isOkToEdit = true;
        DataStore.editPopulated = false;
    }   //loadCSV

    /**
     * This method checks if the current session has system permissions for accessing local storage.
     *
     * @param activity
     * @return whether we are able to read/write to local storage.
     */
    public static boolean verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d("FileIO", "Checking File I/O Permissions...");
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            Log.d("FileIO", "File permissions insufficient, requesting privileges...");
            return false;

        }
        return true;
    }   //verifyStoragePermissions

    /**
     * This method checks in the selected attendant by moving the attendant from the check-in
     * list to the check-out list and mark the log file as dirty. If sessionLog is true, it also
     * writes a transaction entry to the session log.
     *
     * @param attendant specifies the attendant to be checked in.
     * @param timestamp specifies the check-in time.
     * @param logTransaction specifies true to log a transaction entry in the session log.
     */
    public static void checkInAttendant(Attendant attendant, long timestamp, boolean logTransaction)
    {
        if (attendant != null)
        {
            if (logTransaction)
            {
                logTransaction(false, attendant, timestamp);
            }
            attendant.checkIn(timestamp);

            checkInList.remove(checkInList.indexOf(attendant));
            checkOutList.add(attendant);
            attendanceLog.setFileDirty();

            Collections.sort(checkInList, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            Collections.sort(checkOutList, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });
        }
    }   //checkInAttendant

    /**
     * This method checks out the selected attendant by moving the attendant from the check-out
     * list back to the check-in list and mark the log file as dirty. If sessionLog is true, it also
     * writes a transaction entry to the session log.
     *
     * @param attendant specifies the attendant to be checked out.
     * @param timestamp specifies the check-out time.
     * @param logTransaction specifies true to log a transaction entry in the session log.
     */
    public static void checkOutAttendant(Attendant attendant, long timestamp, boolean logTransaction)
    {
        if (attendant != null)
        {
            if (logTransaction)
            {
                logTransaction(true, attendant, timestamp);
            }
            attendant.checkOut(timestamp);

            checkOutList.remove(checkOutList.indexOf(attendant));
            checkInList.add(attendant);
            attendanceLog.setFileDirty();

            Collections.sort(checkInList, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            Collections.sort(checkOutList, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });
        }
    }   //checkOutAttendant

    /**
     * This method returns a String array that contains the date, start time, end time, place and meeting location
     * of the current meeting session.
     *
     * @param month
     * @param day
     * @param year
     * @param startHr
     * @param startMin
     * @param endHr
     * @param endMin
     * @param place
     * @param meeting
     * @return a 5-element String array that is ordered in the order: Date (MM/DD/YYYY), Start Time (HH:MM), End Time (HH:MM), Place, Meeting.
     */
    public static String[] getSessionInfo(int month, int day, int year, int startHr, int startMin, int endHr, int endMin, String place, String meeting)
    {
        String[] tmp = new String[5];
        String monthString = "";
        if (month < 10)
        {
            monthString += "0";
        }
        monthString += month;

        String dayString = "";
        if (day < 10)
        {
            dayString += "0";
        }
        dayString += day;

        String starthourString = "";
        if(startHr < 10)
        {
            starthourString += "0";
        }
        starthourString += startHr;

        String startminString = "";
        if(startMin < 10)
        {
            startminString += "0";
        }
        startminString += startMin;

        //
        String endhourString = "";
        if(endHr < 10)
        {
            endhourString += "0";
        }
        endhourString += endHr;

        String endminString = "";
        if(endMin < 10)
        {
            endminString += "0";
        }
        endminString += endMin;

        tmp[0] = monthString + "/" + dayString + "/" + year;
        tmp[1] = starthourString + ":" + startminString;
        tmp[2] = endhourString + ":" + endminString;
        tmp[3] = place;
        tmp[4] = meeting;
        return tmp;
    }   //getSessionInfo

}
