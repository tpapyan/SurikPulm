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

import com.surik.pulm.ValuesOfSettings;

import java.util.*;

/**
 * All parameters for algorithm monitoring
 */
public class SegmentResultsParms {
	
	public static final int alk   = 0;
	public static final int kk0   = 1;
	
	public static final int amp11  = 2;
	public static final int amp12  = 3;
	public static final int amp13  = 4;
	public static final int amp20  = 5;
	public static final int ampF0 = 6;
	
	public static final int s11 = 7;
	public static final int s12 = 8;
	public static final int s13 = 9;
	public static final int s20 = 10;
	public static final int sF0 = 11;
	
	public static final int in_EnF0_all0 = 12;
	public static final int in_En11_all0 = 13;
	public static final int in_En12_all0 = 14;
	public static final int in_En13_all0 = 15;
	public static final int in_En2_all0 = 16;
	
	public static final int vectF0 = 17;
		
	public static final int rp_NUMBER   = 18;
	
	private double[] values;

	/**
	 * Constructor
	 */
	public SegmentResultsParms(){
		values = new double[rp_NUMBER];
		for(int i = 0; i < rp_NUMBER; i++)
			values[i] = 0;
	}
	public void setValue(int i,double v){
		if(i<values.length) {
			values[i] = v;
		}
	}
	public double getValue(int i){
		if(i<values.length) {
			return values[i];
		}
		return 0;
	}

	/**
	 * Calculate verage
	 *
	 */

	public void calculateAverage(ArrayList<SegmentResultsParms> segmentResults){
		Object[] arResult = segmentResults.toArray();
		int kmev = 3;
		if(ValuesOfSettings.getInstance().isDemo()){
			kmev = 1;
		}
//		int kmev0 = (kmev < arResult.length) ? kmev : 1;
		int kmev0 = (kmev < arResult.length) ? kmev : arResult.length;
		int jFirst = 0;
		int jLast = kmev0;
//		int jFirst = arResult.length - kmev0;
//		int jLast = arResult.length;
		for (int j = jFirst; j < jLast; j++){
			SegmentResultsParms prms = (SegmentResultsParms)arResult[j];
			for(int i = 0; i < rp_NUMBER; i++) // the rest ?
				values[i] += prms.values[i];

		}		
		
		values[alk] = (Math.log10(values[alk] / kmev0 )); //Math.pow((-Math.log10(values[alk] / kmev0 )), 3.0);
		values[kk0] = (values[kk0] / kmev0);//Math.pow((-Math.log10(values[kk0] / kmev0)), 2.0);
		
		values[amp11] =  Math.pow((Math.log10(values[amp11] / kmev0 )), 3.0);
		values[amp12] =  Math.pow((Math.log10(values[amp12] / kmev0 )), 3.0);
		values[amp13] =  Math.pow((Math.log10(values[amp13] / kmev0 )), 3.0);
		values[amp20] =  Math.pow((Math.log10(values[amp20] / kmev0 )), 3.0);
		values[ampF0] =  Math.pow((Math.log10(values[ampF0] / kmev0 )), 3.0);
		
		values[s11] =  Math.log10(values[s11] / kmev0 );
		values[s12] =  Math.log10(values[s12] / kmev0 );
		values[s13] =  Math.log10(values[s13] / kmev0 );
		values[s20] =  Math.log10(values[s20] / kmev0 );
		values[sF0] =  Math.log10(values[sF0] / kmev0 );
		
		values[in_EnF0_all0] =  Math.log10(values[in_EnF0_all0] / kmev0);
		values[in_En11_all0] =  Math.log10(values[in_En11_all0] / kmev0);
		values[in_En12_all0] =  Math.log10(values[in_En12_all0] / kmev0);
		values[in_En13_all0] =  Math.log10(values[in_En13_all0] / kmev0);
		values[in_En2_all0]  =  Math.log10(values[in_En2_all0] / kmev0);
		
		values[vectF0] =  Math.log(values[vectF0] / kmev0);
		
	}
}
