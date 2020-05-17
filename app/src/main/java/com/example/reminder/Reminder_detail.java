package com.example.reminder;

import java.time.LocalDateTime;

public class Reminder_detail {
    String reminder_desc;
    String reminder_date;
    String reminder_time;
    LocalDateTime reminder_f_date;

    public String getReminder_key() {
        return reminder_key;
    }

    public void setReminder_key(String reminder_key) {
        this.reminder_key = reminder_key;
    }

    String reminder_key;


    public Reminder_detail(){

    }

    public Reminder_detail(String reminder_desc, String reminder_date, String reminder_time){
        this.reminder_desc = reminder_desc;
        this.reminder_date = reminder_date ;
        this.reminder_time = reminder_time;
    }

    public void setReminder_desc(String reminder_desc) {
        this.reminder_desc = reminder_desc;
    }

    public void setReminder_date(String reminder_date) {
        this.reminder_date = reminder_date;
    }

    public void setReminder_time(String reminder_time) {
        this.reminder_time = reminder_time;
    }

    public void setReminder_f_date(LocalDateTime reminder_f_date) {
        this.reminder_f_date = reminder_f_date;
    }


    public String getReminder_desc() {
        return reminder_desc;
    }

    public String getReminder_date() {
        return reminder_date;
    }

    public String getReminder_time() {
        return reminder_time;
    }

    public LocalDateTime getReminder_f_date() {
        return reminder_f_date;
    }


}
