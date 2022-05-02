package com.surik.pulm.algorithm;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * HeadSense algorithm is based on Matlab code and validated accordingly,
 * hence it doesn’t always comply with HeadSense Java coding guidelines.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.surik.pulm.HeadSense;
import com.surik.pulm.Params;
import com.surik.pulm.ValuesOfSettings;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Algorithm class
 */
public class Alg {
	private static final String RP_VALUES_FILE_NAME = "rp_values";
	private Activity activity;
	private SegmentData segmentData = null;
	private ArrayList<SegmentResultsParms> segmentResults;
	private ArrayList<SegmentResultsParms> finalResults;
	private ArrayList<Double> icpArray;
	private double[] mCoefSig1;
//	private double[] mCoefSig2;

	public Alg(Activity activity) throws IOException {
		segmentResults = new ArrayList<SegmentResultsParms>();
		finalResults = new ArrayList<SegmentResultsParms>();
		icpArray = new ArrayList<Double>();
		mCoefSig1  = readCoefFile(51, "coefSig1.txt");
//		mCoefSig2  = readCoefFile(151, "coefSig2.txt");
		this.activity = activity;
	}
	
	public void clearLists() {
		segmentResults.clear();
		finalResults.clear();
		icpArray.clear();
	}

	/**
	 * This method gets the raw wave data (without the header), and analyze it
	 * (do the algorithm process) the method returns true if completed without
	 * problems, false otherwise.
	 * 
	 * @param wavRawData
	 *            Array of bytes, represent the wave raw data (not includes the
	 *            header raw data)
	 * @return true if process complete without problems, false otherwise
	 */
	public int processRawData(short[] wavRawData) {
		int flag = 0;
		if (wavRawData == null || wavRawData.length == 0){
			System.out.println("Data is empty...");
			return 1;
		}
		String kmString = Params.kmString;

		String[] words = kmString.split(" ");
		double[] km = new double[words.length];
		for(int i = 0; i < words.length; i++) {
			km[i] = Double.parseDouble(words[i]);
		}
		double[] tmpSig = shortToDouble(wavRawData);


		segmentData = new SegmentData();
		
//		flag = 0;//segmentData.processSegment(inSig, mCoefSig1, mCoefSig2);
		flag = segmentData.processSegment(tmpSig, mCoefSig1);
		int kIcp = 1;
			if (flag == 0) {
				segmentResults.add(segmentData.getResults());
				try {
					getRPFromFile();
					//				saveRPValues(segmentData.getResults());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				double icp = 0;//km[0];
				SegmentResultsParms sa = new SegmentResultsParms();
				sa.calculateAverage(segmentResults);
				finalResults.add(sa);
				icp = km[0] + km[1] * sa.getValue(1) + km[2] * sa.getValue(0) + km[3] * sa.getValue(2) + km[4] * sa.getValue(3) +
						km[5] * sa.getValue(4) + km[6] * sa.getValue(5) + km[7] * sa.getValue(6) + km[8] * sa.getValue(7) +
						km[9] * sa.getValue(8) + km[10] * sa.getValue(9) + km[11] * sa.getValue(10) +
						km[12] * sa.getValue(11) + km[13] * sa.getValue(13) + km[14] * sa.getValue(14) +
						km[15] * sa.getValue(15) + km[16] * sa.getValue(16) + km[17] * (sa.getValue(12)-0.011);

				icp /= kIcp;
				if (icp < 0) {
					icp = -icp;
				}
				Log.d("nn", "ICP ===== " + icp);
				if (icp > 30) {
					icp = 30;
				}
				icpArray.add(icp);
			}
		return flag;
	}

	/**
	 * Sig1
	 * return an array of points in order to draw the total acoustic graph. each
	 * pointF holds x and y value. so we will get array of points - collection
	 * of (x, y).
	 * 
	 * @return array of point, the value will be null if the data isn't ready to
	 *         be read or algorithm process failed.
	 */

	public PointF[] getPeaksEN12s() {
		if(segmentData != null){
			return getPeaksArr2(segmentData.getPeaksEN12s());
		}else{
			return getPeaksArr2(new ArrayList<CPeak>());
		}
	}

	public PointF[] getPeaks12s() {
		if(segmentData != null){
			getPeaksArr(segmentData.getPeaks12s());
			return getPeaksArr2D(segmentData.getPeaks12s());
		}else{
			return getPeaksArr(new ArrayList<CPeak>());
		}
	}
	
	private PointF[] getPeaksArr(ArrayList<CPeak> peaks) {
		if(peaks==null){
			peaks = new ArrayList<CPeak>();
		}
		PointF[] points = new PointF[peaks.size()];
		int i = 0;
		for (CPeak peak : peaks){
			points[i] = new PointF((float)peak.getIndex() / Params.FS, (float)peak.getHeight());
			i++;
		}
		return points;
	}

	private PointF[] getPeaksArr2D(ArrayList<CPeak> peaks) {
		if(peaks==null){
			peaks = new ArrayList<CPeak>();
		}
		PointF[] points = new PointF[peaks.size()];
		int i = 0;
		for (CPeak peak : peaks){
			points[i] = new PointF((float)peak.getWeight() / Params.FS, (float)peak.getHeight());
			i++;
		}
		return points;
	}
	
	private PointF[] getPeaksArr2(ArrayList<CPeak> peaks) {
		if(peaks==null){
			peaks = new ArrayList<CPeak>();
		}
		PointF[] points = new PointF[peaks.size()];
		int i = 0;
		for (CPeak peak : peaks){
			points[i] = new PointF((float)peak.getIndex() / 100, (float)peak.getHeight());
			i++;
		}
		return points;
	}

	/**
	 * @return ICP value as double.
	 */
	public double getIcpValue() {
		Object[] arICP = icpArray.toArray();
		if (arICP.length > 0)
			return ((Double)arICP[arICP.length-1]).doubleValue();
		else
			return 0;
	}
	
	public double[] shortToDouble(short[] array) {
		double[] doubleData = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			doubleData[i] = (double) array[i];
			doubleData[i] /= 32768.0;
		}
		return doubleData;
	}

