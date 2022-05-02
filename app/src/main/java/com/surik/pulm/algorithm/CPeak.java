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
 * calculating peaks from algorithm
 */
public class CPeak {

	public double height;
	public double weight;
	public int index;

	public CPeak(double h, int i) {
		height = h;
		index = i;
	}

	public CPeak(double h, double w, int i) {
		height = h;
		weight = w;
		index = i;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public int getIndex() {
		return index;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
