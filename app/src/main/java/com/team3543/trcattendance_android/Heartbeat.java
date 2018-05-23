package com.team3543.trcattendance_android;

import android.util.Log;

public class Heartbeat
{
    public Heartbeat()
    {
        Log.d("Heartbeat", "Initialized keep-alive thread.");
    }

    public boolean b = false;

    public void run()
    {
        for(;;)
        {
            double time = System.currentTimeMillis() / 1000.0;
            if(time % 15 != 0)
            {
                b = false;
            }
            if (time % 15 == 0 && !b)
            {
                Log.d("Heartbeat", "Senpai notice me! I'm still running in the background. ( " + time + " )");
                b = true;
            }
        }
    }
}
