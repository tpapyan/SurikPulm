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
 * app timer task scheduler
 */
public class Timer_task extends TimerTask{

    private final Handler TimerHandler= new Handler();
    private Runnable r;

    private int timerHours;
    private int timerMinutes;
    private String time;

    private boolean started;

    public Timer_task(final HeadSense activity){
        r = new Runnable() {
            public void run() {
                if(started){
                    time = "";
                    if (timerMinutes >= 60) {
                        timerMinutes = 0;
                        timerHours++;
                    }

                    if (timerHours < 10)
                        time = "0" + String.valueOf(timerHours) + ":";
                    else
                        time = String.valueOf(timerHours) + ":";
                    if (timerMinutes < 10)
                        time += "0" + String.valueOf(timerMinutes);
                    else
                        time += String.valueOf(timerMinutes);

                    activity.updateView(time);
                    timerMinutes++;

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

    public int getTimerHours() {
        return timerHours;
    }

    public void setTimerHours(int timerHours) {
        this.timerHours = timerHours;
    }

    public int getTimerMinutes() {
        return timerMinutes;
    }

    public void setTimerMinutes(int timerMinutes) {
        this.timerMinutes = timerMinutes;
    }

    public void stopHandler(){
        TimerHandler.removeCallbacks(r);
    }
}
