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
import java.util.*;

public class MexAllPeaks {
	public ArrayList<CPeak> mexAllPeaks(double[] data, double K,
			int WindowDuration, double trmaxloc, int Count) {
		ArrayList<CPeak> result = new ArrayList<CPeak>();
		double[] Buf0 = new double[data.length/WindowDuration];
		int[] BufInd0 = new int[data.length/WindowDuration];
		double[] Buf1 = new double[data.length/WindowDuration];
		int[] BufInd1 = new int[data.length/WindowDuration];

		int LengthBuf0 = 0;
		int LengthBuf1 = 0;

		LengthBuf1 = 0;

		int i, j, k, ind;
		double val;

		k = 0;
		for (i = 0; i < data.length - WindowDuration; i += WindowDuration) {
			val = data[i];
			ind = i;
			for (j = i + 1; j <= i + WindowDuration; j++) {
				if (val < data[j]) {
					val = data[j];
					ind = j;
				}
			}
			if (val >= K) {
				Buf0[k] = val;
				BufInd0[k] = ind;
				k = k + 1;
			}

		}
		LengthBuf0 = k;

		// ======================================================================================//
		if (LengthBuf0 > Count) {

			LengthBuf1 = 0;
			k = 0;
			Buf1[k] = 0;
			BufInd1[k] = 0;
			k = k + 1;
			for (i = 1; i < LengthBuf0; i++) {
				if (BufInd0[i] - BufInd0[i - 1] > trmaxloc) {
					Buf1[k] = Buf0[i];
					BufInd1[k] = BufInd0[i];
					k = k + 1;
				} else {
					if (Buf1[k - 1] <= Buf0[i]) {
						Buf1[k - 1] = Buf0[i];
						BufInd1[k - 1] = BufInd0[i];
						//TODO
					}
				}
			}

			LengthBuf1 = k;
			// ErrorCode = 2;
		}

		// ======================================================================================//
		if (LengthBuf1 > Count) {
			for (i = 1; i < LengthBuf1; i++) {
				val = data[BufInd1[i - 1]];
				ind = BufInd1[i - 1];
				for (j = BufInd1[i - 1]; j <= BufInd1[i]; j++) {
					if (val > data[j]) {
						val = data[j];
						ind = j;
					}
				}
				result.add(new CPeak(Buf1[i - 1], BufInd1[i - 1]));
				result.add(new CPeak(val, ind));
			}
			// ErrorCode = 3;
		}
		
		int minFirstId = 0;
		if(data.length>0) {
			double minFirst = data[0];
			for (int f = 0; f < BufInd1[0]; f++) {
				if (minFirst > data[f]) {
					minFirst = data[f];
					minFirstId = f;
				}
			}
			result.add(0, new CPeak(minFirst, minFirstId));
		}
		int minLastId = 0;
		if(LengthBuf1 == 0){
			LengthBuf1++;
		}
		double minLast = Buf1[LengthBuf1 - 1];
		for (int l = BufInd1[LengthBuf1 - 1]; l < data.length; l++) {
			if (minLast > data[l]) {
				minLast = data[l];
				minLastId = l;
			}
		}
		result.add(new CPeak(minLast, minLastId));
		result.add(new CPeak(Buf1[LengthBuf1-1], BufInd1[LengthBuf1-1]));
		
		return result;
	}
	
	public ArrayList<CPeak> mexAllPeaks1s(double[] data, int step) {
		ArrayList<CPeak> result = new ArrayList<CPeak>();
		int i = 0;
        if(data != null && data.length > 10) {
            while (i < data.length - 10) {
                result.add(new CPeak(data[i], i));
                i = i + step;
            }
            result.add(new CPeak(data[data.length - 1], data.length - 1));
        }
		return result;
	}

	public ArrayList<CPeak> mexAllPeaks2D(double[] data, double[] time, int step) {
		ArrayList<CPeak> result = new ArrayList<CPeak>();
		int i = 0;
		while(i<data.length-10){
			result.add(new CPeak(data[i], time[i], i));
			i = i + step;
		}
		if(data.length > 0) {
			result.add(new CPeak(data[data.length - 1], time[data.length - 1], data.length - 1));
		}
		return result;
	}
}
