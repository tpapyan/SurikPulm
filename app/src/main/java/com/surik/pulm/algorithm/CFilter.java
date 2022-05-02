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
 * Filters which are used in calculation of algorithm
 */
public class CFilter {

	// 1D MEDIAN FILTER implementation
	// signal - input signal
	// result - output signal
	// N - length of the signal
	private double[] _medianfilter(double[] signal, int N) {
		double[] result = new double[signal.length - N];
		// Move window through all doubles of the signal
		for (int i = N/2; i < signal.length - (N/2+1); i++) {
			// Pick up window doubles
			double[] window = new double[N];
			for (int j = 0; j < N; ++j)
				window[j] = signal[i - N/2 + j];
			Arrays.sort(window);		
			// Get result - the middle double
			result[i - N/2] = window[N / 2];
		}
		return result;
	}

	// 1D MEDIAN FILTER wrapper
	// signal - input signal
	// result - output signal
	// N - length of the signal
	public double[] medianfilter(double[] signal, int N) {
		// Allocate memory for signal extension
		double[] extension = new double[signal.length + N];
		for (int i = 0, j = N/2; i < signal.length; ++i, ++j)
			extension[j] = signal[i];
		// Call median filter implementation
		return _medianfilter(extension, N);
	}
	
	  /**
     * This method is used to perform median filtering on the image object passed.

     */
    public double[][] medianFilter(double[][] signal, int kf1, int kf2, int width, int height){
        /** 
         * This array will store the output of the median filter operation which will
         * be later written back to the original image pixels.
         */
        double[][] outputPixels = new double[width][height];
        
        /**
         * Buff is a 2D square of odd size like 3x3, 5x5, 7x7, ...
         * For simplicity storing it into 1D array.
         */
        double buff[];
        
        /** Median Filter operation */
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                buff = new double[kf1 * kf2];
                int count = 0;
                for(int r = y - (kf2 / 2); r <= y + (kf2 / 2); r++){
                    for(int c = x - (kf1 / 2); c <= x + (kf1 / 2); c++){
                        if(r < 0 || r >= height || c < 0 || c >= width){
                            /** Some portion of the mask is outside the image. */
                            continue;
                        }else{
                        	buff[count] =  signal[c][r];
                            count++;
                        }
                    }
                }
                
                /** sort buff array */
                java.util.Arrays.sort(buff);
                
//                for(int i = y - (maskSize / 2); r <= y + (maskSize / 2); r++){
                /** save median value in outputPixels array */
//                outputPixels[x+y*width] = buff[count/2];
                outputPixels[x][y] = buff[buff.length/2];
            }
        }
        return outputPixels;
    }

     /**
     * This method is used to perform median filtering on the image object passed.
     *
     * @param maskSize - The size of the mask is an odd integer like 3, 5, 7 ? etc.
     */
    public double[][] medianFilter(double[][] signal, int maskSize, int width, int height){
        /**
         * This array will store the output of the median filter operation which will
         * be later written back to the original image pixels.
         */
        double[][] outputPixels = new double[width][height];

        /**
         * Buff is a 2D square of odd size like 3x3, 5x5, 7x7, ...
         * For simplicity storing it into 1D array.
         */
        double buff[];

        /** Median Filter operation */
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                buff = new double[maskSize * maskSize];
                int count = 0;
                for(int r = y - (maskSize / 2); r <= y + (maskSize / 2); r++){
                    for(int c = x - (maskSize / 2); c <= x + (maskSize / 2); c++){
                        if(r < 0 || r >= height || c < 0 || c >= width){
                            /** Some portion of the mask is outside the image. */
                            continue;
                        }else{
                            buff[count] =  signal[c][r];
                            count++;
                        }
                    }
                }

                /** sort buff array */
                java.util.Arrays.sort(buff);

//                for(int i = y - (maskSize / 2); r <= y + (maskSize / 2); r++){
                /** save median value in outputPixels array */
//                outputPixels[x+y*width] = buff[count/2];
                outputPixels[x][y] = buff[buff.length/2];
            }
        }
        return outputPixels;
    }

    /**
     * Calculates the median of a kxk pixel neighbourhood (including centre pixel).
     *
     * @param input The input image 2D array
     * @param k Dimension of the kernel
     * @param w The image width
     * @param h The image height
     * @param x The x coordinate of the centre pixel of the array
     * @param y The y coordinate of the centre pixel of the array
     * @return The median of the kxk pixels
     */
    public int median(int[][] input, int k,int w, int h, int x, int y)
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
                if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
                    ++number;
                }
            }
        }
        if(number==0)
            return 0;
        Arrays.sort(supp);
        return supp[((k*k-1)/2)];
    }
    /**
     * Calculates the maximum of a kxk pixel neighbourhood (including centre pixel).
     *
     * @param input The input image 2D array
     * @param k Dimension of the kernel
     * @param w The image width
     * @param h The image height
     * @param x The x coordinate of the centre pixel of the array
     * @param y The y coordinate of the centre pixel of the array
     * @return The maximum of the kxk pixels
     */
    public int max(int[][] input, int k, int w, int h, int x, int y)
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
                if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
                    ++number;
                }
            }
        }
        if(number==0)
            return 0;
        Arrays.sort(supp);
        return supp[k*k-1];
    }
    /**
     * Calculates the minimum of a kxk pixel neighbourhood (including centre pixel).
     *
     * @param input The input image 2D array
     * @param k Dimension of the kernel
     * @param w The image width
     * @param h The image height
     * @param x The x coordinate of the centre pixel of the array
     * @param y The y coordinate of the centre pixel of the array
     * @return The minimum of the kxk pixels
     */
    public int min(int[][] input, int k,int w, int h, int x, int y)
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
                if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
                    ++number;
                }
            }
        }
        if(number==0)
            return 0;
        Arrays.sort(supp);
        return supp[0];
    }
    public double[] smoothfilter(double[] signal, int window_size) {
        double[] result = new double[signal.length];
        for (int i = 0, j = 1; i < signal.length; i++)
        {
            double sum = 0;
            if (i == 0)
            {
                result[0] = signal[0];
                j += 2;
            } else if (i < window_size / 2){
                for (int h = 0; h < j; h++)
                {
                    sum += signal[h];
                }
                result[i] = sum / j;
                j += 2;
            } else if (i > signal.length - window_size / 2 - 1) {
                if(i == signal.length - 1){
                    result[i] = signal[i];
                }else{
                    j -= 2;
                    for (int h = signal.length - j; h < signal.length; h++) {
                        sum += signal[h];
                    }
                    result[i] = sum / j;
                }

            } else {
                for (int h = 0; h < j; h++)
                {
                    sum += signal[i - window_size / 2 + h];
                }
                result[i] = sum / j;
            }
        }
        return result;//smoothfilter1(result, signal, window_size);
    }

    public double[] filtfilt(double[] b, double[] x)
    {
        int nfilt;
        int i, j;
        double[] tmpb;
        double[] tmpa;


        nfilt = b.length;
        tmpb = new double[nfilt];
        tmpa = new double[nfilt];

        for (i=0; i<b.length; i++)
            tmpb[i] = b[i];
//	            for (i=0; i<a.length; i++)
        tmpa[0] = 1;
//	            for (i=a.length; i<nfilt; i++)
//	                tmpa[i] = 0.0;


        int nfact = 3*(nfilt-1); //Length of edge transients
        int rwlen = 3*nfilt-5;

        int ylen = 2*nfact+x.length;
        double[] y = new double[ylen];

        double[] yRet;
        if (!(x.length<=nfact)) //Input data too short!
        {
            //Solve system of linear equations for initial conditions
            //  zi are the steady-state states of the filter b(z)/a(z) in the state-space
            //  implementation of the 'filter' command.
            int[] rows = new int[rwlen];
            for (i=0; i<=nfilt-2; i++)
                rows[i] = i;
            for (i=nfilt-1; i<=2*nfilt-4; i++)
                rows[i] = i-nfilt+2;
            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
                rows[i] = i-2*nfilt+3;

            int[] cols = new int[rwlen];
            for (i=0; i<=nfilt-2; i++)
                cols[i] = 0;
            for (i=nfilt-1; i<=2*nfilt-4; i++)
                cols[i] = i-nfilt+2;
            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
                cols[i] = i-2*nfilt+4;

            double[] data = new double[rwlen];
            data[0] = 1.0+tmpa[1];
            for (i=1; i<=nfilt-2; i++)
                data[i] = tmpa[i+1];
            for (i=nfilt-1; i<=2*nfilt-4; i++)
                data[i] = 1.0;
            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
                data[i] = -1.0;

            int N = nfilt-1;
            double[][] sp = new double[N][N];

            for (i=0; i<N; i++)
            {
                for (j=0; j<N; j++)
                    sp[i][j] = 0.0f;
            }

            for (i=0; i<rwlen; i++)
                sp[rows[i]][cols[i]] = data[i];

            double[] denum = new double[N];
            for (i=0; i<N; i++)
                denum[i] = 0.0;
            for (i=2; i<nfilt+1; i++)
                denum[i-2] = b[i-1]-tmpa[i-1]*b[0];

            double[] zi = new double[N];
            for (i=0; i<N; i++)
                zi[i] = 0.0;

            sp = inverse(sp);
//	            sp = MathUtils.inverse(sp);

            double tmp;
            for (i=0; i<N; i++)
            {
                tmp=0.0;

                for (j=0; j<N; j++)
                    tmp += sp[i][j]*denum[i];

                zi[i] = tmp;
            }

            //Extrapolate beginning and end of data sequence using a "reflection
            //  method".  Slopes of original and extrapolated sequences match at
            //  the end points.
            //  This reduces end effects.
            for (i=0; i<nfact; i++)
                y[i] = 2*x[0]-x[nfact-i];

            for (i=0; i<x.length; i++)
                y[i+nfact] = x[i];

            for (i=0; i<nfact; i++)
                y[nfact+x.length+i] = 2*x[x.length-1]-x[x.length-2-i];

            //Filter, reverse the data, filter again, and reverse the data again
            for (i=0; i<N; i++)
                zi[i] = zi[i]*y[0];

            y = filter(tmpb, tmpa, y, false, zi);
            y = reverse(y);
//	            y = SignalProcUtils.reverse(y);

            y = filter(tmpb, tmpa, y, false, zi);
            y = reverse(y);
//	            y = SignalProcUtils.reverse(y);

            // remove extrapolated pieces of y to write the output to x
            yRet = new double[x.length];
            for (i=0; i<x.length; i++)
                yRet[i] = y[i+nfact];
        }
        else
        {
            double[] a = { 1 };
            yRet = filter(b, a, x);
        }

        return yRet;
    }

    public double[] filtfilt(double[] b, double[] a, double[] x)
	    {
	        int nfilt;
	        int i, j;
	        double[] tmpb = null;
	        double[] tmpa = null;

	        if (b.length>a.length)
	        {
	            nfilt = b.length;
	            tmpb = new double[nfilt];
	            tmpa = new double[nfilt];

	            for (i=0; i<b.length; i++)
	                tmpb[i] = b[i];
	            for (i=0; i<a.length; i++)
	                tmpa[i] = a[i];
	            for (i=a.length; i<nfilt; i++)
	                tmpa[i] = 0.0;
	        }
	        else
	        {
	            nfilt = a.length;
	            tmpb = new double[nfilt];
	            tmpa = new double[nfilt];

	            for (i=0; i<a.length; i++)
	                tmpa[i] = a[i];
	            for (i=0; i<b.length; i++)
	                tmpb[i] = b[i];
	            for (i=b.length; i<nfilt; i++)
	                tmpb[i] = 0.0;
	        }

	        int nfact = 3*(nfilt-1); //Length of edge transients
	        int rwlen = 3*nfilt-5;

	        int ylen = 2*nfact+x.length;
	        double[] y = new double[ylen];

	        double[] yRet = null;
	        if (!(x.length<=nfact)) //Input data too short!
	        {
	            //Solve system of linear equations for initial conditions
	            //  zi are the steady-state states of the filter b(z)/a(z) in the state-space
	            //  implementation of the 'filter' command.
	            int[] rows = new int[rwlen];
	            for (i=0; i<=nfilt-2; i++)
	                rows[i] = i;
	            for (i=nfilt-1; i<=2*nfilt-4; i++)
	                rows[i] = i-nfilt+2;
	            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
	                rows[i] = i-2*nfilt+3;

	            int[] cols = new int[rwlen];
	            for (i=0; i<=nfilt-2; i++)
	                cols[i] = 0;
	            for (i=nfilt-1; i<=2*nfilt-4; i++)
	                cols[i] = i-nfilt+2;
	            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
	                cols[i] = i-2*nfilt+4;

	            double[] data = new double[rwlen];
	            data[0] = 1.0+tmpa[1];
	            for (i=1; i<=nfilt-2; i++)
	                data[i] = tmpa[i+1];
	            for (i=nfilt-1; i<=2*nfilt-4; i++)
	                data[i] = 1.0;
	            for (i=2*nfilt-3; i<=3*nfilt-6; i++)
	                data[i] = -1.0;

	            int N = nfilt-1;
	            double[][] sp = new double[N][N];

	            for (i=0; i<N; i++)
	            {
	                for (j=0; j<N; j++)
	                    sp[i][j] = 0.0f;
	            }

	            for (i=0; i<rwlen; i++)
	                sp[rows[i]][cols[i]] = data[i];

	            double[] denum = new double[N];
	            for (i=0; i<N; i++)
	                denum[i] = 0.0;
	            for (i=2; i<nfilt+1; i++)
	                denum[i-2] = b[i-1]-tmpa[i-1]*b[0];

	            double[] zi = new double[N];
	            for (i=0; i<N; i++)
	                zi[i] = 0.0;

	            sp = inverse(sp);
//	            sp = MathUtils.inverse(sp);

	            double tmp;
	            for (i=0; i<N; i++)
	            {
	                tmp=0.0;

	                for (j=0; j<N; j++)
	                    tmp += sp[i][j]*denum[i];

	                zi[i] = tmp;
	            }

	            //Extrapolate beginning and end of data sequence using a "reflection
	            //  method".  Slopes of original and extrapolated sequences match at
	            //  the end points.
	            //  This reduces end effects.
	            for (i=0; i<nfact; i++)
	                y[i] = 2*x[0]-x[nfact-i];

	            for (i=0; i<x.length; i++)
	                y[i+nfact] = x[i];

	            for (i=0; i<nfact; i++)
	                y[nfact+x.length+i] = 2*x[x.length-1]-x[x.length-2-i];

	            //Filter, reverse the data, filter again, and reverse the data again
	            for (i=0; i<N; i++)
	                zi[i] = zi[i]*y[0];

	            y = filter(tmpb, tmpa, y, false, zi);
	            y = reverse(y);
//	            y = SignalProcUtils.reverse(y);

	            y = filter(tmpb, tmpa, y, false, zi);
	            y = reverse(y);
//	            y = SignalProcUtils.reverse(y);

	            // remove extrapolated pieces of y to write the output to x
	            yRet = new double[x.length];
	            for (i=0; i<x.length; i++)
	                yRet[i] = y[i+nfact];
	        }
	        else
	        {
	            yRet = filter(b,a, x);
	        }

	        return yRet;
	    }


    public static double[] filter(double[] b, double[] x)
    {
        double[] a = new double[1];
        a[0] = 1.0;

        return filter(b, a, x);
    }

    public static double[] filter(double[] b, double[] a, double[] x)
    {
        return filter(b, a, x, false);
    }

    public static double[] filter(double[] b, double[] x, boolean bNormalize)
    {
        double[] a = new double[1];
        a[0] = 1.0;

        return filter(b, a, x, bNormalize);
    }

    public static double[] filter(double[] b, double[] a, double[] x, boolean bNormalize)
    {
        double[] zi = new double[Math.max(a.length, b.length)-1];
        Arrays.fill(zi, 0.0);

        return filter(b, a, x, bNormalize, zi);
    }

    public static double[] filter(double[] b, double[] x, boolean bNormalize, double[] zi)
    {
        double[] a = new double[1];
        a[0] = 1.0;

        return filter(b, a, x, bNormalize, zi);
    }

    //Time domain digital filtering
    //  a[0]*y[n] = b[0]*x[n] + b[1]*x[n-1] + ... + b[nb]*x[n-nb]
    //              - a[1]*y[n-1] - ... - a[na]*y[n-na]
    //  b and a are filter coefficients (impulse response of the filter)
    //  If bNormalize is true, all the coeffs are normalized with a[0].
    // The initial conditions should be specified in zi.
    // Setting zi to all zeroes causes no initial conditions to be used.
    // Length of zi should be max(a.length, b.length)-1.
    public static double[] filter(double[] b, double[] a, double[] x, boolean bNormalize, double[] zi)
    {
        int n;
        double x_terms;
        double y_terms;
        int ind;

        double[] y = new double[x.length];

        int nb = b.length-1;
        int na = a.length-1;

        if (bNormalize)
        {
            //Normalize with a[0] first
            if (a[0]!=1.0)
            {
                for (n=0; n<b.length; n++)
                    b[n] /= a[0];
                for (n=0; n<a.length; n++)
                    a[n] /= a[0];
            }
        }

        for (n=0; n<x.length; n++)
        {
            x_terms = 0.0;
            for (ind=n; ind>n-nb-1; ind--)
            {
                if (ind>=0)
                    x_terms += b[n-ind]*x[ind];
                else
                    x_terms += b[n-ind]*zi[-ind-1];
            }

            y_terms = 0.0;
            for (ind=n-1; ind>n-na-1; ind--)
            {
                if (ind>=0)
                    y_terms += -a[n-ind]*y[ind];
            }

            y[n] = x_terms + y_terms;
        }

        return y;
    }

    public static double [] reverse(double [] x)
    {
        double [] y = new double[x.length];
        for (int i=0; i<x.length; i++)
            y[i] = x[x.length-i-1];

        return y;
    }

    public static double[][] inverse(double[][] matrix)
    {
        double[][] invMatrix;
        if (matrix.length==1) //Diagonal matrix
        {
            invMatrix = new double[1][matrix[0].length];
            invMatrix[0] = inverse(matrix[0]);
        }
        else //Full square matrix
        {
            invMatrix = new double[matrix.length][matrix.length];
            for (int i=0; i<matrix.length; i++)
                System.arraycopy(matrix[i], 0, invMatrix[i], 0, matrix[i].length);

            inverseInPlace(invMatrix);
        }

        return invMatrix;
    }

    public static double[] inverse(double[] x)
    {
        double[] invx = new double[x.length];

        for (int i=0; i<x.length; i++)
            invx[i] = 1.0/(x.length*x[i]);

        return invx;
    }

    public static void inverseInPlace(double[][] matrix)
    {
        int dim = matrix.length;
        int i,j;

        double[][] y;
        double[] d = new double[1];
        double[] col;

        y = new double[dim][dim];

        int[] indices = new int[dim];
        col = new double[dim];

        luDecompose(matrix, dim, indices, d);
        for (j=0;j<dim;j++)
        {
            for (i=0;i<dim;i++)
                col[i] = 0.0;
            col[j] = 1.0;
            luSubstitute(matrix, indices, col);
            for (i=0;i<dim;i++)
                y[i][j] = col[i];
        }

        for (i=0;i<dim;i++)
            System.arraycopy(y[i], 0, matrix[i], 0, dim);
    }

    public static void luDecompose(double[][]a, int n, int[] indx, double[] d)
    {
        double TINYVAL = 1e-20;
        int i,imax,j,k;
        double big,dum,sum,temp;
        double[] vv;
        imax=0;

        vv=new double[n];
        d[0]=1.0;

        for (i=1;i<=n;i++)
        {
            big=0.0;
            for (j=1;j<=n;j++)
                if ((temp=Math.abs(a[i-1][j-1])) > big)
                    big=temp;
            if (big == 0.0)
                System.out.println("Singular matrix in routine ludcmp");
            vv[i-1]=1.0/big;
        }

        for (j=1;j<=n;j++)
        {
            for (i=1;i<j;i++)
            {
                sum=a[i-1][j-1];
                for (k=1;k<i;k++)
                    sum -= a[i-1][k-1]*a[k-1][j-1];
                a[i-1][j-1]=sum;
            }

            big=0.0;

            for (i=j;i<=n;i++)
            {
                sum=a[i-1][j-1];
                for (k=1;k<j;k++)
                    sum -= a[i-1][k-1]*a[k-1][j-1];
                a[i-1][j-1]=sum;
                if ( (dum=vv[i-1]*Math.abs(sum)) >= big)
                {
                    big=dum;
                    imax=i;
                }
            }

            if (j != imax)
            {
                for (k=1;k<=n;k++)
                {
                    dum=a[imax-1][k-1];
                    a[imax-1][k-1]=a[j-1][k-1];
                    a[j-1][k-1]=dum;
                }
                d[0] = -d[0];
                vv[imax-1]=vv[j-1];
            }
            indx[j-1]=imax;
            if (a[j-1][j-1] == 0.0)
                a[j-1][j-1]=TINYVAL;
            if (j != n)
            {
                dum=1.0/(a[j-1][j-1]);
                for (i=j+1;i<=n;i++)
                    a[i-1][j-1] *= dum;
            }
        }
    }

    public static void luSubstitute(double[][] a, int[] indx, double b[])
    {
        int n = a.length;
        int i=0;
        int ii=0;
        int ip,j;
        double sum;

        for (i=1;i<=n;i++)
        {
            ip=indx[i-1];
            sum=b[ip-1];
            b[ip-1]=b[i-1];
            if (ii!=0)
            {
                for (j=ii;j<=i-1;j++)
                    sum -= a[i-1][j-1]*b[j-1];
            }
            else if (sum!=0.0)
                ii=i;
            b[i-1]=sum;
        }
        for (i=n;i>=1;i--)
        {
            sum=b[i-1];
            for (j=i+1;j<=n;j++)
                sum -= a[i-1][j-1]*b[j-1];

            b[i-1]=sum/a[i-1][i-1];
        }
    }
}

