package com.team3543.trcattendance_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{

    //
    // The following variables will be displayed in the About page.
    //
    private static final long serialVersionUID = 1L;
    private static final String PROGRAM_TITLE = "Trc Attendance Logger";
    private static final String COPYRIGHT_MSG = "Copyright (c) Titan Robotics Club";
    private static final String PROGRAM_VERSION = "[version 1.0.0]";
    private static final String SESSION_LOG_FILE_NAME = "SessionLog.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_fileio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Options List:
        // - New File
        // - Open File
        // - Edit File
        // - Close File
        // - About
        // - Exit
        switch (item.getItemId())
        {
            case R.id.action_test:
            {
                break;
            }
        }
        return false;
    }

}
