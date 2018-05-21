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

    public static AttendanceLog attendanceLog = null;

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
                    // TODO: Check in an attendant in DataStore
                    // attendancePane.checkInAttendant(attendant, Long.parseLong(transaction[2]), false);
                }
                else if (transaction[0].equals("CheckOut"))
                {
                    // TODO: Check out an attendant in DataStore
                    // attendancePane.checkOutAttendant(attendant, Long.parseLong(transaction[2]), false);
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
    public void logTransaction(boolean checkOut, Attendant attendant, long timestamp)
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
        File readDirectory = new File(Environment.getExternalStorageDirectory(), "TrcAttendance");
        if (!readDirectory.exists())
        {
            readDirectory.mkdir();
        }
        File readFile = new File(readDirectory, "SessionLog.txt");
        SESSION_LOG_FILE_NAME = readFile.toString();
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

}
