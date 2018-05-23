package com.team3543.trcattendance_android;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Scanner;

public class EditAttendantList extends AppCompatActivity
{
    private static String toLoad = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendant_list);
        if (DataStore.havePrevAttendants)
        {
            // populate the list with the names of the attendants, row by row
            EditText editText = (EditText) findViewById(R.id.attendantEntries);
            toLoad = "";
            for(int i = 0; i < DataStore.allAttendants.size(); i++)
            {
                toLoad += DataStore.allAttendants.get(i).toString() + "\n";
            }
            editText.setText(toLoad);
        }
    }

    public void confirmData(View view)
    {
        boolean check = false;
        EditText editText = (EditText) findViewById(R.id.attendantEntries);
        String attendantEntries = editText.getText().toString();
        if (attendantEntries.matches(""))
        {
            check = !check;
        }
        ArrayList<String> attendantNames = new ArrayList<String>();
        Scanner sc = new Scanner(attendantEntries);
        while(sc.hasNextLine())
        {
            attendantNames.add(sc.nextLine());
        }
        String[] tmp = new String[attendantNames.size()];
        for(int i = 0; i < tmp.length; i++)
        {
            Log.d("EditAttendantsList", "attendants[" + i + "]= " + attendantNames.get(i));
            tmp[i] = attendantNames.get(i);
        }
        if (!check)
        {
            DataStore.tempAddStudents = tmp;
            DataStore.attendanceLog.updateAttendants(DataStore.tempAddStudents);
            // DataStore.writeInit();
            DataStore.havePrevAttendants = true;
            MainActivity.newFlag = true;
            Log.d("EnableEditing","Boolean flag set to true");
            finish();
        }
        else
        {
            Snackbar.make(view, "Attendants list cannot be empty.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
