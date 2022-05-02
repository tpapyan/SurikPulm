package com.surik.pulm.fileSystem;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import android.util.Log;

import com.surik.pulm.HeadSense;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Saves data to specified place with specified name
 */
public class CacheObject {

	private static final String TAG = "CacheObject";
	private static final double ONE_MB = 1024 * 1024;
	
	private String mBaseDirectory = "";
	private byte[] mSnapShotData = null;
	private AudioDataBuffer mAudioDataBuffer = null;
	public String mFilesNameForSave = "";
	public String mDirectoryForSave = "";
	private int mYear;
	private int mMonth;
	private int mDay;
	private Calendar mSavedDataCalendar = Calendar.getInstance();
	/**
	 * constructor
	 */
	public CacheObject() {
	}

	/**
	 * constructor
	 * @param audioDataBuffer
	 * @param data
     */
	public CacheObject(AudioDataBuffer audioDataBuffer, byte[] data) {
		if (data != null) {
			mSnapShotData = Arrays.copyOf(data, data.length);;
//			mSnapShotData = data;
		}

		mAudioDataBuffer = audioDataBuffer;
	}
	
	/**
	 * @param snapShot set the current cache object snap shot data
	 */
	public void setSnapShotData(byte[] snapShot) {
//		mSnapShotData = snapShot;
		mSnapShotData = Arrays.copyOf(snapShot, snapShot.length);;
	}
	
	/**
	 * @param audioDataBuffer set the current cache object audio data
	 */
	public void setAudioDataBuffer(AudioDataBuffer audioDataBuffer) {
		mAudioDataBuffer = audioDataBuffer;
	}

    public void saveSnapshot(){

        mBaseDirectory = FileCache.getInstance().getBaseDirectory();
        Log.e("snap", "saveSnapshot():  "+mBaseDirectory);
        if (mBaseDirectory == null) {
            Log.e(TAG, "Base directory is null, set it up in File cache class");
            return;
        }
        setmSavedDataCalendar(Calendar.getInstance());
        mFilesNameForSave = getFileName();
        Log.e("snap", "getFileName():  "+getFileName());
        mDirectoryForSave = getDirectoryName();

        double availiableSpace = FileCache.getInstance().getAvailableSpace();

        // create the new directory
        if (!(new File(mDirectoryForSave)).exists()
                && !(new File(mDirectoryForSave)).mkdirs()) {
            Log.e(TAG, "Unable to create the directory");
            return;
        }

        // first need to check the size of the files, if the size exceeds the
        // file-cache size
        // we need to delete the oldest file(s) and only then save the new files
        if (getTotalSize() >= availiableSpace) {
            Log.w(TAG,
                    "The new file are exceeding the file cache limit. deleting old files. current cache size: "
                            + (FileCache.getInstance()).getCurrentCacheSize()
                            + " Files size: " + getTotalSize());
            if (!deleteOldestCacheObject()) {
                Log.e(TAG,
                        "Could clear room for the cache object (could not delete the oldest files)");
                return;
            }
        }

        if (mSnapShotData != null && !saveBitmap()) {
            Log.e(TAG,
                    "There was an error while saving the bmp file, saving is cancelled");
            return;
        }

        // saving the audio been held by the buffer
        //Log.e("Bug report: directory names",mDirectoryForSave + mFilesNameForSave);
    }
	
	/**
	 * @return the cache object total size (audio + snapshot)
	 */
	public double getTotalSize() {
		double totalSize = 0;
		if (mAudioDataBuffer != null)
			totalSize += mAudioDataBuffer.getWavFileSize();
		if (mSnapShotData != null)
			totalSize += (double) mSnapShotData.length / ONE_MB;
		return totalSize;
	}
		
	/**
	 * return the valid file name for save
	 * @return valid file name [day][month][year]_[hour][minute][second]
	 */
	public String getFileName() {
		
		mYear = mSavedDataCalendar.get(Calendar.YEAR);
		mMonth = mSavedDataCalendar.get(Calendar.MONTH) + 1;
		mDay = mSavedDataCalendar.get(Calendar.DAY_OF_MONTH);
		int hour = mSavedDataCalendar.get(Calendar.HOUR_OF_DAY);
		int minute = mSavedDataCalendar.get(Calendar.MINUTE);
		int seconds = mSavedDataCalendar.get(Calendar.SECOND);
		return mYear + String.format("%02d", mMonth)+ String.format("%02d", mDay) + "_"
				+ String.format("%02d", hour) + String.format("%02d", minute) + String.format("%02d", seconds);
	}
	
