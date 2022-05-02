package com.surik.pulm.model;


import android.app.Activity;
import android.util.Log;

import com.surik.pulm.ValuesOfSettings;
import com.surik.pulm.HeadSense;
import com.surik.pulm.algorithm.Alg;
import com.surik.pulm.fileSystem.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * HeadSense main Model which controls all the processes - start/stop recording,playing
 */
public class HeadSenseModel implements AudioDataBufferClientInterface {
    private static final Object object = new Object();
    private static final int INITIAL_LIMIT_WAV_SIZE = 5;
    private static final String TAG = "HeadSenseModel";
    private HeadSenseModelClientInterface mClient = null;
    private Activity mActivity = null;
    private Alg mAlgorithm = null;
    private AudioDataBuffer mAudioDataBuffer = null;


    public HeadSenseModel(HeadSenseModelClientInterface clientInterface, Activity activity) {
        mClient = clientInterface;
        mActivity = activity;
		setModelFromPreferences();
    }

	public void start() throws IOException {
		mAudioDataBuffer = new AudioDataBuffer(INITIAL_LIMIT_WAV_SIZE, this);

		if(ValuesOfSettings.getInstance().isDemo()) {
			try {
				copyFileFromAsset("sound.wav", "demo/");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		(new File(ValuesOfSettings.getInstance().getBaseDirectory())).mkdir();
		if (!new File(ValuesOfSettings.getInstance().getBaseDirectory() + "coefSig1.txt").exists())  {
			copyFileFromAssetToSD("coefSig1.txt", ValuesOfSettings.getInstance().getBaseDirectory());
		}
	}
		/**
	 * sets model Preferences
	 */
	public void setModelFromPreferences() {
    	FileCache.getInstance().setBaseDirectoryMain(ValuesOfSettings.getInstance().getBaseDirectory());
    	FileCache.getInstance().setBaseSignalsDirectory(ValuesOfSettings.getInstance().getBaseDirectory()+"Signals/");

		if(ValuesOfSettings.getInstance().isDemo()) {
			try {
				copyFileFromAsset("sound.wav", "demo/");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	public void saveAudioDataBuffer(byte[] snapShotData) {
		stopRecordAndSaveNewCacheObject(snapShotData);
	}
	
	/**
	 * Starting to play and record, when record time is done, and we are still 
	 * in play mode, then we are waiting a  time and start again. 
	 */
	public void startPlayingAndRecording() {
		mAudioDataBuffer.startPlayingAudio();
//		recordStatusChanged(AudioDataBufferEnum.RECORD_STOPPED);
	}
	
	/**
	 * stops the current recording and saves the wav file.
	 */
	public void stopPlayingAndRecording() {
		if (mAudioDataBuffer == null) {
			return;
		}

		mAudioDataBuffer.stopRecording();
        recordStatusChanged(AudioDataBufferEnum.RECORD_STOPPED);
	}

	/**
	 * Use this to get the algorithm, in order to use is results
	 * @return the algorithm variable
	 */
	public Alg getAlgorithm() {
		return mAlgorithm;
	}

	private void stopRecordAndSaveNewCacheObject(byte[] snapShotData) {
		CacheObject tempCacheObject;
		(tempCacheObject = new CacheObject(mAudioDataBuffer, snapShotData)).saveCacheObject();

		FileSystemPaths.setmCompleteFilesPath(tempCacheObject.mDirectoryForSave + tempCacheObject.mFilesNameForSave);
		FileSystemPaths.setWavFileName(tempCacheObject.mFilesNameForSave + ".wav");
		FileSystemPaths.setmCompleteFilePath(tempCacheObject.mDirectoryForSave);
	}
	

	public void startAlgorithmWithSpecifiedData() {
		synchronized (object) {
			try {
				if (mAlgorithm == null) {
					mAlgorithm = new Alg(mActivity);
				} else {
					mAlgorithm.clearLists();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		int processRawData = 0;

//		if (Flow.getAlgorithmIndex() < Flow.getDataForAlgorithm().size()) {
			if (ValuesOfSettings.getInstance().isDemo()) {
				String directoryStr = ValuesOfSettings.getInstance().getBaseDirectory() + "demo/";
//				try {
//					copyFileFromAsset("sound.wav", "");
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				File folder = new File(directoryStr);
				File[] listOfFiles = folder.listFiles();

				if (listOfFiles != null && listOfFiles.length > 0) {
					Arrays.sort(listOfFiles);
					int index = HeadSense.getDemoIndex();// (int)(Math.random() * listOfFiles.length);
					while (index < listOfFiles.length && listOfFiles[index].isDirectory() && !listOfFiles[index].getName().endsWith("wav")) {
						index++;
					}
					if (index < listOfFiles.length && listOfFiles[index].isFile()) {
						byte[] tempRawDataB = AudioDataBuffer.read(directoryStr + listOfFiles[index].getName());
						short[] tempRawDataFile = new short[(tempRawDataB.length)/2];
//						if (tempRawDataB != null && tempRawDataB.length >= 4 * 11025 * 10) {
							for (int i = 0; i < tempRawDataFile.length; i++) {
								tempRawDataFile[i] = getShort(tempRawDataB[2*i], tempRawDataB[2*i + 1]);
							}
//						}else{
//
//						}
						if (mAlgorithm != null) {
							processRawData = mAlgorithm.processRawData(tempRawDataFile);
						}
						if (index < listOfFiles.length - 1) {
                            HeadSense.setDemoIndex(index + 1);
						} else {
                            HeadSense.setDemoIndex(0);
						}
					} else {
                        HeadSense.setDemoIndex(0);
					}
				}
			} else {
				if (mAlgorithm != null) {
//					processRawData = mAlgorithm.processRawData(HeadSense.getDataForAlgorithm().get(HeadSense.getAlgorithmIndex()));
					processRawData = mAlgorithm.processRawData(mAudioDataBuffer.getmRecordedRawDataShort());
				}
			}

//        HeadSense.setAlgorithmIndex(HeadSense.getAlgorithmIndex() + 1);

//			processRawData = 1;
//        if(processRawData != -1){
//            processRawData =(int)(2.5*Math.random());
			if (processRawData == 0) {
                HeadSense.setBadAlg(false);
				Log.i(TAG, "Algorithm has finished its processing, data is ready to be pulled");
				mClient.headSenseModelStatusChanged(HeadSenseModelEnum.ALGORITHM_HAS_FINISHED_WITH_SUCCESS);
			} else {
                HeadSense.setBadAlg(true);
				mClient.headSenseModelStatusChanged(HeadSenseModelEnum.ALGORITHM_HAS_FINISHED_WITH_FAILURE);
				Log.i(TAG, "Algorithm returned false answer, record is not valid for display");

			}
		}
//	}

    /**
     * handles all recording states here
     *
     * @param status get the list of statuses
     */
    public void recordStatusChanged(AudioDataBufferEnum status) {
        Log.i(TAG, status.name());
        switch (status) {
            case RECORD_STARTED:

                break;
            case RECORD_STOPPED:
                mClient.headSenseModelStatusChanged(HeadSenseModelEnum.AUDIO_RECORD_STOPPED);
                break;
            case RECORD_FAILED:
                mClient.headSenseModelStatusChanged(HeadSenseModelEnum.AUDIO_RECORD_FAILED);
                break;
            case ALGORITHM_START:
                mClient.headSenseModelStatusChanged(HeadSenseModelEnum.ALGORITHM_START);
                break;
            case DATA_FILE_SAVED:
                mClient.headSenseModelStatusChanged(HeadSenseModelEnum.FILES_WERE_SAVED);
                break;
            case DATA_FILE_COULD_NOT_BE_SAVED:

                break;
            default:
                break;
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private short getShort(byte argB1, byte argB2) {
        return (short) ((argB1 & 0xff) | (argB2 << 8));
    }

	@SuppressWarnings("unused")
	private void copyFileFromAssetToSD(String fileName, String path) throws IOException
	{
		InputStream inputStream;
		FileOutputStream outputStream = null;
		try {
			inputStream = mActivity.getAssets().open(fileName);
			outputStream = new FileOutputStream(path + fileName);
			copyFile(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(outputStream != null)
					outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void copyFileFromAsset(String fileName, String folderName) throws IOException
	{
		String directoryStr = ValuesOfSettings.getInstance().getBaseDirectory();

		if (!new File(directoryStr + folderName).exists()) {
			File folder = new File(directoryStr + folderName);
			if (!folder.isDirectory() && !folder.mkdir()) {
				Log.e(TAG, "The directory: " + folder
						+ " could not be created, Base directory is still: "
						+ folder);
			}
		}
		File directory = new File(directoryStr + folderName);
		File[] contents = directory.listFiles();
		if (!new File(directoryStr + folderName + fileName).exists() && (contents != null && contents.length == 0)) {
			copyFileFromAssetToSD(fileName, directoryStr + folderName);
		}
	}
}