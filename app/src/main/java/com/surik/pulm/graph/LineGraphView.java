package com.surik.pulm.graph;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import com.surik.pulm.utils.GraphViewData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
/**
 * Creates Graph using LinearLayout
 */
public class LineGraphView extends GraphView {
	private final Paint paintBackground;
	private boolean drawBackground;
	private int pointColor;
	private float pointStroke;
	private Boolean mIsPolygonDraw = false;

	public LineGraphView(Context context, String title, int pointColor, float pointStroke, Boolean isTime) {
		super(context, title, isTime);		
		paintBackground = new Paint();
		paintBackground.setARGB(255, 20, 40, 60);
		paintBackground.setStrokeWidth(4);
		this.pointColor = pointColor;
		this.pointStroke = pointStroke;
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth,
							float graphheight, float border, double minX, double minY,
								double diffX, double diffY, float horstart) {
		
		// draw background
		double lastEndY = 0;
		double lastEndX = 0;
		if (drawBackground) {
			float startY = graphheight + border;
			for (int i = 0; i < values.length; i++) {
				double valY = values[i].valueY - minY;
				double ratY = valY / diffY;
				double y = graphheight * ratY;

				double valX = values[i].valueX - minX;
				double ratX = valX / diffX;
				double x = graphwidth * ratX;

				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight + 2;

				if (i > 0) {
					// fill space between last and current point
					int numSpace = (int) ((endX - lastEndX) / 3f) + 1;
					for (int xi = 0; xi < numSpace; xi++) {
						float spaceX = (float) (lastEndX + ((endX - lastEndX)
								* xi / (numSpace - 1)));
						float spaceY = (float) (lastEndY + ((endY - lastEndY)
								* xi / (numSpace - 1)));

						// start => bottom edge
						float startX = spaceX;
						// do not draw over the left edge
						if (startX - horstart > 1) {
							canvas.drawLine(startX, startY, spaceX, spaceY, paintBackground);
						}
					}
				}

				lastEndY = endY;
				lastEndX = endX;
			}
		}

		// draw data
		lastEndY = 0;
		lastEndX = 0;
		Path path = null;
		float initialStartX = 0;
		float initialStartY = 0;
		float finalEndX = 0;
		
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].valueY - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;
			

			double valX = values[i].valueX - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY = (float) (border - lastEndY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight;

				Paint tempPaint = new Paint();
				tempPaint.setColor(pointColor);
				tempPaint.setStrokeWidth(pointStroke);
				canvas.drawPoint(startX, startY, tempPaint);
				canvas.drawPoint(endX, endY, tempPaint);

				canvas.drawLine(startX, startY, endX, endY, linePaint);
				
				if (mIsPolygonDraw) {
					if (path == null) {
						path = new Path();
						path.moveTo(startX, startY);
						initialStartX = startX;
						initialStartY = startY;
					} else if (i + 1 == values.length) {
						finalEndX = endX;
					}
					path.lineTo(endX, endY);
				}				
			}
			lastEndY = y;
			lastEndX = x;
		}
		if (mIsPolygonDraw) {
			if (path == null) {
				path = new Path();
			}
			Paint newPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			newPaint.setStrokeWidth(2);
			newPaint.setColor(android.graphics.Color.RED);
			newPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			newPaint.setAntiAlias(true);

			path.lineTo(finalEndX, graphheight / 2);
			path.lineTo(initialStartX, graphheight / 2);
			path.lineTo(initialStartX, initialStartY);
			path.close();
			canvas.drawPath(path, linePaint);
		}
	}
}
