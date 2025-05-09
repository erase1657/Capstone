package com.example.alramapp.RecycleView;

public class Board {

    private String time;
    private String amp;
    private String dow;
    private boolean isChecked;

    public Board(String time, String amp, String dow, boolean isChecked) {
        this.time = time;
        this.amp = amp;
        this.dow = dow;
        this.isChecked = isChecked;
    }

    public String getTime() {
        return time;
    }
    public String setTime(String time) {
        return this.time = time;
    }


    public String getAmp() { return amp;}
    public String setAmp(String amp) { return this.amp = amp;}


    public String getDow() { return dow;}
    public String setDow(String dow) { return this.dow = dow;}


    public boolean isChecked() { return isChecked;}
    public boolean setChecked(boolean isChecked) { return this.isChecked = isChecked;}

}
