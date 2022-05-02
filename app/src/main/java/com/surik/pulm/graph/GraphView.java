package com.surik.pulm.graph;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;
import android.widget.LinearLayout;

import com.surik.pulm.Graph_Types;
import com.surik.pulm.R;
import com.surik.pulm.ValuesOfSettings;
import com.surik.pulm.utils.GraphUtilities;
import com.surik.pulm.utils.GraphViewData;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates abstract Graph using LinearLayout
 */
abstract public class GraphView extends LinearLayout {
    private static final Object object = new Object();
    static final private class GraphViewConfig {
        static final float BORDER = 1 * Resources.getSystem().getDisplayMetrics().density;
    }


    private class GraphViewContentView extends View {
        private float graphwidth;
        private boolean mIsTime = false;

        public GraphViewContentView(Context context, Boolean isTime) {
            super(context);
            mIsTime = isTime;
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			linePaint.setStrokeWidth(0);			
			float border1 = GraphViewConfig.BORDER;
			float height1 = getHeight();
			float graphheight1 = height1 - (2 * border1);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight1);
			}
			
			//printing the vertical values
			int labelsColor1 = Color.rgb(250, 250, 250);
			linePaint.setTextAlign(Align.RIGHT);
			linePaint.setColor(labelsColor1);
			linePaint.setTextSize(getResources().getDimension(R.dimen.gvtextsize));
			float y1 = getResources().getDimension(R.dimen.minmaxscaley);
            float x1 = getResources().getDimension(R.dimen.minmaxscalex);
			String verlabel = "";
			
			try {
                if(verlabels.length>1){
                    if (Double.parseDouble(verlabels[1].replaceAll(",", "")) == Integer.MAX_VALUE) {
                        verlabels[1] = "";
                    }else{
                        verlabel += "min " + verlabels[1];
                    }
                }

				if (Double.parseDouble(verlabels[0].replaceAll(",", "")) == Integer.MIN_VALUE) {
					verlabels[0] = "";
				}else{
					verlabel += "  |  max " + verlabels[0];
				}
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}

			if(ValuesOfSettings.getInstance().getGraph_type() == Graph_Types.ACOUSTIC)
				canvas.drawText(verlabel, x1, y1, linePaint);


			
			linePaint.setTextSize(getResources().getDimension(R.dimen.ghtextsize));
			linePaint.setStrokeWidth(0);
			float border = GraphViewConfig.BORDER;
			float horstart = 0;
			float height = getHeight()- 30;
			float width = getWidth() - 1;
			//width = 1059;
			double maxY = getMaxY();
			double minY = getMinY();
			double diffY = maxY - minY;
			double maxX = getMaxX(false);
			double minX = getMinX(false);
			double diffX = maxX - minX;
			float graphheight = height - (2 * border);
			
			graphwidth = width;
			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical lines
			linePaint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				linePaint.setColor(Color.rgb(22, 22, 22));
				float y = ((graphheight / vers) * i) + border;
				
