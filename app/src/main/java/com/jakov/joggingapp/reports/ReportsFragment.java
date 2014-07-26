package com.jakov.joggingapp.reports;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.extra.Run;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ReportsFragment extends Fragment implements View.OnClickListener {
    Button btnAverageSpeeds, btnAverageDistances;
    TextView tvAverageSpeed, tvAverageDistance, tvNumberRuns;
    Spinner spnrWeek;

    ArrayList<Holder> list;

    public ReportsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reports, container, false);
        btnAverageSpeeds = (Button) v.findViewById(R.id.btnAverageSpeeds);
        btnAverageSpeeds.setOnClickListener(this);
        btnAverageDistances = (Button) v.findViewById(R.id.btnAverageDistances);
        btnAverageDistances.setOnClickListener(this);
        tvAverageSpeed = (TextView) v.findViewById(R.id.tvAverageSpeed);
        tvAverageDistance = (TextView) v.findViewById(R.id.tvAverageDistance);
        tvNumberRuns = (TextView) v.findViewById(R.id.tvNumberRuns);
        spnrWeek = (Spinner) v.findViewById(R.id.spnrWeek);
        return v;
    }

    private void setup() {
        list = new ArrayList<Holder>();
        ParseQuery<Run> query = ParseQuery.getQuery(Run.class);
        query.fromLocalDatastore();
        query.addAscendingOrder(Run.C_DATE);
        query.findInBackground(new FindCallback<Run>() {
            @Override
            public void done(List<Run> runs, ParseException e) {
                if (e == null) {
                    if (runs.size() > 0) {
                        Date firstDate = runs.get(0).getDate();
                        Calendar first = Calendar.getInstance();
                        first.clear();
                        first.setTime(firstDate);
                        first.setFirstDayOfWeek(Calendar.MONDAY);
                        first.clear(Calendar.HOUR_OF_DAY);
                        Date lastDate = runs.get(runs.size() - 1).getDate();
                        Calendar last = Calendar.getInstance();
                        last.clear();
                        last.setTime(lastDate);
                        last.setFirstDayOfWeek(Calendar.MONDAY);
                        last.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        Calendar next = Calendar.getInstance();
                        next.clear();
                        next = (Calendar) last.clone();
                        next.add(Calendar.WEEK_OF_YEAR, -1);
                        while (last.after(first) || last.equals(first)) {
                            double distance = 0;
                            long timeMills = 0;
                            int numOfRuns=0;
                            for (Run run : runs) {
                                if (run.getDate().getTime() <= last.getTime().getTime() && run.getDate().getTime() > next.getTime().getTime()) {
                                    distance += run.getDistance();
                                    timeMills += run.getTime().getTime();numOfRuns++;
                                }
                            }

                            double averageSpeed = Const.getAverageSpeed(distance, new Date(timeMills));
                            if(numOfRuns>0){
                                String week=Const.sdfDate.format(next.getTime())+"-"+Const.sdfDate.format(last.getTime());
                                list.add(new Holder(distance,averageSpeed,numOfRuns,week));
                            }
                            last.add(Calendar.WEEK_OF_YEAR, -1);
                            next.add(Calendar.WEEK_OF_YEAR, -1);
                        }
                        setupSpinner();
                    }
                }
            }
        });

    }

    private void setupSpinner() {
        ArrayList<String> stringList = new ArrayList<String>();
        for (Holder h : list) {
            stringList.add(h.week);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, stringList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrWeek.setAdapter(adapter);
        spnrWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setValues(list.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private class Holder {
        double distance, averageSpeed;
        int numOfRuns;
        String week;

        Holder(double distance, double averageSpeed, int numOfRuns, String week) {
            this.distance = distance;
            this.averageSpeed = averageSpeed;
            this.numOfRuns = numOfRuns;
            this.week = week;
        }
    }

    private void setValues(Holder h) {
        tvAverageDistance.setText("Distance: " + String.format(Locale.getDefault(), "%1$.2f", h.distance) + " km");
        tvAverageSpeed.setText("Average Speed: " + String.format(Locale.getDefault(), "%1$.2f", h.averageSpeed) + " km/h");
        tvNumberRuns.setText("Number Of Runs: " + h.numOfRuns);
    }

    @Override
    public void onResume() {
        super.onResume();
        setup();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAverageDistances:
                averageDistances();
                break;
            case R.id.btnAverageSpeeds:
                averageSpeeds();
                break;
        }
    }

    private void averageSpeeds() {
        Intent i = new Intent(getActivity(), AverageSpeedsActivity.class);
        startActivity(i);
    }

    private void averageDistances() {
        Intent i= new Intent(getActivity(),DistancesActivity.class);
        startActivity(i);
    }
}
