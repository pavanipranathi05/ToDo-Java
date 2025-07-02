package com.example.todoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.todoapp.data.TaskContract;
import com.example.todoapp.data.TaskDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDescription;
    private Button btnDate, btnTime, btnSave;
    private RadioGroup rgPriority;
    private Switch switchAlarm;

    private TaskDbHelper dbHelper;
    private int taskId;
    private String selectedDate;
    private String selectedTime;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat displayTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Edit Task");

        // Initialize date and time formats
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        displayTimeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Initialize views
        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDescription = findViewById(R.id.et_task_description);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        btnSave = findViewById(R.id.btn_save);
        rgPriority = findViewById(R.id.rg_priority);
        switchAlarm = findViewById(R.id.switch_alarm);

        dbHelper = new TaskDbHelper(this);

        // Get task data from intent
        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTaskTitle.setText(getIntent().getStringExtra("task_title"));
        etTaskDescription.setText(getIntent().getStringExtra("task_description"));

        selectedDate = getIntent().getStringExtra("task_date");
        selectedTime = getIntent().getStringExtra("task_time");

        if (selectedDate != null) {
            try {
                calendar.setTime(dateFormat.parse(selectedDate));
                btnDate.setText(displayDateFormat.format(calendar.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (selectedTime != null) {
            try {
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(timeFormat.parse(selectedTime));
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                btnTime.setText(displayTimeFormat.format(calendar.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int priority = getIntent().getIntExtra("task_priority", 1);
        if (priority == 3) {
            ((RadioButton) findViewById(R.id.rb_high)).setChecked(true);
        } else if (priority == 2) {
            ((RadioButton) findViewById(R.id.rb_medium)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rb_low)).setChecked(true);
        }

        switchAlarm.setChecked(getIntent().getBooleanExtra("task_has_alarm", false));

        // Set up date picker
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Set up time picker
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Set up save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = dateFormat.format(calendar.getTime());
                        btnDate.setText(displayDateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        selectedTime = timeFormat.format(calendar.getTime());
                        btnTime.setText(displayTimeFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError("Title is required");
            etTaskTitle.requestFocus();
            return;
        }

        int selectedPriorityId = rgPriority.getCheckedRadioButtonId();
        int priority;
        if (selectedPriorityId == R.id.rb_high) {
            priority = 3;
        } else if (selectedPriorityId == R.id.rb_medium) {
            priority = 2;
        } else {
            priority = 1;
        }

        boolean hasAlarm = switchAlarm.isChecked();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TITLE, title);
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, description);
        values.put(TaskContract.TaskEntry.COLUMN_DATE, selectedDate);
        values.put(TaskContract.TaskEntry.COLUMN_TIME, selectedTime);
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, priority);
        values.put(TaskContract.TaskEntry.COLUMN_HAS_ALARM, hasAlarm ? 1 : 0);

        int rowsAffected = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                TaskContract.TaskEntry._ID + "=?",
                new String[]{String.valueOf(taskId)}
        );

        if (rowsAffected > 0) {
            if (hasAlarm && selectedDate != null && selectedTime != null) {
                // TODO: Update alarm using AlarmManager
                // This would typically involve creating a service or broadcast receiver
                // to handle the alarm when it triggers
            }

            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}