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
import android.util.Log;

import com.surik.pulm.HeadSense;
import com.surik.pulm.Params;
import com.surik.pulm.ValuesOfSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * Starts Algorithm data monitoring
 */
class SegmentData {
	private static final String TAG = "Algorithm.SegmentData";
	private CFourier fourier;
	private MexAllPeaks mCPeaks;
	private CFilter filter;

	private double alk;

	private double[] sig12;
	private double[] sig13;
	private double[] fSig1 ;

	private ArrayList<CPeak> peaks12;
	private ArrayList<CPeak> peaks12s;
	private ArrayList<CPeak> peaks13;
	private ArrayList<CPeak> peaksFSig1;
	private ArrayList<CPeak> peaks13s;
	private ArrayList<CPeak> peaksFSig1s;
	private ArrayList<CPeak> peaksFSig111111s;

	private ArrayList<CPeak> peaksEN12;
	private ArrayList<CPeak> peaksEN12s;

	private SegmentResultsParms parms;

	/**
	 * Constructor
	 *
	 */
	public SegmentData() {
		fourier = new CFourier();
		mCPeaks = new MexAllPeaks();
		filter = new CFilter();
		parms = new SegmentResultsParms();
	}

	/**
	 * This method gets the raw wave data (without the header), and analyze all
	 * the method returns 0 if every thing is ok, other if signal is not good.
	 *
	 * @param sig00 Array of sig00, represent the data
	 * @return 0 if every thing is ok, other if signal is not good or app is stop.
	 */
	public int processSegment(double[] sig00, double[] coefSig1) {
		int value = 0;
//		sig12 = new double[sig00.length];
		sig13 = new double[sig00.length];
		for (int i = 0; i < sig00.length-1; i++) {
			sig13[i] = sig00[i+1] - sig00[i];
		}

		double[] a = {1};
		int as = 30;

		double[] fSig1_44100 = filter.filtfilt(coefSig1, a, sig00);
//		         fSig1 =  filter.filtfilt(coefSig2, a, fSig11);


		double[] fSig1 = new double[fSig1_44100.length/4];

		for (int i = 0; i < fSig1.length; i++) {
			fSig1[i] = fSig1_44100[4*i];
		}
////////////////////////////////////////////////////////////////////////////////////////////
		double K = 0;//mean;// 0 - 0.0022;
		int WindowDuration = 11025 / 400;
		double trmaxloc = 11025 / (double) 400;
		int Count = 0;
		int peaks1step = 200;
		int p;
		peaksFSig111111s = mCPeaks.mexAllPeaks1s(fSig1, peaks1step);
//		peaksFSig1 = mCPeaks.mexAllPeaks(fSig1, K, WindowDuration, trmaxloc, Count);
//		p = 1;
//		for (int i = 0; i < peaksFSig1.size(); i++) {
//			if (peaksFSig1.get(i).getIndex() % peaks1step != 0) {
//				peaksFSig1s.add(peaksFSig1.get(i).getIndex() / peaks1step + p, peaksFSig1.get(i));
//				p++;
//			}
//		}

////////////////////////////////////////////////////////////////////////////////////////////

        int as1 = 150;
		double vol_perc = 0.25;

		double[] coefSig3 = new double[101];
		for (int i = 0; i < coefSig3.length; i++) {
			coefSig3[i] = (double)1/101;
		}
		double[] fl01 = filter.medianfilter(fSig1, 101);

		double[] fl01_150 = new double[fl01.length/as1];
		for (int i = 0; i < fl01_150.length; i++) {
			fl01_150[i] = fl01[as1*i];
		}

		double[] flow =  filter.filtfilt(coefSig3, a, fl01_150);

//		double flow_mean = 0.0;
//		for (int i = 0; i < flow.length; i++) {
//			flow_mean += flow[i];
//		}
//		flow_mean = flow_mean/flow.length;
//
//		for (int i = 0; i < flow.length; i++) {
//			flow[i] = flow[i] - flow_mean;
//		}

		double[] insp_binary = new double[flow.length + 2];
		insp_binary[0] = 0;
		insp_binary[insp_binary.length - 1] = 0;
		for (int i = 0; i < flow.length; i++) {
			if(flow[i] > 0){
				insp_binary[i + 1] = 1;
			}else{
				insp_binary[i + 1] = 0;
			}
		}

		double[] d_insp_binary = new double[insp_binary.length - 1];
		for (int i = 0; i < d_insp_binary.length; i++) {
			d_insp_binary[i] = insp_binary[i + 1] - insp_binary[i];
		}

		List<Integer> insp_st = new ArrayList<Integer>();
		List<Integer> insp_ed = new ArrayList<Integer>();

		for (int i = 0; i < d_insp_binary.length; i++) {
			if(d_insp_binary[i] > 0){
				insp_st.add(i);
			}
			if(d_insp_binary[i] < 0){
				insp_ed.add(i);
			}
		}

		double[] val = new double[flow.length];
		double[] flow1 = {0};

//		if(insp_st.size() == 0 || insp_ed.size() == 0){
		if(insp_st.size() < 2 || insp_ed.size() < 2){
			value = 1;
			val = makeCumul(flow);
		}else{
			double[] sum_fl = new double[insp_st.size()-1];
			for (int i = 0; i < sum_fl.length; i++) {
				for (int j = insp_st.get(i); j < insp_ed.get(i); j++) {
					sum_fl[i] += Math.abs(flow[j]);
				}
			}
			double sum_fl_m = 0.0;
			for (int i = 0; i < sum_fl.length; i++) {
				if(sum_fl_m < sum_fl[i]){
					sum_fl_m = sum_fl[i];
				}
			}
			List<Integer> insp_st1 = new ArrayList<Integer>();
			List<Integer> insp_ed1 = new ArrayList<Integer>();
			for (int i = 0; i < sum_fl.length; i++) {
				if(sum_fl[i] > sum_fl_m*vol_perc){
					insp_st1.add(insp_st.get(i));
					insp_ed1.add(insp_ed.get(i));
				}
			}

			List<Integer> insp_exp_ind = new ArrayList<Integer>();
			for (int i = 0; i < insp_st1.size(); i++) {
				insp_exp_ind.add(insp_st1.get(i));
			}
			for (int i = 0; i < insp_ed1.size(); i++) {
				insp_exp_ind.add(insp_ed1.get(i));
			}

			for (int i = 0; i < insp_ed1.size(); i++) {
				if(insp_ed1.get(i) <= insp_st1.get(0)){
					insp_ed1.remove(i);
					i--;
				}
			}

			Collections.sort(insp_exp_ind);

			List<Integer> insp_exp_ind1 = new ArrayList<Integer>();
			for (int i = 0; i < insp_exp_ind.size(); i=i+2) {
				insp_exp_ind1.add(insp_exp_ind.get(i));
			}

			int[] ind = new int[(insp_exp_ind1.size() == 0) ? 0 : 2*insp_exp_ind1.size() - 1];
			int ind_max = 0;
			for (int i = 0; i < ind.length; i++) {
				ind[i] = insp_exp_ind.get(i+1) - insp_exp_ind.get(i);
				if(ind_max < ind[i]){
					ind_max = ind[i];
				}
			}

			double[] flowinsp = new double[ind_max];
			double[] flowexp  = new double[ind_max];

			for (int i = 0; i < insp_exp_ind1.size(); i++) {
				for (int j = insp_exp_ind.get(2*i)+1; j < insp_exp_ind.get(2*i+1); j++) {
					flowinsp[j - (insp_exp_ind.get(2*i)+1)] =
							flowinsp[j - (insp_exp_ind.get(2*i)+1)] + flow[j];
				}
			}

			for (int i = 0; i < insp_exp_ind1.size()-1; i++) {
				for (int j = insp_exp_ind.get(2*(i + 1)); j > insp_exp_ind.get(2*(i) + 1); j--) {
					flowexp[(insp_exp_ind.get(2*(i + 1))) - j] =
							flowexp[(insp_exp_ind.get(2*(i + 1))) - j] + flow[j];
				}
			}
			val = makeCumul(flow);

			flow1 = new double[flowinsp.length + flowexp.length];
			for (int i = 0; i < flow1.length; i++) {
				if (i < flowinsp.length) {
					flow1[i] = flowinsp[i];
				} else {
					flow1[i] = flowexp[flowexp.length - (i - flowinsp.length) - 1];
				}
			}
			val = makeCumul(flow1);

			double val_max = 0.0;
			for (int i = 0; i < val.length; i++) {
				if(val_max < val[i]){
					val_max = val[i];
				}
			}

			for (int i = 0; i < val.length; i++) {
				val[i] = val_max - val[i];
			}

			for (int i = 0; i < flow1.length; i++) {
				flow1[i] = - flow1[i];
			}
		}



		Log.i(TAG, "3. Starting mpic2pic...");
		if (!mpic2pic(val, flow1)) {
//			value = 1;
		}

		return value;
	}


