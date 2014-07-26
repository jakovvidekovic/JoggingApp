package com.jakov.joggingapp.jogging;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Run;
import com.jakov.joggingapp.tracking.TrackingActivity;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class JoggingFragment extends Fragment implements View.OnClickListener {

    Button btnAddEntry, btnStartTracking;
    ImageButton btnDateFrom, btnDateTo;
    TextView tvDateTo, tvDateFrom;

    ListView listViewEntry;

    List<Run> mRuns;
    EntryListAdapter adapter;

    public JoggingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_jogging, container, false);
        btnAddEntry = (Button) v.findViewById(R.id.btnAddEntry);
        btnStartTracking = (Button) v.findViewById(R.id.btnStartTracking);
        btnDateFrom = (ImageButton) v.findViewById(R.id.btnDateFrom);
        btnDateTo = (ImageButton) v.findViewById(R.id.btnDateTo);
        tvDateFrom = (TextView) v.findViewById(R.id.tvDateFrom);
        tvDateTo = (TextView) v.findViewById(R.id.tvDateTo);

        btnAddEntry.setOnClickListener(this);
        btnStartTracking.setOnClickListener(this);
        btnDateFrom.setOnClickListener(this);
        btnDateTo.setOnClickListener(this);

        listViewEntry = (ListView) v.findViewById(R.id.listViewEntries);
        mRuns = new ArrayList<Run>();
        adapter = new EntryListAdapter();
        adapter.setData(mRuns);
        listViewEntry.setAdapter(adapter);
        setListAdapter();

        return v;
    }

    private void setListAdapter() {
        final ParseQuery<Run> query = new ParseQuery<Run>(Run.class);
        query.fromLocalDatastore();
        query.whereNotEqualTo(Run.C_DELETED, true);
        query.addDescendingOrder(Run.C_DATE);
        query.findInBackground(new FindCallback<Run>() {
            @Override
            public void done(List<Run> runs, ParseException e) {
                if (e == null) {
                    mRuns = runs;
                    adapter.setData(mRuns);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("offline", e.getMessage());
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddEntry:
                startAddEntryActivity();
                break;
            case R.id.btnStartTracking:
                startTrackingActivity();
                break;
            case R.id.btnDateFrom:
                showDatePicke(R.id.btnDateFrom);
                break;
            case R.id.btnDateTo:
                showDatePicke(R.id.btnDateTo);
                break;
        }
    }

    private void startTrackingActivity() {
        Intent i = new Intent(getActivity(), TrackingActivity.class);
        startActivity(i);
    }

    private void startAddEntryActivity() {
        Intent i = new Intent(getActivity(), AddEditEntryActivity.class);
        startActivity(i);
    }


    //region Filtering list by date
    private Date dateFrom, dateTo;

    private void showDatePicke(final int id) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (id == R.id.btnDateFrom) {
                    dateFrom = cal.getTime();
                    tvDateFrom.setText("FROM:\n" + Const.sdfDate.format(dateFrom));
                }
                if (id == R.id.btnDateTo) {
                    dateTo = cal.getTime();
                    tvDateTo.setText("TO:\n" + Const.sdfDate.format(dateTo));
                }
                filterResults();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void filterResults() {
        List<Run> list = new ArrayList<Run>(mRuns);
        if (dateFrom != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDate().before(dateFrom)) {
                    list.remove(i);
                    i--;
                }
            }
        }
        if (dateTo != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDate().after(dateTo)) {
                    list.remove(i);
                    i--;
                }
            }
        }

        adapter.setData(list);
        adapter.notifyDataSetChanged();
    }
    //endregion

    //region check if online update is needed
    @Override
    public void onResume() {
        super.onResume();
        updateOnlineIfNeed();
    }

    private void updateOnlineIfNeed() {
        final SharedPreferences sp = getActivity().getSharedPreferences(Const.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        final Date lastUpdate = new Date(sp.getLong(Const.PREFS_UPDATE_KEY, 0));
        ParseQuery<Run> offline = ParseQuery.getQuery(Run.class);
        offline.fromLocalDatastore();
        offline.whereGreaterThanOrEqualTo(Run.C_UPDATED_AT, lastUpdate);
        offline.findInBackground(new FindCallback<Run>() {
            @Override
            public void done(List<Run> runs, ParseException e) {
                if (e == null) {
                    for (final Run run : runs) {
                        if (run.isDeleted()) {
                            run.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        run.unpinInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    sp.edit().putLong(Const.PREFS_UPDATE_KEY, new Date().getTime()).commit();
                                                } else
                                                    Log.e("sync", "unpin fail " + e.getMessage());
                                            }
                                        });
                                    } else
                                        Log.e("sync", "delete fail " + e.getMessage());
                                }
                            });
                        } else {
                            run.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        sp.edit().putLong(Const.PREFS_UPDATE_KEY, new Date().getTime()).commit();
                                    } else
                                        Log.e("sync", "unpdate online fail " + e.getMessage());
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    //endregion

    private class EntryListAdapter extends BaseAdapter {
        List<Run> list;

        public void setData(List<Run> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.layout_entry_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Run run = list.get(position);
            holder.date.setText("Date: " + Const.sdfDate.format(run.getDate()));
            holder.time.setText("Time: " + Const.sdfTime.format(run.getTime()) + " h");
            holder.distance.setText("Distance: " + String.format(Locale.getDefault(), "%1$.2f", run.getDistance()) + " km");
            double avSpeed = Const.getAverageSpeed(run.getDistance(), run.getTime());
            holder.speed.setText("Av. Speed: " + String.format(Locale.getDefault(), "%1$.2f", avSpeed) + " km/h");

            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), AddEditEntryActivity.class);
                    i.putExtra(Run.KEY_ARGS, run.getObjectId());
                    i.putExtra(Run.C_DATE, run.getDate());
                    i.putExtra(Run.C_TIME, run.getTime());
                    i.putExtra(Run.C_DISTANCE, run.getDistance());
                    startActivity(i);
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    run.setUpdatedAt(new Date());
                    run.setDeleted(true);
                    run.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Crouton.makeText(getActivity(),"Deleted", Style.INFO).show();
                            }
                        }
                    });
                    mRuns.remove(run);
                    notifyDataSetChanged();
                }
            });
            if (run.hasCoordinates()) {
                holder.edit.setVisibility(View.GONE);
                holder.map.setVisibility(View.VISIBLE);
                holder.map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), DisplayRunActivity.class);
                        i.putExtra(Run.KEY_ARGS, run.getObjectId());
                        startActivity(i);
                    }
                });
            }else{
                holder.edit.setVisibility(View.VISIBLE);
                holder.map.setVisibility(View.GONE);
            }
            return convertView;
        }

        class ViewHolder {
            ImageButton edit, delete, map;
            TextView date, time, speed, distance;

            public ViewHolder(View v) {
                map = (ImageButton) v.findViewById(R.id.ibMap);
                edit = (ImageButton) v.findViewById(R.id.ibtnEdit);
                delete = (ImageButton) v.findViewById(R.id.ibtnDelete);
                date = (TextView) v.findViewById(R.id.tvDate);
                time = (TextView) v.findViewById(R.id.tvTime);
                speed = (TextView) v.findViewById(R.id.tvAvSpeed);
                distance = (TextView) v.findViewById(R.id.tvDistance);
            }
        }
    }
}
