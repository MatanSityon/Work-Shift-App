package com.example.workshiftapp.models;

public class Worker {
    private String email;
    private String fullName;
    private double wage;
    public Worker(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double salary) {
        this.wage = wage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
