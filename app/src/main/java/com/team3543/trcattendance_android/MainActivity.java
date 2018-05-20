package com.team3543.trcattendance_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
{
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

}