	public SegmentResultsParms getResults() {
		return parms;
	}

	/**
	 * M Pic 2 Pics
	 * get spector pics
	 *
	 */
	boolean mpic2pic(double[] val, double[] flow) {

		double K = 0;//mean;// 0 - 0.0022;
		int WindowDuration = 11025 / 400;
		double trmaxloc = 11025 / (double) 400;
		int Count = 0;
		int peaks1step = 1;
		int p;

////////////////////////////////////////////////////////////////////////////////////////////
//		peaksFSig1s = mCPeaks.mexAllPeaks1s(fSig1, peaks1step);
////		peaksFSig1 = mCPeaks.mexAllPeaks(fSig1, K, WindowDuration, trmaxloc, Count);
////		p = 1;
////		for (int i = 0; i < peaks13.size(); i++) {
////			if (peaks13.get(i).getIndex() % peaks1step != 0) {
////				peaksFSig1s.add(peaksFSig1.get(i).getIndex() / peaks1step + p,
////						peaksFSig1.get(i));
////				p++;
////			}
////		}
//
//////////////////////////////////////////////////////////////////////////////////////////////
//		peaks13s = mCPeaks.mexAllPeaks1s(sig13, peaks1step);
//		peaks13 = mCPeaks.mexAllPeaks(sig13, K, WindowDuration, trmaxloc, Count);
//		p = 1;
//		for (int i = 0; i < peaks13.size(); i++) {
//			if (peaks13.get(i).getIndex() % peaks1step == 0) {
//			} else {
//				peaks13s.add(peaks13.get(i).getIndex() / peaks1step + p,
//						peaks13.get(i));
//				p++;
//			}
//		}
//		double[] mx13 = new double[peaks13.size() / 2];
//		Object[] arpeaks13 = peaks13.toArray();
//		for (int j = 0, i = 0; i < arpeaks13.length - 1; i += 2, j++) {
//			CPeak p1 = (CPeak) arpeaks13[i];
//			CPeak p2 = (CPeak) arpeaks13[i + 1];
//			mx13[j] = Math.abs(p2.getHeight() - p1.getHeight());
//		}
//		Arrays.sort(mx13);
//		parms.setValue(SegmentResultsParms.amp13, countAverangeRange_7(mx13));
//////////////////////////////////////////////////////////////////////////////////////////////
//		peaks12s = mCPeaks.mexAllPeaks1s(val, peaks1step);
//		peaks12 = mCPeaks.mexAllPeaks(val, K, WindowDuration, trmaxloc, Count);

		peaks12s = mCPeaks.mexAllPeaks2D(flow, val, peaks1step);
//		peaks12 = mCPeaks.mexAllPeaks(val, K, WindowDuration, trmaxloc, Count);
//		double[] mx12 = new double[peaks12.size() / 2];
//		Object[] arpeaks12 = peaks12.toArray();
//		for (int j = 0, i = 0; i < arpeaks12.length - 1; i += 2, j++) {
//			CPeak p1 = (CPeak) arpeaks12[i];
//			CPeak p2 = (CPeak) arpeaks12[i + 1];
//			mx12[j] = Math.abs(p2.getHeight() - p1.getHeight());
//		}
//		Arrays.sort(mx12);
//		parms.setValue(SegmentResultsParms.amp12, countAverangeRange_7(mx12));

		return true;
	}
	/**
	 * Count averange range 7
	 *
	 */
	private double countAverangeRange_7(double[] mxP) {
		Arrays.sort(mxP);
		double av = 0;
		int count = mxP.length;
		for (int i = count * 7/ 20; i < count * 19 / 20; i++)
			av += (double) mxP[i];
		if (count != 0) {
			av /= ((count * 19 / 20) - (count  * 7/ 20));
		}
		return (double) av;
	}

	public ArrayList<CPeak> getPeaks12s() {
		return peaks12s;
	}

	public ArrayList<CPeak> getPeaksEN12s() {
		return peaksEN12s;
	}


	public double[] makeCumul(double[] in) {
		double[] out = new double[in.length];
		double total = 0;
		for (int i = 0; i < in.length; i++) {
			total += in[i];
			out[i] = total;
		}
		return out;
	}
}
