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
import java.util.Arrays;

/**
 * Generates Spectrogram for app algorithm
 */
public class Spectrogram{
    
    public static final int SPECTROGRAM_DEFAULT_FFT_SAMPLE_SIZE = 1024;

    private double[] sig0;
    private static double[][] absoluteSpectrogram; // absolute spectrogram
    private int fftSampleSize;      // number of sample in fft, the value needed to be a number to power of 2
    private int overlapFactor;      // 1/overlapFactor overlapping, e.g. 1/4=25% overlapping
    private int numFrames;  // number of frames of the spectrogram
    private int numFrequencyUnit;   // number of y-axis unit
//    private int fs = 11025;
    private int win_size = 1;
    private CFourier fourier;

    private static final Object object = new Object();

    /**
     * Constructor
     * @param fftSampleSize number of sample in fft, the value needed to be a number to power of 2
     * @param overlapFactor 1/overlapFactor overlapping, e.g. 1/4=25% overlapping, 0 for no overlapping
     */
    public Spectrogram(double[] sig0, int win_size, int fftSampleSize, int overlapFactor, int fs) {
        fourier = new CFourier();
        if(sig0 == null){
            this.sig0 = new double[1];
        }else {
            this.sig0 = Arrays.copyOf(sig0, sig0.length);
        }
//        this.fs = fs;
        if(win_size != 0) {
            this.win_size = win_size;
        }
            
        if (Integer.bitCount(fftSampleSize)==1){
                this.fftSampleSize=fftSampleSize;
        }
        else{
                System.err.print("The input number must be a power of 2");
                this.fftSampleSize=SPECTROGRAM_DEFAULT_FFT_SAMPLE_SIZE;
        }

        this.overlapFactor=overlapFactor;

        buildSpectrogram();
    }
    
    /**
     * Build spectrogram
     */
    private void buildSpectrogram(){
    	
    	double[] amplitudes = new double[sig0.length];
    	System.arraycopy(sig0, 0, amplitudes, 0, sig0.length);
  
        int numSamples = sig0.length;
        
        int pointer=0;
        if (overlapFactor>1){
            int backSamples=win_size*(overlapFactor-1)/overlapFactor;
            int numOverlappedSamples=(numSamples-backSamples)*overlapFactor;
            int win_size_1=win_size-1;
            double[] overlapAmp= new double[numOverlappedSamples];
            pointer=0;
            for (int i=0; i<amplitudes.length; i++){
                overlapAmp[pointer++]=amplitudes[i];
                if (amplitudes.length - i >(win_size-backSamples) && pointer%win_size==win_size_1){
                        // overlap
                        i-=backSamples;
                }
            }
            numSamples=numOverlappedSamples;
            amplitudes=overlapAmp;
        }

        if(win_size!=0) {
            numFrames = numSamples / win_size;

            int m = win_size / 2;
            double r;
            double pi = Math.PI;
            double[] win = new double[win_size];
            if(m != 0) {
                r = pi / m;
                for (int n = -m; n < m; n++)
                    win[m + n] = 0.54f + 0.46f * Math.cos(n * r);
            }

            double[][] signals=new double[numFrames][];
            for(int f=0; f<numFrames; f++) {
              signals[f]=new double[fftSampleSize];
              int startSample=f*win_size;
              for (int n=0; n<win_size; n++){
                  if((startSample+n) < amplitudes.length){
                    signals[f][n]=amplitudes[startSample+n]*win[n];
                  }

              }
            }
            synchronized (object) {
                if (absoluteSpectrogram == null) {
                    absoluteSpectrogram = new double[numFrames][fftSampleSize / 2];
                } else {
                    if (absoluteSpectrogram.length != numFrames && absoluteSpectrogram[0].length != fftSampleSize / 2) {
                        absoluteSpectrogram = new double[numFrames][fftSampleSize / 2];
                    }
                }
            }
            for (int i=0; i<numFrames; i++){
              double[] signal = new double[fftSampleSize];
              System.arraycopy(signals[i], 0, signal, 0, fftSampleSize);
              double[] complexNumbers = fourier.fFT(signal, fftSampleSize, 1);
              for (int j = 0; j < fftSampleSize; j += 2) {
                  absoluteSpectrogram[i][j / 2] = Math.sqrt(complexNumbers[j] * complexNumbers[j] + complexNumbers[j + 1] * complexNumbers[j + 1]);
              }
            }
            int a = 0;
            if (absoluteSpectrogram.length>0){
              numFrequencyUnit=absoluteSpectrogram[0].length;
            }
        }
    }

	/**
	 * Get spectrogram: spectrogram[time][frequency]=intensity
	 * 
	 * @return absolute spectrogram
	 */
	public double[][] getAbsoluteSpectrogramData() {
        return Arrays.copyOf(absoluteSpectrogram, absoluteSpectrogram.length);
	}
    
	public int getNumFrequencyUnit() {
		return numFrequencyUnit;
	}
}