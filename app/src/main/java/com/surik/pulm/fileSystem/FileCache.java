package com.surik.pulm.fileSystem;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File Cache works with app data and monitor it
 */
public class FileCache 
{
	private static final String TAG = "FileCache";
	private static final double ONE_MB = 1024 * 1024;	//1MB 
	private static final double INITIAL_VALUE = 10;		//initial value of 10MB

	private double mMaxCacheSize = INITIAL_VALUE;
	private String mBaseDirectory = null;
	
	private static FileCache mFileCacheInstance = new FileCache();
	
	private FileCache(){}
	
	/**
	 * use this method to get access to FileCache
	 * @return FileCache instance
	 */
	public static FileCache getInstance() {
		return mFileCacheInstance;
	}
	
	/**
	 * @return size of base directory(MB), 0 if base directory isn't initialize
	 */
	public double getCurrentCacheSize() {
		double currentCacheSize = 0;
		if (mBaseDirectory == null) {
			Log.e(TAG, "Base directory is null! need to set it up");
			return 0;
		}
		List<File> files = getFileList(new File(mBaseDirectory));
		if (files == null)
			return 0;

		for (File file : files) {
			currentCacheSize += file.length(); // sums the size of files
		}
		return currentCacheSize / ONE_MB;
	}
	
	/**
	 * return the available space in the cache
	 * @return free space (MB)
	 */
	public double getAvailableSpace() {
		return getMaxCacheSize() - getCurrentCacheSize();
	}
	
	/**
	 * return the oldest file in the bas library. if library is empty return null.
	 * @return
	 */
	public File getOldestFile() {
		long earliestTime = Long.MAX_VALUE;
		File fileToReturn = null;

		List<File> files = getFileList(new File(mBaseDirectory));
		if (files == null)
			return null;

		for (File file : files) {
			if (file.lastModified() < earliestTime) {
				earliestTime = file.lastModified();
				fileToReturn = file;
			}
		}
		return fileToReturn;
	}

	/**
	 * sets main base directory
	 * @param directory
     */
	public void setBaseDirectoryMain(String directory) {
		File cacheDirectory = new File(directory);

		// the given path isn't a directory so we will try to create directory
		if (!cacheDirectory.mkdirs() && !cacheDirectory.isDirectory()) {
			Log.e(TAG+"3", "The directory: " + directory
					+ " could not be created, Base directory is still: "
					+ mBaseDirectory);
			return;
		}
		
		mBaseDirectory = directory;
	}
	
	/**
	 * check if the given signal directory exists, if so - good. if not, will try to create it. if
	 * failed does nothing.
	 * @param directory
	 */
	public void setBaseSignalsDirectory(String directory) {
		File cacheDirectory = new File(directory);

		// the given path isn't a directory so we will try to create directory
		if (!cacheDirectory.isDirectory() && !cacheDirectory.mkdir()) {
			Log.e(TAG+"4", "The directory: " + directory
					+ " could not be created, Base directory is still: "
					+ mBaseDirectory);
			return;
		}
		
/*		File trials = new File(directory + "Trials/");
		trials.mkdir();
		ClinicModelExportValues.EXPORT_FILE_DIRECTORY = directory + "Trials/";
		
		File audioFiels = new File(directory + "Audio Files/");
		audioFiels.mkdir();
		
		mBaseDirectory = directory + "Audio Files/";*/

		mBaseDirectory = directory;
	}
	
	/**
	 * check if the given directory exists, if so - good. if not, will try to create it. if
	 * failed does nothing.
	 * @param directory
	 */
	public void setBaseCSVDirectory(String directory) {
		File cacheDirectory = new File(directory);

		// the given path isn't a directory so we will try to create directory
		if (!cacheDirectory.isDirectory() && !cacheDirectory.mkdir()) {
			Log.e(TAG+"5", "The directory: " + directory
					+ " could not be created, Base directory is still: "
					+ cacheDirectory);
			return;
		}
	}
	
	/**
	 * return the cache directory name
	 * @return
	 */
	public String getBaseDirectory() {
		return mBaseDirectory;
	}
	
	/**
	 * set the max size of the cache to given size (in MB), must be bigger than 0
	 * @param newMaxSize
	 */
	public void setMaxCacheSize(double newMaxSize) {
		if (newMaxSize <= 0) {
			Log.e(TAG+"6",
					"The given max cache size was 0 or smaller. must be bigger than 0");
			return;
		}

		mMaxCacheSize = newMaxSize;
	}

	public void setMaxCacheSize(int newMaxSize) {
		if (newMaxSize <= 0) {
			Log.e(TAG+"6",
					"The given max cache size was 0 or smaller. must be bigger than 0");
			return;
		}

		mMaxCacheSize = newMaxSize*1.0;
	}
	
	/**
	 * returns the max size of cache (in MB)
	 * @return max size of cache
	 */
	public double getMaxCacheSize() {
		return mMaxCacheSize;
	}
	
	//get the list of all the file (include sub directories) in base directory
	//and delete empty directories
	private List<File> getFileList(File fatherFile) {
		// create an empty list
		List<File> fileList = new ArrayList<File>();
		try {
			File[] filesAndDirs = fatherFile.listFiles();
			List<File> filesDirs = new ArrayList<File>();
			if(filesAndDirs!=null){
				filesDirs = Arrays.asList(filesAndDirs);
			}
				

			for (File file : filesDirs) {
				// if it is a file, add it to the list of files
				if (file.isFile())
					fileList.add(file);
				// if it is directory, get its inner list and add it to the
				// list. if the directory is empty delete it
				else if (file.isDirectory()) {
					List<File> deeperList = getFileList(file);

					if (deeperList == null)
						return null;

					if (deeperList.isEmpty()) {
						boolean deleteFile = file.delete(); // delete empty directories
						Log.i(TAG, "The directory: " + file.getName()
								+ " was deleted cause it is empty" +" deletedStatus:"+ deleteFile);
					} else
						fileList.addAll(deeperList);
				}
			}
			return fileList;
		} catch (ClassCastException e) {
			Log.e(TAG, "Error retrieving the file list. " + e);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Error retrieving the file list. " + e);
		} catch (NullPointerException e) {
			Log.e(TAG, "Error retrieving the file list. " + e);
		} catch (UnsupportedOperationException e) {
			Log.e(TAG, "Error retrieving the file list. " + e);
		}
		return null;
	}	
}
