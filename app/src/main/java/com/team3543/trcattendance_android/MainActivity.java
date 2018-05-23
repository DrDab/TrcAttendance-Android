package com.team3543.trcattendance_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    private EditText placeLocation;

    private Button createMeetingButton;

    private Button checkInButton;
    private Button checkOutButton;

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

        placeLocation = (EditText) findViewById(R.id.placeLocation);

        createMeetingButton = (Button) findViewById((R.id.button_createMeeting));

        checkInButton = (Button) findViewById(R.id.checkInButton);
        checkOutButton = (Button) findViewById(R.id.checkOutButton);

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

        disableEditText(placeLocation);

        disableButton(createMeetingButton);

        disableAttendance();
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
                    try
                    {
                        DataStore.toOpen = file;
                        DataStore.loadCSV(DataStore.toOpen.toString());
                        if(DataStore.readExistingSessionLog(DataStore.SESSION_LOG_FILE_NAME))
                        {
                            disableButton(createMeetingButton);

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

                            disableEditText(placeLocation);

                            enableAttendance();
                        }
                        else
                        {
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

                            enableEditText(placeLocation);

                            enableButton(createMeetingButton);
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).showDialog();
        }
        else if (id == R.id.action_editfile)
        {
            DataStore.havePrevAttendants = true;
            Intent intent = new Intent(this, EditAttendantList.class);
            startActivityForResult(intent, 0);
        }
        else if (id == R.id.action_about)
        {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }
        else if (id == R.id.action_exit)
        {
            // prompt the user is they want to save if a session is currently open.
            // then exit.
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

    public void enableButton(Button button)
    {
        button.setEnabled(true);
    }

    public void disableButton(Button button)
    {
        button.setEnabled(false);
    }

    public void enableAttendance()
    {
        enableButton(checkInButton);
        enableButton(checkOutButton);
    }

    public void disableAttendance()
    {
        disableButton(checkInButton);
        disableButton(checkOutButton);
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

            enableEditText(placeLocation);

            enableButton(createMeetingButton);

            // add new attendants into the list.

            for(int i = 0; i < DataStore.attendanceLog.attendantsList.size(); i++)
            {
                 DataStore.allAttendants = new ArrayList<Attendant>();
                 Attendant lol = DataStore.attendanceLog.attendantsList.get(i);
                 DataStore.allAttendants.add(lol);
                 if(DataStore.checkInList.indexOf(lol) == -1)
                 {
                     DataStore.checkInList.add(lol);
                 }
            }

            ArrayList<Attendant> tmpIn = new ArrayList<Attendant>();
            ArrayList<Attendant> tmpOut = new ArrayList<Attendant>();
            ArrayList<Attendant> tmpAll = new ArrayList<Attendant>();

            // remove attendants that have been removed from the list.
            for(int i = 0; i < DataStore.checkInList.size(); i++)
            {
                if((DataStore.attendanceLog).findAttendant(DataStore.checkInList.get(i).toString()) != null)
                {
                    tmpIn.add(DataStore.checkInList.get(i));
                    tmpAll.add(DataStore.checkInList.get(i));
                }
            }
            for(int i = 0; i < DataStore.checkOutList.size(); i++)
            {
                if((DataStore.attendanceLog).findAttendant(DataStore.checkOutList.get(i).toString()) != null)
                {
                    tmpOut.add(DataStore.checkOutList.get(i));
                    tmpAll.add(DataStore.checkOutList.get(i));
                }
            }

            Collections.sort(tmpAll, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            DataStore.checkInList = tmpIn;
            DataStore.checkOutList = tmpOut;
            DataStore.allAttendants = tmpAll;

            newFlag = false;
        }
    }

    public void onCreateMeetingButtonClicked(View view)
    {
        String[] info = null;
        // parse the start and end times, date from the forms.
        // MM/DD/YYYY, HH:MM, HH:MM, xxxxxxxxxxxxxxxxxxxx, (Mechanical/Programming/Drive/Other)
        boolean b = true;
        int month = -1;
        int day = -1;
        int year = -1;
        int startHour = -1;
        int startMinute = -1;
        int endHour = -1;
        int endMinute = -1;
        String place = "";
        String meetingType = "";
        try
        {
            month = Integer.parseInt(meetingMM.getText().toString());
            day = Integer.parseInt(meetingDD.getText().toString());
            year = Integer.parseInt(meetingYYYY.getText().toString());
        }
        catch(NumberFormatException e)
        {
            Snackbar.make(view, "Issue with date formatting", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            b = false;
        }
        catch(NullPointerException e)
        {
            Snackbar.make(view, "Date cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            b = false;
        }

        try
        {
            startHour = Integer.parseInt(startHH.getText().toString());
            startMinute = Integer.parseInt(startMM.getText().toString());
            endHour = Integer.parseInt(endHH.getText().toString());
            endMinute = Integer.parseInt(endMM.getText().toString());
        }
        catch(NumberFormatException e)
        {
            Snackbar.make(view, "Issue with time formatting", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            b = false;
        }
        catch(NullPointerException e)
        {
            Snackbar.make(view, "Time cannot be empty", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            b = false;
        }

        place = placeLocation.getText().toString();

        int cnt = 0;

        if(mechanicalBox.isChecked())
        {
            meetingType += "Mechanical";
            cnt++;
        }
        if(programmingBox.isChecked())
        {
            if(cnt > 0)
            {
                cnt--;
                meetingType += "/";
            }
            meetingType += "Programming";
            cnt++;
        }
        if(driveBox.isChecked())
        {
            if(cnt > 0)
            {
                cnt--;
                meetingType += "/";
            }
            meetingType += "Drive";
            cnt++;
        }
        if(otherBox.isChecked())
        {
            if(cnt > 0)
            {
                cnt--;
                meetingType += "/";
            }
            meetingType += "Other";
            cnt++;
        }

        info = DataStore.getSessionInfo(month, day, year, startHour, startMinute, endHour, endMinute, place, meetingType);

        if (b)
        {
            // TODO: Unlock the check-in/check-out buttons upon verification
            disableButton(createMeetingButton);

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

            disableEditText(placeLocation);

            enableAttendance();
            DataStore.attendanceLog.createSession(info);
        }
    }

    public void checkIn(View view)
    {
        //
        ArrayAdapter<Attendant> checkInAdapter = new ArrayAdapter<Attendant>(this, android.R.layout.select_dialog_multichoice);
        for(int i = 0; i < DataStore.checkInList.size(); i++)
        {
            checkInAdapter.add(DataStore.checkInList.get(i));
            Log.d("checkIn",DataStore.checkInList.get(i).toString());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Who are you?");
        builder.setAdapter(checkInAdapter, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                DataStore.checkInAttendant(DataStore.checkInList.get(item), DataStore.getEpochTime(), true);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void checkOut(View view)
    {
        ArrayAdapter<Attendant> checkOutAdapter = new ArrayAdapter<Attendant>(this, android.R.layout.select_dialog_multichoice);
        for(int i = 0; i < DataStore.checkOutList.size(); i++)
        {
            checkOutAdapter.add(DataStore.checkOutList.get(i));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Who are you?");
        builder.setAdapter(checkOutAdapter, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                DataStore.checkOutAttendant(DataStore.checkOutList.get(item), DataStore.getEpochTime(), true);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
