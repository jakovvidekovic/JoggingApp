package com.jakov.joggingapp.reports;

import android.os.Bundle;

import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.extra.Run;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AverageSpeedsActivity extends BaseChartActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ParseQuery<Run> query = ParseQuery.getQuery(Run.class);
        query.fromLocalDatastore();
        query.addAscendingOrder(Run.C_DATE);
        query.findInBackground(new FindCallback<Run>() {
            @Override
            public void done(List<Run> runs, ParseException e) {
                if (e == null) {
                    chart.addView(getChart(runs));
                }
            }
        });
    }

    private GraphicalView getChart(List<Run> runs) {
        if (runs.size() > 0) {
            double maxAverageSpeed=0;
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            TimeSeries series = new TimeSeries("Average Speed");
            Date firstDate = runs.get(0).getDate();
            Calendar first = Calendar.getInstance();
            first.clear();
            first.setTime(firstDate);
            first.clear(Calendar.HOUR_OF_DAY);
            Date lastDate = runs.get(runs.size() - 1).getDate();
            Calendar last = Calendar.getInstance();
            last.clear();
            last.setTime(lastDate);
            Calendar next = Calendar.getInstance();
            next.clear();
            next = (Calendar) first.clone();
            next.add(Calendar.DAY_OF_MONTH, 1);
            XYMultipleSeriesRenderer renderer = getRenderer(first, last,"Average Speeds per day","Speed [km/h]");
            while (last.after(first) || last.equals(first)) {
                double distance = 0;
                long timeMills = 0;
                for (Run run : runs) {
                    if (run.getDate().getTime() >= first.getTime().getTime() && run.getDate().getTime() < next.getTime().getTime()) {
                        distance += run.getDistance();
                        timeMills += run.getTime().getTime();
                    }
                }

                double averageSpeed = Const.getAverageSpeed(distance, new Date(timeMills));
                if (averageSpeed > 0) {
                    series.add(first.getTime(), averageSpeed);
                    if(averageSpeed>maxAverageSpeed){
                        maxAverageSpeed=averageSpeed;
                    }
                }
                first.add(Calendar.DAY_OF_MONTH, 1);
                next.add(Calendar.DAY_OF_MONTH, 1);
            }
            dataset.addSeries(series);
            tvBest.setText("Best Average Speed: " + String.format(Locale.getDefault()," %1$.2f",maxAverageSpeed) + "km/h");
            renderer.setYAxisMax(maxAverageSpeed+5);
            return ChartFactory.getTimeChartView(this, dataset, renderer, "dd.MM.yy.");
        }
        return null;
    }





}
