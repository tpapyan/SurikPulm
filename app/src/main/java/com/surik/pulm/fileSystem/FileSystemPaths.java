package com.surik.pulm.fileSystem;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * All paths according to app are here
 */
public class FileSystemPaths {
    private static String mCompleteFilesPath;
    private static String mBadSignalsPath;
    private static String wavFileName;
    private static String screenShotFileName;
    private static String snapshotFileName;
    private static String mCompleteFilePath;

    /**
     * @returnm CompleteFilesPath
     */
    public static String getmCompleteFilesPath() {
        return mCompleteFilesPath;
    }

    /**
     * sets mCompleteFilesPath
     *
     * @param mCompleteFilesPath
     */
    public static void setmCompleteFilesPath(String mCompleteFilesPath) {
        FileSystemPaths.mCompleteFilesPath = mCompleteFilesPath;
    }

    /**
     * @return mBadSignalsPath
     */
    public static String getmBadSignalsPath() {
        return mBadSignalsPath;
    }

    /**
     * sets mBadSignalsPath
     *
     * @param mBadSignalsPath
     */
    public static void setmBadSignalsPath(String mBadSignalsPath) {
        FileSystemPaths.mBadSignalsPath = mBadSignalsPath;
    }

    /**
     * @return wavFileName
     */
    public static String getWavFileName() {
        return wavFileName;
    }

    /**
     * sets wavFileName
     *
     * @param wavFileName
     */
    public static void setWavFileName(String wavFileName) {
        FileSystemPaths.wavFileName = wavFileName;
    }

    /**
     * @return screenShotFileName
     */
    public static String getScreenShotFileName() {
        return screenShotFileName;
    }

    /**
     * sets screenShotFileName
     *
     * @param screenShotFileName
     */
    public static void setScreenShotFileName(String screenShotFileName) {
        FileSystemPaths.screenShotFileName = screenShotFileName;
    }

    /**
     * @return snapshotFileName
     */
    public static String getSnapshotFileName() {
        return snapshotFileName;
    }

    /**
     * sets snapshotFileName
     *
     * @param snapshotFileName
     */
    public static void setSnapshotFileName(String snapshotFileName) {
        FileSystemPaths.snapshotFileName = snapshotFileName;
    }

    /**
     * @return mCompleteFilePath
     */
    public static String getmCompleteFilePath() {
        return mCompleteFilePath;
    }

    /**
     * sets mCompleteFilePath
     *
     * @param mCompleteFilePath
     */
    public static void setmCompleteFilePath(String mCompleteFilePath) {
        FileSystemPaths.mCompleteFilePath = mCompleteFilePath;
    }

}
