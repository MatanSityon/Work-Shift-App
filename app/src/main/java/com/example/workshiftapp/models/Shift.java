package com.example.workshiftapp.models;

public class Shift {
    String worker;
    String startTime;
    String endTime;
    String date;


    public Shift(String worker, String startTime, String endTime, String date) {
        this.worker = worker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }


    public Shift(String worker, String startTime, String endTime) {
        this.date = worker;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
