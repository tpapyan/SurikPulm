package com.surik.pulm.utils;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */

import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;

import com.surik.pulm.graph.GraphView;
import com.surik.pulm.graph.LineGraphView;
import com.surik.pulm.graph.GraphView.GraphViewSeries;

import android.content.Context;
import android.graphics.PointF;

/**
 * Utilities for creating graphs
 */
public class GraphUtilities {

	public static GraphView generateGraph(Context context,
			GraphViewSeries graphData,
			int graphDataColor,
		    int graphDataStroke,
			int pointColor,
			float pointStroke,
		 	float scaleX,
		 	double startOfGraphView,
		 	double sizeOfGraphXLine,
		 	String graphHeadline,
		 	Boolean isTime
	)
	{
		GraphView graphView = new LineGraphView(context, graphHeadline, pointColor, pointStroke, isTime);
		if (graphData != null)
		{
			graphData.setLineColor(graphDataColor);
			graphData.setLineStroke(graphDataStroke);
			graphView.addSeries(graphData);
		}
		graphView.setViewPort(startOfGraphView, sizeOfGraphXLine);
		graphView.setScaleX(scaleX);
		
		return graphView;
	}

	private static GraphViewData convertPointFToGraphData(PointF pointf)
	{
		if (pointf == null)
			return null;
		
		return new GraphViewData(pointf.x, pointf.y);
	}

	public static GraphViewSeries convertPointFArrayToGraphDataSeries(PointF[] pointsF)
	{
		if (pointsF == null)
			return null;
		
		GraphViewData[] graphViewDatas = new GraphViewData[pointsF.length];
		int arrayCounter = 0;
		
		for (PointF pointf : pointsF)
			graphViewDatas[arrayCounter++] = GraphUtilities.convertPointFToGraphData(pointf);
		
		return new GraphViewSeries(graphViewDatas);
	}

	public static float pointFArrayXMaxValue(PointF[] points)
	{
		float maxValue = Float.MIN_VALUE;
		for (PointF pointF : points)
		{
			if (pointF.x > maxValue)
				maxValue = pointF.x;
		}
		return maxValue;
	}

	public static String convertLongToTime(String time)
	{
		time = time.replaceAll(",", "");
		
		int loc = time.indexOf('.');
		if (loc > -1) {
			time = time.substring(0, loc);
		}

		Calendar cal = Calendar.getInstance();
		long timeL = Long.parseLong(time);
		Date date = new Date(timeL);
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		return (String.format(Locale.US, "%02d", hour) + ":" + String.format(Locale.US, "%02d", minute));
	}

	public static float pointFArrayXMinValue(PointF[] points)
	{
		float minValue = Float.MAX_VALUE;
		for (PointF pointF : points)
		{
			if (pointF.x < minValue)
				minValue = pointF.x;
		}
		return minValue;
	}
}
