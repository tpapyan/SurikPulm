package com.surik.pulm.timer;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import java.util.Timer;

/**
 * app timer
 */
public class Timer_ {
    private Timer timer;
    public Timer_(){
        timer = new Timer();
    }
    public Timer getTimer() {
        return timer;
    }
}
