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
/**
 * Fourier transforms are specified here
 */
public class CFourier {
	private double pic;
	private static double[] vector;
	private int vLength;
	private double[] sig = null;
	private static final Object object = new Object();

	public CFourier() {
		pic = 4.0 * Math.atan(1.0);
		vLength = 0;
	}
	
	static double[] cosDelta;
	static double[] sinDelta;

	static {
		int N = 32;
		cosDelta = new double[N];
		sinDelta = new double[N];
		for (int i = 1; i < N; i++) {
			double delta = - 2*Math.PI / (1 << i);
			cosDelta[i] = Math.cos(delta);
			sinDelta[i] = Math.sin(delta);
		}
	}

	// FFT 1D
	public void complexFFT(double[] vector, int sample_rate, int sign) {

		// variables for the fft
		int n, mmax, m, j, istep, i, k1, k2;
		double wtemp, wr, wpr, wpic, wi, theta, tempr, tempic;
		double swaptemp;

		// binary inversion (note that the indexes
		// start from 0 witch means that the
		// real part of the complex is on the even-indexes
		// and the complex part is on the odd-indexes)
		n = sample_rate << 1;
		j = 0;
		for (i = 0; i < n / 2; i += 2) {
			if (j > i) {
				swaptemp = vector[j];
				vector[j] = vector[i];
				vector[i] = swaptemp;
				swaptemp = vector[j + 1];
				vector[j + 1] = vector[i + 1];
				vector[i + 1] = swaptemp;
				if ((j / 2) < (n / 4)) {
					k1 = (n - (i + 2));
					k2 = (n - (j + 2));
					swaptemp = vector[k1];
					vector[k1] = vector[k2];
					vector[k2] = swaptemp;
					k1++;
					k2++;
					swaptemp = vector[k1];
					vector[k1] = vector[k2];
					vector[k2] = swaptemp;
				}
			}
			m = n >> 1;
			while (m >= 2 && j >= m) {
				j -= m;
				m >>= 1;
			}
			j += m;
		}
		// end of the bit-reversed order algorithm

		// Danielson-Lanzcos routine
		mmax = 2;
		while (n > mmax) {
			istep = mmax << 1;
			theta = (double) sign * (2 * pic / mmax);
			wtemp = (double) Math.sin(0.5 * theta);
			wpr = -2.0f * wtemp * wtemp;
			wpic = (double) Math.sin(theta);
			wr = 1.0f;
			wi = 0.0f;
			for (m = 1; m < mmax; m += 2) {
				for (i = m; i <= n; i += istep) {
					j = i + mmax;
					tempr = wr * vector[j - 1] - wi * vector[j];
					tempic = wr * vector[j] + wi * vector[j - 1];
					vector[j - 1] = (double) (vector[i - 1] - tempr);
					vector[j] = (double) (vector[i] - tempic);
					vector[i - 1] += (double) tempr;
					vector[i] += (double) tempic;
				}
				wr = (wtemp = wr) * wpr - wi * wpic + wr;
				wi = wi * wpr + wtemp * wpic + wi;
			}
			mmax = istep;
		}
	}

	public double[] specSig(int start, int end) {
		double[] spec = new double[end - start + 1];
		if(vector != null) {
			for (int n = 0, k = start * 2; n < spec.length; n++, k += 2)
				spec[n] = Math.sqrt(vector[k] * vector[k] + vector[k + 1] * vector[k + 1]);
		}
		return spec;
	}
	
	public double[] specSig(double[] data, int start, int end) {
		double[] spec = new double[end - start + 1];
		for (int n = 0, k = start*2; n < spec.length; n++, k += 2)
			spec[n] =  Math.sqrt(data[k] * data[k] + data[k + 1] * data[k + 1]);
		return spec;
	}

	public double[] fFT(double[] data, int NFFT, int sign) {
		// new complex array of size n=2*sample_rate
		synchronized (object) {
			if (vLength != NFFT) {
				vLength = NFFT;
				if (vector == null) {
					vector = new double[2 * NFFT];
				} else {
					if (vector.length != 2 * NFFT) {
						setVector(new double[2 * NFFT]);
					}
				}
			}
		}
		
		for (int i = 0; i < NFFT; i++) {
			if (i < data.length)
				vector[2 * i] = data[i];
			else
				vector[2 * i] = 0;
			vector[2 * i + 1] = 0;
		}
		complexFFT(vector, NFFT, sign);
		return getVector();
	}
	
	public void ifft(double[] data) {
		int N = data.length/2;

		complexFFT(data, data.length/2, 1);
		
		for (int i = 0; i < N; i++) {
			data[i] = data[i]/N;
		}
	}

	public static double[] getVector() {
		return vector;
	}

	public static void setVector(double[] vector) {
		CFourier.vector = vector;
	}
}
