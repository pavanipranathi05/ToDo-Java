package com.example.todoapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private TaskItemClickListener listener;

    private SimpleDateFormat dateFormatInput;
    private SimpleDateFormat timeFormatInput;
    private SimpleDateFormat dateFormatOutput;
    private SimpleDateFormat timeFormatOutput;

    public interface TaskItemClickListener {
        void onEditClick(Task task);
        void onDeleteClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, TaskItemClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;

        dateFormatInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormatInput = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormatOutput = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        timeFormatOutput = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getDescription());

        // Format date and time
        StringBuilder dateTimeBuilder = new StringBuilder();
        if (task.getDate() != null) {
            try {
                Date date = dateFormatInput.parse(task.getDate());
                dateTimeBuilder.append(dateFormatOutput.format(date));
            } catch (ParseException e) {
                dateTimeBuilder.append(task.getDate());
            }
        }

        if (task.getTime() != null) {
            try {
                Date time = timeFormatInput.parse(task.getTime());
                if (dateTimeBuilder.length() > 0) {
                    dateTimeBuilder.append(" - ");
                }
                dateTimeBuilder.append(timeFormatOutput.format(time));
            } catch (ParseException e) {
                if (dateTimeBuilder.length() > 0) {
                    dateTimeBuilder.append(" - ");
                }
                dateTimeBuilder.append(task.getTime());
            }
        }

        if (dateTimeBuilder.length() > 0) {
            holder.tvDateTime.setText(dateTimeBuilder.toString());
            holder.tvDateTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvDateTime.setVisibility(View.GONE);
        }

        // Set priority text and background
        String priorityText;
        int priorityColor;
        switch (task.getPriority()) {
            case 3:
                priorityText = "High";
                priorityColor = R.color.colorPriorityHigh;
                break;
            case 2:
                priorityText = "Medium";
                priorityColor = R.color.colorPriorityMedium;
                break;
            default:
                priorityText = "Low";
                priorityColor = R.color.colorPriorityLow;
                break;
        }

        holder.tvPriority.setText(priorityText);
        holder.tvPriority.setBackgroundTintList(ContextCompat.getColorStateList(context, priorityColor));

        // Show alarm icon if task has alarm
        holder.ivAlarm.setVisibility(task.isHasAlarm() ? View.VISIBLE : View.GONE);

        // Set click listeners
        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditClick(task);
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskDescription, tvDateTime, tvPriority;
        ImageView ivEdit, ivDelete, ivAlarm;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            ivAlarm = itemView.findViewById(R.id.iv_alarm);
        }
    }
}