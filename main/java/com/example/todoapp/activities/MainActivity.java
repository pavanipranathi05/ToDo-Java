package com.example.todoapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.adapter.TaskAdapter;
import com.example.todoapp.data.TaskContract;
import com.example.todoapp.data.TaskDbHelper;
import com.example.todoapp.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskItemClickListener {

    private static final int ADD_TASK_REQUEST = 1;
    private static final int EDIT_TASK_REQUEST = 2;

    private TaskDbHelper dbHelper;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Todo List");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, taskList, this);
        recyclerView.setAdapter(taskAdapter);

        dbHelper = new TaskDbHelper(this);

        loadTasks();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });
    }

    private void loadTasks() {
        taskList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DATE));
            String time = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIME));
            int priority = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            boolean hasAlarm = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_HAS_ALARM)) == 1;

            Task task = new Task(id, title, description, date, time, priority, hasAlarm);
            taskList.add(task);
        }

        cursor.close();

        // Sort tasks by date, time, and priority
        Collections.sort(taskList, new Comparator<Task>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            @Override
            public int compare(Task t1, Task t2) {
                try {
                    // First compare by date
                    if (t1.getDate() != null && t2.getDate() != null) {
                        Date date1 = dateFormat.parse(t1.getDate());
                        Date date2 = dateFormat.parse(t2.getDate());

                        int dateComparison = date1.compareTo(date2);
                        if (dateComparison != 0) {
                            return dateComparison;
                        }
                    } else if (t1.getDate() == null) {
                        return 1;
                    } else if (t2.getDate() == null) {
                        return -1;
                    }

                    // Then compare by time if dates are equal
                    if (t1.getTime() != null && t2.getTime() != null) {
                        Date time1 = timeFormat.parse(t1.getTime());
                        Date time2 = timeFormat.parse(t2.getTime());

                        int timeComparison = time1.compareTo(time2);
                        if (timeComparison != 0) {
                            return timeComparison;
                        }
                    } else if (t1.getTime() == null) {
                        return 1;
                    } else if (t2.getTime() == null) {
                        return -1;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Finally, compare by priority (higher priority first)
                return Integer.compare(t2.getPriority(), t1.getPriority());
            }
        });

        taskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadTasks();
        }
    }

    @Override
    public void onEditClick(Task task) {
        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_title", task.getTitle());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_date", task.getDate());
        intent.putExtra("task_time", task.getTime());
        intent.putExtra("task_priority", task.getPriority());
        intent.putExtra("task_has_alarm", task.isHasAlarm());
        startActivityForResult(intent, EDIT_TASK_REQUEST);
    }

    @Override
    public void onDeleteClick(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry._ID + "=?",
                new String[]{String.valueOf(task.getId())}
        );

        // Remove alarm if set
        if (task.isHasAlarm()) {
            // TODO: Cancel alarm using AlarmManager
        }

        loadTasks();
    }
}