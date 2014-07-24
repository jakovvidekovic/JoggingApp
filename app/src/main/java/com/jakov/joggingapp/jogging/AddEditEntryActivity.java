package com.jakov.joggingapp.jogging;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Run;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class AddEditEntryActivity extends ActionBarActivity {
    TextView tvEntryDate, tvEntryTime;
    EditText etDistance;

    private boolean edit = false;

    String runId;
    Run mRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        edit = getIntent().hasExtra(Run.KEY_ARGS);
        if (edit) {
            findViewById(R.id.btnEntryDelete).setVisibility(View.VISIBLE);
            runId = getIntent().getExtras().getString(Run.KEY_ARGS);
            ParseQuery<Run> query = new ParseQuery<Run>(Run.class);
            query.fromLocalDatastore();
            query.getInBackground(runId, new GetCallback<Run>() {
                @Override
                public void done(Run run, ParseException e) {
                    if (e == null) {
                        mRun = run;
                        setInitValues();
                    }
                }
            });

        } else {
            mRun = new Run();
            mRun.setDeleted(false);
            mRun.setUser(ParseUser.getCurrentUser());
            mRun.setTime(new Date(0));
            mRun.setDate(new Date());
        }
        init();
    }

    private void setInitValues() {

        tvEntryDate.setText(Const.sdfDate.format(mRun.getDate()));
        tvEntryTime.setText(Const.sdfTime.format(mRun.getTime()));
        etDistance.setText("" + mRun.getDistance());
    }

    private void init() {
        tvEntryDate = (TextView) findViewById(R.id.tvEntryDate);
        tvEntryTime = (TextView) findViewById(R.id.tvEntryTime);
        etDistance = (EditText) findViewById(R.id.etEntryDistance);
    }


    public void clickTime(View v) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(mRun.getTime());
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.clear();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                tvEntryTime.setText(Const.sdfTime.format(c.getTime()));
                mRun.setTime(c.getTime());
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);

        timePicker.show();
    }

    public void clickCalendar(View v) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(mRun.getDate());
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvEntryDate.setText(Const.sdfDate.format(cal.getTime()));
                mRun.setDate(cal.getTime());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    public void clickSave(View v) {
        if (TextUtils.isEmpty(tvEntryTime.getText())||mRun.getTime().getTime()<1000*60) {
            Crouton.makeText(this, "Please select time", Style.ALERT).show();
            return;
        }

        if (TextUtils.isEmpty(etDistance.getText())) {
            Crouton.makeText(this, "Please enter distance", Style.ALERT).show();
            return;
        }
        mRun.setDistance(Double.parseDouble(etDistance.getText().toString()));
        if (edit) {
            saveEdit();
        } else {
            saveNew();
        }

    }

    private void saveNew() {
        mRun.setUpdatedAt(new Date());
        mRun.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(AddEditEntryActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddEditEntryActivity.this, "Problem occurred!", Toast.LENGTH_SHORT).show();
                }
                NavUtils.navigateUpFromSameTask(AddEditEntryActivity.this);
            }
        });


    }

    private void saveEdit() {

        mRun.setUpdatedAt(new Date());
        mRun.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(AddEditEntryActivity.this, "Successfully updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddEditEntryActivity.this, "Problem occurred!", Toast.LENGTH_SHORT).show();
                }
                NavUtils.navigateUpFromSameTask(AddEditEntryActivity.this);
            }
        });


    }

    public void clickCancel(View v) {
        NavUtils.navigateUpFromSameTask(AddEditEntryActivity.this);
    }

    public void clickDelete(View v) {
        mRun.setUpdatedAt(new Date());
        mRun.setDeleted(true);
        mRun.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(AddEditEntryActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddEditEntryActivity.this, "Deleting error!", Toast.LENGTH_SHORT).show();
                }
                NavUtils.navigateUpFromSameTask(AddEditEntryActivity.this);
            }
        });

    }


}
