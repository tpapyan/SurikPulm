package com.surik.pulm.timer;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import android.os.Handler;

import com.surik.pulm.HeadSense;

import java.util.TimerTask;

/**
 * app total timer task scheduler
 */
public class Total_Timer_task extends TimerTask {
    private final Handler TimerHandler= new Handler();
    private Runnable r;
    private String time;

    private boolean started;


    public Total_Timer_task(final HeadSense activity){
        r = new Runnable() {
            public void run() {
                if(started){
                    time = "";
                    if (HeadSense.getTotal_timer_minutes() >= 60) {
                        HeadSense.setTotal_timer_minutes(0);
                        HeadSense.setTotal_timer_hours(HeadSense.getTotal_timer_hours()+1);
                    }

                    if (HeadSense.getTotal_timer_hours() < 10)
                        time = "0" + String.valueOf(HeadSense.getTotal_timer_hours()) + ":";
                    else
                        time = String.valueOf(HeadSense.getTotal_timer_hours()) + ":";
                    if (HeadSense.getTotal_timer_minutes() < 10)
                        time += "0" + String.valueOf(HeadSense.getTotal_timer_minutes());
                    else
                        time += String.valueOf(HeadSense.getTotal_timer_minutes());

                    HeadSense.setTotal_timer_minutes(HeadSense.getTotal_timer_minutes()+1);

                }

            }
        };
    }

    @Override
    public void run() {
        TimerHandler.post(r);
    }



    public void setStarted(boolean started) {
        this.started = started;
    }

    public void stopHandler(){
        TimerHandler.removeCallbacks(r);
    }
}
