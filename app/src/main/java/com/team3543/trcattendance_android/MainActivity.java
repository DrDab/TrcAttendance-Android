package com.team3543.trcattendance_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;

import attendance.Attendant;

public class MainActivity extends AppCompatActivity
{
    private EditText meetingMM;
    private EditText meetingDD;
    private EditText meetingYYYY;

    private CheckBox mechanicalBox;
    private CheckBox programmingBox;
    private CheckBox driveBox;
    private CheckBox otherBox;

    private EditText startHH;
    private EditText startMM;
    private EditText endHH;
    private EditText endMM;

    public static boolean newFlag = false;
    public static String nameFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if we have storage permissions first
        if (DataStore.verifyStoragePermissions(this))
        {
            DataStore.initIO();
        }
        else
        {
            AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog1.setTitle("Warning! (DON'T CLOSE)");
            alertDialog1.setMessage("Please go into Settings > Apps > \"TRC Attendance Logger\" > Permissions and check Storage.");
            alertDialog1.show();
            return;
        }

        // disable the entry boxes at startup (because no file is loaded at startup)
        meetingMM = (EditText) findViewById(R.id.dateMM);
        meetingDD = (EditText) findViewById(R.id.dateDD);
        meetingYYYY = (EditText) findViewById(R.id.dateYYYY);

        mechanicalBox = (CheckBox) findViewById(R.id.mechanicalBox);
        programmingBox = (CheckBox) findViewById(R.id.programmingBox);
        driveBox = (CheckBox) findViewById(R.id.driveBox);
        otherBox = (CheckBox) findViewById(R.id.otherBox);

        startHH = (EditText) findViewById(R.id.startHH);
        startMM = (EditText) findViewById(R.id.startMM);

        endHH = (EditText) findViewById(R.id.endHH);
        endMM = (EditText) findViewById(R.id.endMM);

        disableEditText(meetingMM);
        disableEditText(meetingDD);
        disableEditText(meetingYYYY);

        disableCheckBox(mechanicalBox);
        disableCheckBox(programmingBox);
        disableCheckBox(driveBox);
        disableCheckBox(otherBox);

        disableEditText(startHH);
        disableEditText(startMM);

        disableEditText(endHH);
        disableEditText(endMM);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_fileio, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem edit = menu.findItem(R.id.action_editfile);
        setGreyedOut(edit, !DataStore.isOkToEdit);
        MenuItem close = menu.findItem(R.id.action_closefile);
        setGreyedOut(close, !DataStore.isOkToClose);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Options List:
        // - New File
        // - Open File
        // - Edit File
        // - Close File
        // - About
        // - Exit
        int id = item.getItemId();
        if (id == R.id.action_newfile)
        {
            try
            {
                final String[] recipient = {""};
                final EditText txtUrl = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle("New File")
                        .setMessage("Please enter the name of the CSV file.")
                        .setView(txtUrl)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                recipient[0] = txtUrl.getText().toString();
                                if (DataStore.fileExists(recipient[0]))
                                {
                                    drawOverwriteWarning(recipient[0]);
                                }
                                else
                                {
                                    DataStore.newCSV(recipient[0]);
                                    IGotMistakenlyHandedACalculatorOnAnAPTestAndIAmTakingTheBlameHelpMe();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        { public void onClick(DialogInterface dialog, int whichButton) {} }).show();
            }
            catch (Exception arg0)
            {
                // TODO Auto-generated catch block
                arg0.printStackTrace();
            }
        }
        else if (id == R.id.action_openfile)
        {
            // open a file chooser with the items inside the TrcAttendance folder
            new FileChooser(this).setFileListener(new FileChooser.FileSelectedListener()
            {
                @Override
                public void fileSelected(final File file)
                {
                    // do something with the file
                    DataStore.toOpen = file;
                    DataStore.loadCSV(DataStore.toOpen.toString());

                    enableEditText(meetingMM);
                    enableEditText(meetingDD);
                    enableEditText(meetingYYYY);

                    enableCheckBox(mechanicalBox);
                    enableCheckBox(programmingBox);
                    enableCheckBox(driveBox);
                    enableCheckBox(otherBox);

                    enableEditText(startHH);
                    enableEditText(startMM);

                    enableEditText(endHH);
                    enableEditText(endMM);
                }
            }).showDialog();
        }
        else if (id == R.id.action_editfile)
        {
            DataStore.havePrevAttendants = true;
            Intent intent = new Intent(this, EditAttendantList.class);
            startActivity(intent);
        }
        else if (id == R.id.action_about)
        {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }
        else if (id == R.id.action_exit)
        {

        }
        else if (id == R.id.action_closefile)
        {

        }
        return super.onOptionsItemSelected(item);
    }

    public void setGreyedOut(MenuItem menuItem, boolean isGray)
    {
        menuItem.setEnabled(!isGray);
        Drawable resIcon = getResources().getDrawable(R.drawable.ic_launcher_background);
        if (isGray)
        {
            resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
        menuItem.setIcon(resIcon);
    }

    public void drawOverwriteWarning(final String filename0)
    {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This file already exists. Are you sure you want to overwrite an existing file?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        new File(DataStore.readDirectory, filename0).delete();
                        DataStore.newCSV(filename0);
                        IGotMistakenlyHandedACalculatorOnAnAPTestAndIAmTakingTheBlameHelpMe();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener()
                {
                        public void onClick(DialogInterface dialog, int whichButton) {}
                }).show();
    }

    public static void disableEditText(EditText editText)
    {
        editText.setFocusableInTouchMode(false);
    }

    public void enableEditText(EditText editText)
    {
        editText.setFocusableInTouchMode(true);
    }

    public void disableCheckBox(CheckBox checkBox)
    {
        checkBox.setEnabled(false);
    }

    public void enableCheckBox(CheckBox checkBox)
    {
        checkBox.setEnabled(true);
    }

    public void IGotMistakenlyHandedACalculatorOnAnAPTestAndIAmTakingTheBlameHelpMe()
    {
        Intent intent = new Intent(this, EditAttendantList.class);
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (newFlag)
        {
            Log.d("EnableEditing","Boolean flag triggered");
            enableEditText(meetingMM);
            enableEditText(meetingDD);
            enableEditText(meetingYYYY);

            enableCheckBox(mechanicalBox);
            enableCheckBox(programmingBox);
            enableCheckBox(driveBox);
            enableCheckBox(otherBox);

            enableEditText(startHH);
            enableEditText(startMM);

            enableEditText(endHH);
            enableEditText(endMM);

            DataStore.loadCSV((String) new File(DataStore.readDirectory, nameFlag).toString());

            // for(int i = 0; i < DataStore.attendanceLog.attendantsList.size(); i++)
            // {
            //     Attendant lol = DataStore.attendanceLog.attendantsList.get(i);
            //     DataStore.allAttendants.add(lol);
            //     DataStore.checkInList.add(lol);
            // }

            newFlag = false;
        }
    }

}
