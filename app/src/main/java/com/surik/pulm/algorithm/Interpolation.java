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

/**
 * This class solves interpolation problems in algorithm section
 */
public class Interpolation {

	   public static final double[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

	        if (x.length != y.length) {
	            throw new IllegalArgumentException("X and Y must be the same length");
	        }
	        if (x.length == 1) {
	            throw new IllegalArgumentException("X must contain more than one value");
	        }
	        double[] dx = new double[x.length - 1];
	        double[] dy = new double[x.length - 1];
	        double[] slope = new double[x.length - 1];
	        double[] intercept = new double[x.length - 1];

	        // Calculate the line equation (i.e. slope and intercept) between each point
	        for (int i = 0; i < x.length - 1; i++) {
	            dx[i] = x[i + 1] - x[i];
	            if (dx[i] == 0) {
	                throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
	            }
	            if (dx[i] < 0) {
	                throw new IllegalArgumentException("X must be sorted");
	            }
	            dy[i] = y[i + 1] - y[i];
	            slope[i] = dy[i] / dx[i];
	            intercept[i] = y[i] - x[i] * slope[i];
	        }

	        // Perform the interpolation here
	        double[] yi = new double[xi.length];
	        for (int i = 0; i < xi.length; i++) {
	            if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
	                yi[i] = Double.NaN;
	            }
	            else {
	                int loc = Arrays.binarySearch(x, xi[i]);
	                if (loc < -1) {
	                    loc = -loc - 2;
	                    yi[i] = slope[loc] * xi[i] + intercept[loc];
	                }
	                else {
	                    yi[i] = y[loc];
	                }
	            }
	        }

	        return yi;
	    }
}