package com.jakov.joggingapp.reports;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jakov.joggingapp.R;

import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.NumberFormat;
import java.util.Calendar;

public class BaseChartActivity extends ActionBarActivity {
    protected FrameLayout chart;
    protected TextView tvBest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = (FrameLayout) findViewById(R.id.chart);
        tvBest = (TextView) findViewById(R.id.tvBest);
    }

    protected XYMultipleSeriesRenderer getRenderer(Calendar first, Calendar last,String title,String yAxis) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(25);
        renderer.setLabelsTextSize(20);
        renderer.setLegendTextSize(20);
        renderer.setPointSize(5f);
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        r.setPointStyle(PointStyle.POINT);
        r.setLineWidth(3);
        renderer.addSeriesRenderer(r);
        renderer.setChartTitle(title);
        renderer.setXTitle("Date");
        renderer.setYTitle(yAxis);
        renderer.setXAxisMin(first.getTimeInMillis());
        renderer.setXAxisMax(last.getTimeInMillis());
        renderer.setYAxisMin(-5);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.DKGRAY);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setApplyBackgroundColor(true);
        renderer.setPanEnabled(true, false);
        for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
            SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
            seriesRenderer.setDisplayChartValues(true);
            seriesRenderer.setChartValuesTextSize(15);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            seriesRenderer.setChartValuesFormat(nf);
        }
        return renderer;
    }
}
