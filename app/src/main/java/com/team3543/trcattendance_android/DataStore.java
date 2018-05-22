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

public class DataStore
{

    //
    // The following variables will be displayed in the About page.
    //
    public static final long serialVersionUID = 1L;
    public static final String PROGRAM_TITLE = "Trc Attendance Logger";
    public static final String COPYRIGHT_MSG = "Copyright (c) Titan Robotics Club";
    public static final String PROGRAM_VERSION = "[version 1.0.0]";
    public static String SESSION_LOG_FILE_NAME = "SessionLog.txt";

    public static File readDirectory = null;

    public static String[] tempAddStudents = null;

    public static AttendanceLog attendanceLog = null;

    public static ArrayList<Attendant> checkInList = null;
    public static ArrayList<Attendant> checkOutList = null;
    public static ArrayList<Attendant> allAttendants = null;

    public static boolean havePrevAttendants = false;

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

            // onCreateMeeting(sessionInfo[0], sessionInfo[1], sessionInfo[2], sessionInfo[3], sessionInfo[4]);
            // meetingPane.setMeetingInfo(sessionInfo[0], sessionInfo[1], sessionInfo[2], sessionInfo[3], sessionInfo[4]);
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

    public static void initIO()
    {
        readDirectory = new File(Environment.getExternalStorageDirectory(), "TrcAttendance");
        if (!readDirectory.exists())
        {
            readDirectory.mkdir();
        }
        File readFile = new File(readDirectory, "SessionLog.txt");
        SESSION_LOG_FILE_NAME = readFile.toString();
    }

    public static boolean fileExists(String name)
    {
        File test = new File(readDirectory, name);
        if (test.exists() && !test.isDirectory())
        {
            return true;
        }
        return false;
    }

    public static void newCSV(String name)
    {
        havePrevAttendants = false;
        checkInList = new ArrayList<Attendant>();
        checkOutList = new ArrayList<Attendant>();
        allAttendants = new ArrayList<Attendant>();
        try
        {
            File test = new File(readDirectory, name);
            attendanceLog = new AttendanceLog(test, true);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void writeInit()
    {
        try
        {
            attendanceLog.closeLogFile();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadCSV(String name)
    {
        havePrevAttendants = false;
        checkInList = new ArrayList<Attendant>();
        checkOutList = new ArrayList<Attendant>();
        allAttendants = new ArrayList<Attendant>();
        try
        {
            attendanceLog = new AttendanceLog(new File(readDirectory, name), false);
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
    }

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
    }

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

            int removeIdx = findAttendant(checkInList, attendant);
            checkInList.remove(removeIdx);
            checkOutList.add(attendant);
            Collections.sort(checkOutList, new Comparator<Attendant>()
            {
                @Override
                public int compare(Attendant lhs, Attendant rhs)
                {
                    return lhs.compareTo(rhs);
                }
            });
            attendanceLog.setFileDirty();
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
            // checkOutList.removeItem(attendant);
            // checkInList.addItem(attendant);
            attendanceLog.setFileDirty();
        }
    }   //checkOutAttendant

    /**
     * This method will return the index of an attendant (search) inside a sorted ArrayList attendants,
     * or -1 if there is NO attendant found.
     *
     * @param attendants is a SORTED ArrayList of attendants to search for the specified Attendant in.
     * @param search specifies the Attendant to search for in the ArrayList attendants.
     * @return the index of the attendant in question, or -1 if attendant is NOT found.
     */
    public static int findAttendant(ArrayList<Attendant> attendants, Attendant search)
    {
        int first = 0;
        int last = attendants.size() - 1;
        int mid;
        while (first <= last)
        {
            mid = (first + last) / 2;
            if (search == attendants.get(mid))
            {
                return mid;
            }
            else if (search.compareTo(attendants.get(mid)) > 0)
            {
                last = mid - 1;
            }
            else
            {
                first = mid + 1;
            }
        }

        return -1;
    }

}
