package com.surik.pulm.timer;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import java.util.Timer;

public class Total_Timer {
    private Timer timer;
    public Total_Timer(){
        timer = new Timer();
    }
    public Timer getTimer() {
        return timer;
    }
}
