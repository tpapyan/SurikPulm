package com.surik.pulm.utils;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * one data set for a graph series
 */
public class GraphViewData {
	public final double valueX;
	public final double valueY;

	/**
	 * GraphViewData constructor
	 * @param valueX
	 * @param valueY
     */
	public GraphViewData(double valueX, double valueY) {
		super();
		this.valueX = valueX;
		this.valueY = valueY;
	}
}