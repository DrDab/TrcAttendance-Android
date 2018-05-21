package com.team3543.trcattendance_android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{

    private boolean isOkToEdit = false;
    private boolean isOkToClose = false;

    private EditText meetingMM;
    private EditText meetingDD;
    private EditText meetingYYYY;

    private CheckBox mechanicalBox;
    private CheckBox programmingBox;
    private CheckBox driveBox;
    private CheckBox otherBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        disableEditText(meetingMM);
        disableEditText(meetingDD);
        disableEditText(meetingYYYY);

        disableCheckBox(mechanicalBox);
        disableCheckBox(programmingBox);
        disableCheckBox(driveBox);
        disableCheckBox(otherBox);

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
        setGreyedOut(edit, !isOkToEdit);
        MenuItem close = menu.findItem(R.id.action_closefile);
        setGreyedOut(close, !isOkToClose);
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

        }
        else if (id == R.id.action_openfile)
        {

        }
        else if (id == R.id.action_editfile)
        {

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

    public void disableEditText(EditText editText)
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

}