	private void saveRPValues(SegmentResultsParms valueRP) throws IOException {
		FileOutputStream fos = activity.openFileOutput(RP_VALUES_FILE_NAME, Context.MODE_APPEND);


		StringBuffer buf = new StringBuffer();
		for (int i = SegmentResultsParms.alk; i < SegmentResultsParms.rp_NUMBER; ++i) {
			buf.append(valueRP.getValue(i) + ",");
		}
		buf.append("\n");
		String stringForWrite = buf.toString();
		fos.write(stringForWrite.getBytes(Charset.forName("UTF-8")));
		fos.close();
	}
	
	private void getRPFromFile() {
		SegmentResultsParms parms = new SegmentResultsParms();
		List<String> storeWordList = new ArrayList<String>();
		String[] valueRP;
		String strLine = ""; 
		FileInputStream fis;
		try {
			fis = activity.openFileInput(RP_VALUES_FILE_NAME);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis, Charset.defaultCharset()));
			while ((strLine = br.readLine()) != null) {  
	            storeWordList.add(strLine);      
	        }
			fis.close();
			dis.close();
			br.close();
			
			int kmev = 3;
			int kmev0 = (kmev < storeWordList.size()) ? kmev : storeWordList.size();

			for (int i = storeWordList.size() - kmev0; i < storeWordList.size(); i++) {
				valueRP = storeWordList.get(i).split(",");
				for (int j = 0; j < valueRP.length; j++) {
					parms.setValue(j,Double.parseDouble(valueRP[j]));
				}
				segmentResults.add(parms);
				parms = new SegmentResultsParms();
			}
			FileOutputStream fosICP = activity.openFileOutput(RP_VALUES_FILE_NAME, Context.MODE_PRIVATE);
			fosICP.close();

			for (int i = 0; i < kmev; i++) {
				if(segmentResults.size()>i && segmentResults.get(i) != null){
					saveRPValues(segmentResults.get(i));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double[] readCoefFile(int amountOFVariables, String fileName) {
		double[] coefData1 = new double[amountOFVariables];
		int doubleCounter = 0;
		try {
			InputStream fstream = new FileInputStream(new File(
					ValuesOfSettings.getInstance().getBaseDirectory() + fileName));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				if (strLine.equals(""))
					break;
				coefData1[doubleCounter++] = Double.parseDouble(strLine);
			}

			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e);
		}
		return coefData1;
	}
}