				canvas.drawLine(horstart, y, width, y, linePaint);
			}

			//check if all horlabel are 0, if so display a 0 1 2 3.. instead.
			Boolean isAllZeros = true;
			for (int i = 0; i < horlabels.length; i++) {
				try {
					if (Double.parseDouble(horlabels[i].replaceAll(",", ".")) != 0) {
						isAllZeros = false;
						break;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
			// horizontal labels + lines
			int hors = horlabels.length - 1;
			int innerLineColor = Color.rgb(42, 42, 42);
			int labelsColor = Color.rgb(0, 0, 0);
			float x;
			double labelValue = 0;
			DecimalFormat formatter = new DecimalFormat("#");
			for (int i = 0; i < horlabels.length; i++) {
				x = ((graphwidth / hors) * i) + horstart;
				
				//printing vertical inner lines
				linePaint.setColor(innerLineColor);
				linePaint.setStyle(Style.STROKE);
                final float[] dashPathEffectParam = new float[]{2, 2};
                linePaint.setPathEffect(new DashPathEffect(dashPathEffectParam, 0));
				canvas.drawLine(x, height - border, x, border, linePaint);
				linePaint.setPathEffect(null);
				
				//deals with aligning the text
				linePaint.setTextAlign(Align.CENTER);
				if (i==horlabels.length-1)
					linePaint.setTextAlign(Align.RIGHT);
				if (i==0)
					linePaint.setTextAlign(Align.LEFT);
				
				
				linePaint.setColor(labelsColor);
				if (isAllZeros) {
					canvas.drawText(String.valueOf(i), x, height - border/3, linePaint);
				} else {
					if (!mIsTime) {
						labelValue = Double.parseDouble(horlabels[i].replaceAll(",", "."));
						horlabels[i] = formatter.format(labelValue).toString();
//						horlabels[i] = formatter.format(labelValue + 1).toString();
						canvas.drawText(horlabels[i], x, height - border/3, linePaint);
					} else {
						canvas.drawText(
								GraphUtilities.convertLongToTime(horlabels[i]),
								x, height - border/3, linePaint);
					}
				}
			}

			linePaint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, linePaint);

			if (maxY != minY) {
				linePaint.setStrokeCap(Paint.Cap.ROUND);
				linePaint.setStrokeWidth(3);
				for (int i=0; i<graphSeries.size(); i++) {
					linePaint.setColor(graphSeries.get(i).lineColor);
					linePaint.setStrokeWidth(graphSeries.get(i).lineStroke);
					drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart);
				}

				if (showLegend) drawLegend(canvas, height, width);
			}
//			//TODO Tigran Papyan
//			invalidate();
		}
}


	/**
	 * a graph series
	 */
	static public class GraphViewSeries {
		private String description;
		private int lineColor;
		private int lineStroke;
		private GraphViewData firstData;
		private GraphViewData lastData;
        private GraphViewData[] values;

		public GraphViewSeries(GraphViewData[] values) {
			description = null;
            lineColor = 0xff0077cc; // blue version
            this.values = Arrays.copyOf(values, values.length);
            firstData = values[0];
			lastData = values[values.length - 1];
        }

        public void setLineStroke(int stroke) {
            this.lineStroke = stroke;
        }

        public void setLineColor(int color) {
            this.lineColor = color;
        }

        public GraphViewSeries(String description, Integer lineColor, GraphViewData[] values) {
            super();
            this.description = description;
            if (lineColor == null) {
                lineColor = 0xff0077cc; // blue version
            }
            this.lineColor = lineColor;
            this.values = Arrays.copyOf(values, values.length);
        }
    }

    public enum LegendAlign {
        TOP, MIDDLE, BOTTOM
    }

    private class VerLabelsView extends View {

        public VerLabelsView(Context context) {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 10));
        }

        @Override
		protected void onDraw(Canvas canvas) {
			// normal
			linePaint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}
			
			//printing the vertical values
			int labelsColor = Color.rgb(250, 250, 250);
			linePaint.setTextAlign(Align.RIGHT);
			linePaint.setColor(labelsColor);
			float y = 0;
			
			String verlabel = "";
			
			try {
                if(verlabels.length>1){
                    if (Double.parseDouble(verlabels[1].replaceAll(",", "")) == Integer.MAX_VALUE) {
                        verlabels[1] = "";
                    }else{
                        verlabel += "min " + verlabels[1];
                    }
                }

				if (Double.parseDouble(verlabels[0].replaceAll(",", "")) == Integer.MIN_VALUE) {
					verlabels[0] = "";
				}else{
					verlabel += "  |  max " + verlabels[0];
				}
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
			
			canvas.drawText(verlabel, 900, y, linePaint);
		}
	}

	protected final Paint linePaint;
	private String[] horlabels;
	private String[] verlabels;
	private String title;
	private double viewportStart;
	private double viewportSize;
	private final View viewVerLabels;
	private NumberFormat numberformatter;
	private final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private float legendWidth = 120;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private boolean manualYAxis;
	private double manualMaxYValue;
	private double manualMinYValue;

    /**
     * constructor
     *
     * @param context
     * @param title   [optional]
     */
	public GraphView(Context context, String title, Boolean isTime) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (title == null)
			title = "";
		else
			this.title = title;

		linePaint = new Paint();
		graphSeries = new ArrayList<GraphViewSeries>();

		viewVerLabels = new VerLabelsView(context);
		addView(viewVerLabels);
		addView(new GraphViewContentView(context, isTime), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
	}

    /**
     * @param idxSeries
     * @return array of GraphViewData
     */
	private GraphViewData[] _values(int idxSeries) {
		GraphViewData[] values = graphSeries.get(idxSeries).values;
		if (viewportStart == 0 && viewportSize == 0) {
			// all data
			return values;
		} else {
			// viewport
			List<GraphViewData> listData = new ArrayList<GraphViewData>();
			for (int i=0; i<values.length; i++) {
				if (values[i].valueX >= viewportStart) {
					if (values[i].valueX > viewportStart+viewportSize) {
						listData.add(values[i]); // one more for nice scrolling
						break;
					} else {
						listData.add(values[i]);
					}
				} else {
					if (listData.isEmpty()) {
						listData.add(values[i]);
					}
					listData.set(0, values[i]); // one before, for nice scrolling
				}
			}
			return listData.toArray(new GraphViewData[listData.size()]);
		}
	}

	public void addSeries(GraphViewSeries series) {
		graphSeries.add(series);
	}

	protected void drawLegend(Canvas canvas, float height, float width) {
		int shapeSize = 15;

		// rect
		linePaint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize+5)*graphSeries.size() +5;
		float lLeft = width-legendWidth - 10;
		float lTop;
		switch (legendAlign) {
		case TOP:
			lTop = 10;
			break;
		case MIDDLE:
			lTop = height/2 - legendHeight/2;
			break;
		default:
			lTop = height - GraphViewConfig.BORDER - legendHeight -10;
		}
		float lRight = lLeft+legendWidth;
		float lBottom = lTop+legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, linePaint);
		
		for (int i=0; i<graphSeries.size(); i++) {
			linePaint.setColor(graphSeries.get(i).lineColor);
			
			//added by sion
			linePaint.setStrokeWidth(graphSeries.get(i).lineStroke);
			
			canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), linePaint);
			if (graphSeries.get(i).description != null) {
				linePaint.setColor(Color.WHITE);
				linePaint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), linePaint);
			}
		}
	}

	abstract public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart);

	/**
	 * formats the label
	 * can be overwritten
	 * @param value x and y values
	 * @return value to display
	 */
	protected String formatLabel(double value) {
		if (numberformatter == null) {
			
			numberformatter = NumberFormat.getNumberInstance();
			double highestvalue = getMaxY();
			double lowestvalue = getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter.setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter.setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter.setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter.setMaximumFractionDigits(1);
			} else {
				numberformatter.setMaximumFractionDigits(0);
			}
		}
		
		return numberformatter.format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = 3;//(int) (graphwidth/GraphViewConfig.VERTICAL_LABEL_WIDTH);
		String[] labels = new String[numLabels+1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i=0; i<=numLabels; i++) {
			labels[i] = formatLabel(min + ((max-min)*i/numLabels));
		}
		return labels;
	}

    /**
     * generates vertical labels
     *
     * @param graphheight
     * @return
     */
	private String[] generateVerlabels(float graphheight) {
 		synchronized(object){
			int numLabels = 1;//(int) (graphheight/GraphViewConfig.HORIZONTAL_LABEL_HEIGHT);
			String[] labels = new String[numLabels+1];
			double min = getMinY();
			double max = getMaxY();
			for (int i=0; i<=numLabels; i++) {
				labels[numLabels-i] = formatLabel(min + ((max-min)*i/numLabels));
			}
			return labels;
		}

    }

	private double getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart+viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X value
			double highest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				highest = values[values.length-1].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					highest = Math.max(highest, values[values.length-1].valueX);
				}
			}
			return highest;
		}
	}

    /**
     * @return max y value
     */
    private double getMaxY() {
		double largest;
		if (manualYAxis) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewData[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].valueY > largest)
						largest = values[ii].valueY;
			}
		}
		
		return largest;
	}


	private double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X value
			double lowest = 0;
			if (graphSeries.size() > 0)
			{
				GraphViewData[] values = graphSeries.get(0).values;
				lowest = values[0].valueX;
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					lowest = Math.min(lowest, values[0].valueX);
				}
			}
			return lowest;
		}
	}

    /**
     * @return min y value
     */
    private double getMinY() {
        double smallest;
        if (manualYAxis) {
            smallest = manualMinYValue;
        } else {
            smallest = Integer.MAX_VALUE;
            for (int i = 0; i < graphSeries.size(); i++) {
                GraphViewData[] values = _values(i);
                for (int ii = 0; ii < values.length; ii++)
                    if (values[ii].valueY < smallest)
                        smallest = values[ii].valueY;
            }
        }
        return smallest;
    }

    public void setViewPort(double start, double size) {
        viewportStart = start;
        viewportSize = size;
    }
}