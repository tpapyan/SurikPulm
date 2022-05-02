package com.surik.pulm.fileSystem;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.util.Log;

import com.surik.pulm.HeadSense;
import com.surik.pulm.R;
import com.surik.pulm.ValuesOfSettings;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Everything related to wav file playing/recording is here
 */
public class AudioDataBuffer {
    private static final int ONE_MB = 1024 * 1024;
    private static final String TAG = "AudioDataBuffer";
    private static final int WAV_HEADER_LENGTH = 44;
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 4*11025;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_DEFAULT;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int recordTime = 10;
    private short[] mRecordedRawDataShort;
    private int mNumOfDataShorts;
    private int mNumOfDataShortsCut;
    private Thread recordingThread = null;
    private AudioRecord recorder = null;
    private AudioDataBufferClientInterface mClient = null;
    private static final Object object = new Object();
    private int recordEndTime = RECORDER_SAMPLERATE * 180;
    private int mBufferSize = 0;


    /**
     * Constructor of the class. if the limit is 0 or smaller, the default size will be 10MB
     *
     * @param limitSizeInBytes In MB. limit the raw data size
     * @param clientInterface  The class client
     */
    public AudioDataBuffer(int limitSizeInBytes, AudioDataBufferClientInterface clientInterface)
    {
        if (limitSizeInBytes <= 0) {
            Log.e(TAG, "The wav file limit size that was given is " + limitSizeInBytes + " should be bigger than 0.");
            return;
        }

        this.recordTime = ValuesOfSettings.getInstance().getSoundLength()+1;
        this.recordEndTime = RECORDER_SAMPLERATE * ValuesOfSettings.getInstance().getSoundLength();
        if(mRecordedRawDataShort == null){
            mRecordedRawDataShort = new short[RECORDER_SAMPLERATE * (recordTime-1)];
        }else{
            if(mRecordedRawDataShort.length != RECORDER_SAMPLERATE * (recordTime-1)) {
                mRecordedRawDataShort = new short[RECORDER_SAMPLERATE * (recordTime-1)];
            }
        }
        mClient = clientInterface;
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);

    }

    public double getWavFileSize() {
        return ((double) mNumOfDataShorts + WAV_HEADER_LENGTH)
                / (double) ONE_MB;
    }

    public Boolean isRecording()
    {
        if (recorder == null)
            return false;

        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
            return true;

        return false;
    }

    /**
     * Stopping the record process
     */
    public void stopRecording(){

        if (recorder == null || !isRecording())
            return;

        try {
            recorder.stop();
            recorder.release();
            Log.i(TAG, "Record has stopped, record length (in bytes) - " + mNumOfDataShorts);
            Log.e(TAG + "aa", String.valueOf(mRecordedRawDataShort.length));
            mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_STOPPED);
            recorder = null;
            synchronized (object) {
                recordingThread = null;
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Could not stop the recording. " + e);
            mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_FAILED);
        }
    }

    /**
     * writes recorded data to array
     */
    private void writeDataToArray()
    {
        mNumOfDataShorts = 0;


        short[] dataShort = new short[mBufferSize/2];
        int numOfReadData = 0;
        mNumOfDataShorts = 0;
        mNumOfDataShortsCut = 0;
        Boolean overSized = false;

        while (isRecording() && !overSized && !HeadSense.isStopedAlgo() && mNumOfDataShorts!= recordEndTime) {
            numOfReadData = recorder.read(dataShort, 0, mBufferSize / 2);
            if (numOfReadData != AudioRecord.ERROR_INVALID_OPERATION
                    && numOfReadData != AudioRecord.ERROR_BAD_VALUE) {
                try {
                    for (int i = 0; i < numOfReadData; i++) {mNumOfDataShortsCut++;
                        if (mNumOfDataShortsCut > RECORDER_SAMPLERATE) {
                            mRecordedRawDataShort[mNumOfDataShorts++] = dataShort[i];
                            if (mNumOfDataShorts >= mRecordedRawDataShort.length) {
                                overSized = true;
                                Log.w(TAG, "Raw data reached to the limit");
                                // mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_REACHED_BUFFER_LIMIT);
                                break;
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "Reached to end of array. stoping to record data");
                }
            }
        }

        if(mNumOfDataShorts==recordEndTime){
            stopRecording();
//            mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_STOPPED);
        }else{
            synchronized (object) {
                if (mNumOfDataShorts < RECORDER_SAMPLERATE * (recordTime - 1) + 0) {
                    mNumOfDataShorts = 0;
                }
            }
            if(HeadSense.isStopedAlgo()){
                stopRecording();
//                mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_STOPPED);
            }
        }

        if (mNumOfDataShorts > 0) {
            mClient.recordStatusChanged(AudioDataBufferEnum.ALGORITHM_START);
        }
    }

    /**
     * Saving the raw data as wav file. the method will add the ".wav".
     * @param outFilename The given file name (without the ending) 
     */
    public void saveFile(String outFilename){
        HeadSense.setWavFilePath(outFilename);
    	if (mNumOfDataShorts == 0)
    	{
    		mRecordedRawDataShort = null;
    		Log.w(TAG, "Wav file can't be saved, there is not raw data");
    		return;
    	}
    	
        FileOutputStream out = null;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = 0;
        
        try {
            if(!ValuesOfSettings.getInstance().isDemo()) {
                out = new FileOutputStream(outFilename + ".wav");
                totalAudioLen = mNumOfDataShorts * 2 - byteRate;    //added to cut 1st second
                totalDataLen = totalAudioLen + WAV_HEADER_LENGTH;

                writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                        longSampleRate, channels, RECORDER_SAMPLERATE * 2);

                byte[] tmpArray = new byte[(int) (mNumOfDataShorts * 2 - byteRate)];

                int j = 0;
                for (int i = 0; i < mNumOfDataShorts; i++) {
                    tmpArray[j] = (byte) (mRecordedRawDataShort[i] & 0xff);
                    tmpArray[j + 1] = (byte) ((mRecordedRawDataShort[i] >>> 8) & 0xff);
                    j = j + 2;
                }
                out.write(tmpArray, 0, (int) (mNumOfDataShorts * 2 - byteRate));

            }
            Log.i(TAG, "Wav file was saved to " + outFilename + ".wav");
            mClient.recordStatusChanged(AudioDataBufferEnum.DATA_FILE_SAVED);
            
        } catch (FileNotFoundException e) {
        	Log.e(TAG, "Could not create the file for saving. Check file cache size (probably too small)" + e);
        	mClient.recordStatusChanged(AudioDataBufferEnum.DATA_FILE_COULD_NOT_BE_SAVED);
        } catch (IOException e) {
        	Log.e(TAG, "There was problem with to the file, or closing the file: " + e);
        	mClient.recordStatusChanged(AudioDataBufferEnum.DATA_FILE_COULD_NOT_BE_SAVED);
        } catch (NegativeArraySizeException e) {
        	mClient.recordStatusChanged(AudioDataBufferEnum.DATA_FILE_COULD_NOT_BE_SAVED);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {
	    byte[] header = new byte[44];   
	    header[0] = 'R';  // RIFF/WAVE header
	    header[1] = 'I';
	    header[2] = 'F';
	    header[3] = 'F';
	    header[4] = (byte) (totalDataLen & 0xff);
	    header[5] = (byte) ((totalDataLen >> 8) & 0xff);
	    header[6] = (byte) ((totalDataLen >> 16) & 0xff);
	    header[7] = (byte) ((totalDataLen >> 24) & 0xff);
	    header[8] = 'W';
	    header[9] = 'A';
	    header[10] = 'V';
	    header[11] = 'E';
	    header[12] = 'f';  // 'fmt ' chunk
	    header[13] = 'm';
	    header[14] = 't';
	    header[15] = ' ';
	    header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
	    header[17] = 0;
	    header[18] = 0;
	    header[19] = 0;
	    header[20] = 1;  // format = 1
	    header[21] = 0;
	    header[22] = (byte) channels;
	    header[23] = 0;
	    header[24] = (byte) (longSampleRate & 0xff);
	    header[25] = (byte) ((longSampleRate >> 8) & 0xff);
	    header[26] = (byte) ((longSampleRate >> 16) & 0xff);
	    header[27] = (byte) ((longSampleRate >> 24) & 0xff);
	    header[28] = (byte) (byteRate & 0xff);
	    header[29] = (byte) ((byteRate >> 8) & 0xff);
	    header[30] = (byte) ((byteRate >> 16) & 0xff);
	    header[31] = (byte) ((byteRate >> 24) & 0xff);
	    //header[32] = (byte) (2 * 16 / 8);  // block align
	    header[32] = (byte) (2);  // block align
	    header[33] = 0;
	    header[34] = RECORDER_BPP;  // bits per sample
	    header[35] = 0;
	    header[36] = 'd';
	    header[37] = 'a';
	    header[38] = 't';
	    header[39] = 'a';
	    header[40] = (byte) (totalAudioLen & 0xff);
	    header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
	    header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
	    header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
	    out.write(header, 0, 44);
    }

	public void startPlayingAudio() {

    	this.recordTime = ValuesOfSettings.getInstance().getSoundLength()+1;
    	this.recordEndTime = RECORDER_SAMPLERATE * ValuesOfSettings.getInstance().getSoundLength();
        if(mRecordedRawDataShort == null){
            mRecordedRawDataShort = new short[RECORDER_SAMPLERATE * (recordTime-1)];
        }else{
            if(mRecordedRawDataShort.length != RECORDER_SAMPLERATE * (recordTime-1)) {
                mRecordedRawDataShort = new short[RECORDER_SAMPLERATE * (recordTime-1)];
            }
        }
        synchronized (object) {
            try {
                recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, mBufferSize);
                recorder.startRecording();
                Log.i(TAG, "Record has started");
                recordingThread = new Thread(new Runnable() {
                    public void run() {
                        writeDataToArray();
                    }
                },"AudioRecorder Thread");
                recordingThread.start();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Could not start the recording. " + e);
                mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_FAILED);
            } catch (IllegalThreadStateException e) {
                Log.e(TAG, "Could not start the recording thread. " + e);
                mClient.recordStatusChanged(AudioDataBufferEnum.RECORD_FAILED);
            }
        }
	}

	
	// read a wav file into this class
	public static byte[] read(String fileName)
	{
		DataInputStream inFile = null;
		byte[] myData;
		byte[] tmpLong = new byte[4];
		byte[] tmpInt = new byte[2];
        FileInputStream fileInputStream = null;
		try
		{
			fileInputStream = new FileInputStream(fileName);
			inFile = new DataInputStream(fileInputStream);
			String chunkID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			inFile.read(tmpLong); // read the ChunkSize
			//myChunkSize = byteArrayToLong(tmpLong);
			String format = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			String subChunk1ID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			inFile.read(tmpLong); // read the SubChunk1Size
			inFile.read(tmpInt); // read the audio format.  This should be 1 for PCM
			inFile.read(tmpInt); // read the # of channels (1 or 2)
			inFile.read(tmpLong); // read the samplerate
			inFile.read(tmpLong); // read the byterate
			inFile.read(tmpInt); // read the blockalign
			inFile.read(tmpInt); // read the bitspersample
			// read the data chunk header - reading this IS necessary, because not all wav files will have the data chunk here - for now, we're just assuming that the data chunk is here
			String dataChunkID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			inFile.read(tmpLong); // read the size of the data
			int myDataSize = (int) byteArrayToLong(tmpLong);
			// read the data chunk
			myData = new byte[myDataSize];
			inFile.read(myData);
			// close the input stream
			inFile.close();
			Log.i(TAG, "Chunk ID: " + chunkID + "\nFormat: " + format + "\nSub Chunk ID: " + subChunk1ID + "\nData chunk ID: " + dataChunkID);
			return myData;
		} catch(IOException e){
			Log.e(TAG, "There was a problem with reading the wav file");
		} catch (NegativeArraySizeException e) {
			Log.e(TAG, "There was a problem with reading the wav file - we got negative array size");
		}finally {
            try {
                if(fileInputStream !=null)
                    fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return null;
	}

	// these two routines convert a byte array to an unsigned integer
	private static long byteArrayToLong(byte[] b)
	{
		int start = 0;
		int i;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++)
		{
			tmp[cnt] = b[i];
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
		{
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return accum;
	}

    public short[] getmRecordedRawDataShort() {
        return mRecordedRawDataShort;
    }
}
