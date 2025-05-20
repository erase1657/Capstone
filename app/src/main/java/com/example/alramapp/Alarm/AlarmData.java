package com.example.alramapp.Alarm;

import java.io.Serializable;

public class AlarmData implements Serializable {
    private long id;
    private String name;      //알람 이름
    private int hour;         //시간
    private int minute;       //분
    private String repeat;       //반복 요일
    private String sound;        //알람 음원
    private int volume;          //알람 소리
    private int mis_num;      //미션 선택
    private int mis_count;       //미션 조건
    private boolean mis_On;        //미션 진행 유무
    private boolean soundOn;
    private boolean isEnabled;
    private String userUid;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getMis_num() {
        return mis_num;
    }

    public void setMis_num(int mis_num) {
        this.mis_num = mis_num;
    }

    public int getMis_count() {
        return mis_count;
    }

    public void setMis_count(int mis_count) {
        this.mis_count = mis_count;
    }

    public boolean getMisOn() {return mis_On;}

    public void setMisOn(boolean mis_On) {this.mis_On = mis_On;}

    public boolean getSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }
    public boolean getIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getUserUid() {
        return userUid;
    }
    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
    public void copyFrom(AlarmData other) {
        this.hour = other.hour;
        this.minute = other.minute;
        this.name = other.name;
        this.isEnabled = other.isEnabled;
        this.repeat = other.repeat;
        // 기타 필드도 모두 복사
    }
}
