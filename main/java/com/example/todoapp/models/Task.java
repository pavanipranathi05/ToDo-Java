package com.example.todoapp.model;

public class Task {
    private int id;
    private String title;
    private String description;
    private String date;
    private String time;
    private int priority;
    private boolean hasAlarm;

    public Task(int id, String title, String description, String date, String time, int priority, boolean hasAlarm) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.hasAlarm = hasAlarm;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isHasAlarm() { return hasAlarm; }
    public void setHasAlarm(boolean hasAlarm) { this.hasAlarm = hasAlarm; }
}