	/**
	 * @return return the parsed directory name
	 */
	public String getDirectoryName() {
		Calendar curCalendar = Calendar.getInstance();
		mYear = curCalendar.get(Calendar.YEAR);
		mMonth = curCalendar.get(Calendar.MONTH) + 1;
		mDay = curCalendar.get(Calendar.DAY_OF_MONTH);
		return mBaseDirectory + mYear + File.separator + mMonth
				+ File.separator + mDay + File.separator;
	}

	/**
	 * saves screen shot
	 */
	public void saveScreenCacheObject() {
		mBaseDirectory = FileCache.getInstance().getBaseDirectory();
		if (mBaseDirectory == null) {
			Log.e(TAG, "Base directory is null, set it up in File cache class");
			return;
		}

		setmSavedDataCalendar(Calendar.getInstance());
		mFilesNameForSave = getFileName();
		mDirectoryForSave = getDirectoryName();
	}
	
	/**
	 * saves the cache object to the disk. 
	 */
	public void saveCacheObject() {
		mBaseDirectory = FileCache.getInstance().getBaseDirectory();
		if (mBaseDirectory == null) {
			Log.e(TAG, "Base directory is null, set it up in File cache class");
			return;
		}
		setmSavedDataCalendar(Calendar.getInstance());
		mFilesNameForSave = getFileName();
		mDirectoryForSave = getDirectoryName();

		if (mAudioDataBuffer == null) // if there is no audio we will not save
										// anything
		{
			Log.e(TAG, "There is no audio to save");
			return;
		}

		double availiableSpace = FileCache.getInstance().getAvailableSpace();

		// create the new directory
		if (!(new File(mDirectoryForSave)).exists()
				&& !(new File(mDirectoryForSave)).mkdirs()) {
			Log.e(TAG, "Unable to create the directory");
			return;
		}

		// first need to check the size of the files, if the size exceeds the
		// file-cache size
		// we need to delete the oldest file(s) and only then save the new files
		if (getTotalSize() >= availiableSpace) {
			Log.w(TAG,
					"The new file are exceeding the file cache limit. deleting old files. current cache size: "
							+ (FileCache.getInstance()).getCurrentCacheSize()
							+ " Files size: " + getTotalSize());
			if (!deleteOldestCacheObject()) {
				Log.e(TAG,
						"Could clear room for the cache object (could not delete the oldest files)");
				return;
			}
		}

		if (mSnapShotData != null && !saveBitmap()) {
			Log.e(TAG,
					"There was an error while saving the bmp file, saving is cancelled");
			return;
		}

		// saving the audio been held by the buffer
		//Log.e("Bug report: directory names",mDirectoryForSave + mFilesNameForSave);
		mAudioDataBuffer.saveFile(mDirectoryForSave + mFilesNameForSave);
	}
	
	/**
	 * check the base directory and if it can't contain the file we it will delete the 
	 * oldest file in it, until it has a room for it.
	 * @return true if the deletion process was complete false otherwise
	 */
	public Boolean deleteOldestCacheObject() {
		while (getTotalSize() >= FileCache.getInstance().getAvailableSpace()) {
			// get the oldest file from directory and delete it
			File oldestFile = FileCache.getInstance().getOldestFile();
			if (oldestFile == null) {
				Log.e(TAG,
						"No files to delete. check directory or check size of file cache");
				return false;
			}

			if (oldestFile.delete()) {
				Log.i(TAG, "The file: " + oldestFile.getName()
						+ " was deleted.");
				Log.i(TAG, "Total size of cache object is " + getTotalSize()
						+ ". current avaiable space is "
						+ FileCache.getInstance().getAvailableSpace());
			} else {
				Log.e(TAG, "The file: " + oldestFile.getName()
						+ " could not be deleted!");
				return false;
			}
		}
		return true;
	}
	
	//return true if the bitmap saving process was OK. false otherwise.
	private Boolean saveBitmap() {
        Log.e("rr","saveBitmap");
        File file;
        if(null==HeadSense.getWavFilePath() || ("").equals(HeadSense.getWavFilePath())){
			Log.e("bef","mtav before");
            file= new File(mDirectoryForSave + mFilesNameForSave + ".M.jpg");
        }else{
			Log.e("bef","mtav after");
            file = new File(HeadSense.getWavFilePath() + ".M.jpg");
        }
		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(mSnapShotData);
			fileOutputStream.flush();
//			fileOutputStream.close();
			Log.i(TAG, "Jpg file was saved to " + file.getPath());
			return true;

		} catch (FileNotFoundException e) {
			Log.e(TAG, "Bitmap file couldn't be open for writing. " + e);
		} catch (IOException e) {
			Log.e(TAG, "Flushing or closing failed while saving bitmap. " + e);
		}finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * sets mSavedDataCalendar
	 * @param mSavedDataCalendar
     */
	public void setmSavedDataCalendar(Calendar mSavedDataCalendar) {
		this.mSavedDataCalendar = mSavedDataCalendar;
	}
}
