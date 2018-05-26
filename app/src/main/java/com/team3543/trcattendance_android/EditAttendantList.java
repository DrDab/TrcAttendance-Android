package com.team3543.trcattendance_android;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Scanner;

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

public class EditAttendantList extends AppCompatActivity
{
    private static String toLoad = "";
    private Button confirm;
    private TextWatcher tw;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendant_list);
        confirm = (Button) findViewById(R.id.confirmAttendants);
        confirm.setEnabled(false);
        EditText editText = (EditText) findViewById(R.id.attendantEntries);
        if (DataStore.havePrevAttendants)
        {
            // populate the list with the names of the attendants, row by row.
            toLoad = "";
            for(int i = 0; i < DataStore.allAttendants.size(); i++)
            {
                toLoad += DataStore.allAttendants.get(i).toString() + "\n";
            }
            editText.setText(toLoad);
        }
        tw = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                confirm.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        };
        editText.addTextChangedListener(tw);
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
