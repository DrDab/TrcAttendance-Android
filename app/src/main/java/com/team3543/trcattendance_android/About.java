package com.team3543.trcattendance_android;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

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

public class About extends AppCompatActivity
{

    private int activate = 0;

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
        Button theButton = (Button) findViewById(R.id.easterEgg);
        theButton.setVisibility(View.VISIBLE);
        theButton.setBackgroundColor(Color.TRANSPARENT);
    }

    public void mystery(View view)
    {
        activate++;
        if(activate >= 10)
        {
            MediaPlayer player = MediaPlayer.create(this, R.raw.dimmadubstep);
            try {
                // player.prepare();
                player.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
