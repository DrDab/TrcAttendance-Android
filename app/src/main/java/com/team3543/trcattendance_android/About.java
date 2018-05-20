package com.team3543.trcattendance_android;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class About extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About This App");
        setTitleColor(Color.parseColor("#ff669900"));
        TextView progTitleView = (TextView) findViewById (R.id.progTitleView);
        progTitleView.setText(DataStore.PROGRAM_TITLE);
        TextView versionView = (TextView) findViewById (R.id.versionView);
        versionView.setText(DataStore.PROGRAM_VERSION);
        TextView copyrightView = (TextView) findViewById (R.id.copyrightView);
        copyrightView.setText(DataStore.COPYRIGHT_MSG);
    }

}
