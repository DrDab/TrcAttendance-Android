package com.team3543.trcattendance_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import attendance.Attendant;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        Runnable keepAlive = new Runnable()
        {
            @Override
            public void run()
            {
                Heartbeat hb = new Heartbeat();
                hb.run();
            }
        };
        new Thread(keepAlive).start();

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
                                    loadAttendantListEditorIntent();
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
                            // TODO: Populate the time, date and other fields using the loaded SessionLog
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


                            String[] OwOWhatsThis = DataStore.existingMeetingInfo;
                            // "Date", "Start Time", "End Time", "Place", "Meeting"
                            String date = OwOWhatsThis[0];
                            String timeStart = OwOWhatsThis[1];
                            String timeEnd = OwOWhatsThis[2];
                            String place = OwOWhatsThis[3];
                            String meeting = OwOWhatsThis[4];
                            String datep1 = date.substring(date.indexOf("/") + 1);
                            String datep2 = datep1.substring(datep1.indexOf("/")+1);
                            String MM = date.substring(0, date.indexOf("/"));
                            String DD = datep1.substring(0, datep1.indexOf("/"));
                            String YYYY = datep2;

                            String sH = timeStart.substring(0, timeStart.indexOf(":"));
                            String sM = timeStart.substring(timeStart.indexOf(":")+1);

                            String eH = timeEnd.substring(0, timeEnd.indexOf(":"));
                            String eM = timeEnd.substring(timeEnd.indexOf(":")+1);

                            boolean mech = false;
                            boolean prog = false;
                            boolean driv = false;
                            boolean othr = false;

                            if(meeting.indexOf("Mechanical") != -1)
                            {
                                mech = true;
                            }
                            if (meeting.indexOf("Programming") != -1)
                            {
                                prog = true;
                            }
                            if (meeting.indexOf("Drive") != -1)
                            {
                                driv = true;
                            }
                            if (meeting.indexOf("Other") != -1)
                            {
                                othr = true;
                            }

                            meetingMM.setText(MM);
                            meetingDD.setText(DD);
                            meetingYYYY.setText(YYYY);

                            startHH.setText(sH);
                            startMM.setText(sM);

                            endHH.setText(eH);
                            endMM.setText(eM);

                            placeLocation.setText(place);

                            mechanicalBox.setChecked(mech);
                            programmingBox.setChecked(prog);
                            driveBox.setChecked(driv);
                            otherBox.setChecked(othr);

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

                            setDefaultDateTimePlace();
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
            if(DataStore.allAttendants != null)
            {
                new AlertDialog.Builder(this)
                        .setTitle("Save")
                        .setMessage("Would you like to save before exiting?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                try
                                {
                                    DataStore.attendanceLog.closeLogFile();
                                }
                                catch (FileNotFoundException e)
                                {
                                    e.printStackTrace();
                                }

                                new File(DataStore.SESSION_LOG_FILE_NAME).delete();

                                meetingMM.setText("");
                                meetingDD.setText("");
                                meetingYYYY.setText("");

                                mechanicalBox.setChecked(false);
                                programmingBox.setChecked(false);
                                driveBox.setChecked(false);
                                otherBox.setChecked(false);

                                startHH.setText("");
                                startMM.setText("");

                                endHH.setText("");
                                endMM.setText("");

                                placeLocation.setText("");

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

                                finish();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                finish();
                                System.exit(0);
                            }
                        }).show();
            }
            else
            {
                finish();
                System.exit(0);
            }
        }
        else if (id == R.id.action_closefile)
        {
            try
            {
                DataStore.attendanceLog.closeLogFile();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }

            new File(DataStore.SESSION_LOG_FILE_NAME).delete();

            meetingMM.setText("");
            meetingDD.setText("");
            meetingYYYY.setText("");

            mechanicalBox.setChecked(false);
            programmingBox.setChecked(false);
            driveBox.setChecked(false);
            otherBox.setChecked(false);

            startHH.setText("");
            startMM.setText("");

            endHH.setText("");
            endMM.setText("");

            placeLocation.setText("");

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

            DataStore.isOkToEdit = false;
            DataStore.isOkToClose = false;

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
                        loadAttendantListEditorIntent();
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

    public void loadAttendantListEditorIntent()
    {
        Intent intent = new Intent(this, EditAttendantList.class);
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (newFlag)
        {
            boolean tmpB = false;
            if(!DataStore.editPopulated)
            {
                setDefaultDateTimePlace();

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

            // if checkOut list has entries that are also in checkIn list, remove those entries in checkIn list.
            ArrayList<Attendant> tmpInFin = new ArrayList<Attendant>();

            for(int i = 0; i < tmpOut.size(); i++)
            {
                int idx = tmpIn.indexOf(tmpOut.get(i));
                if (idx != -1)
                {
                    tmpIn.set(idx, null);
                }
            }

            for(int i = 0; i < tmpIn.size(); i++)
            {
                Attendant attdtmp =  tmpIn.get(i);
                if(attdtmp != null)
                {
                    tmpInFin.add(attdtmp);
                }
            }

            Collections.sort(tmpAll, new Comparator<Attendant>()
            {
                public int compare(Attendant a1, Attendant a2)
                {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            DataStore.editPopulated = true;

            DataStore.checkInList = tmpInFin;
            DataStore.checkOutList = tmpOut;
            DataStore.allAttendants = tmpAll;

            if(DataStore.attendanceLog.getCurrentSession() != null)
            {
                disableButton(createMeetingButton);
            }

            newFlag = false;
        }
    }

    public void onCreateMeetingButtonClicked(View view)
    {
        String[] info = null;
        // parse the start and end times, date from the forms.
        // Date, Start, End, Place, Meeting Type
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

    public void setDefaultDateTimePlace()
    {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int endhour = (hour + 2 > 23) ? (hour + 2) - 24 : (hour + 2);
        int minute = now.get(Calendar.MINUTE);
        if (minute < 15)
        {
            minute = 0;
        }
        else if (minute < 45)
        {
            minute = 30;
        }
        else
        {
            minute = 0;
            hour++;
        }
        String minText = "";
        String hrText = "";
        String hrText2 = "";
        if(minute < 10)
        {
            minText += "0";
        }
        minText += minute;

        if (hour < 10)
        {
            hrText += "0";
        }
        hrText += hour;

        if (endhour < 10)
        {
            hrText2 += "0";
        }
        hrText2 += endhour;

        meetingMM.setText(month + "");
        meetingDD.setText(day + "");
        meetingYYYY.setText(year + "");
        startHH.setText(hrText);
        startMM.setText(minText);
        endHH.setText(hrText2);
        endMM.setText(minText);
    }   //setDefaultDateTimePlace

}
