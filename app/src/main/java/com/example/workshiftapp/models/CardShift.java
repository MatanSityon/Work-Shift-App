package com.example.workshiftapp.models;

public class CardShift
{
    String startTime;
    String endTime;
    String date;
    String hours;
    String day;
    public CardShift(String startTime, String endTime, String date, String hours, String day) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.hours = hours + " Hours";
        this.day = day;
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
    public String getHours() {
        return hours;
    }
    public void setHours(String hours) {
        this.hours = hours;
    }
    public String getDay() {
        return day;
    }
    public void setDay(String day) {
        this.day = day;
    }
}
