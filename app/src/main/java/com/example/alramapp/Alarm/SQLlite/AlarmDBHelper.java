package com.example.alramapp.Alarm.SQLlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.alramapp.Alarm.AlarmData;

import java.util.ArrayList;
import java.util.List;


//alarmData 객체에 저장된 데이터를 getter를 통해 DB에 접근하는 클래스
//SQLlite를 사용하여 알람데이터는 개인로컬에 저장(파이어베이스에 저장하면 너무 무거울것같음)
public class AlarmDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "alarms.db";
    private static final int    DB_VER  = 2; // ⬅️ 버전 올려서 DB 초기화 유도
    private static final String TABLE = "alarms";

    public AlarmDBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    /**
     * 객체 생성시 테이블 생성
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE alarms (" +
                        "id           INTEGER PRIMARY KEY AUTOINCREMENT," +     //알람 아이디(각 알람의 고유 번호)
                        "name         TEXT," +                                  //알람 이름
                        "hour         INTEGER," +                               //시간
                        "minute       INTEGER," +                               //분
                        "repeat       TEXT," +                                  //반복 주기
                        "mission_num  INTEGER," +                               //미션 번호
                        "mission_cnt  INTEGER," +                               //미션 조건
                        "sound        TEXT," +                                  //사운드 이름
                        "volume       INTEGER," +                               //사운드 볼륨 크기
                        "mis_on       INTEGER," +                               //미션 on/off
                        "sound_on     INTEGER," +                               //사운드 on/off(사운드없을 시 기본 진동알람)
                        "is_enabled   INTEGER," +                               //알람 on/off
                        "user_uid     TEXT NOT NULL" +                          //사용자 uid(이 값 기반으로 사용자를 분리)
                        ");"
        );
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /**
     * 테이블 데이터 삽입
     * @param data
     * @return
     */
    public long insertAlarm(AlarmData data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name",      data.getName());
        cv.put("hour",      data.getHour());
        cv.put("minute",    data.getMinute());
        cv.put("repeat",    data.getRepeat());
        cv.put("sound",     data.getSound());
        cv.put("volume",    data.getVolume());
        cv.put("mission_num", data.getMis_num());
        cv.put("mission_cnt", data.getMis_count());
        cv.put("mis_on",    data.getMisOn() ? 1 : 0);
        cv.put("sound_on",  data.getSoundOn() ? 1 : 0);
        cv.put("is_enabled", data.getIsEnabled() ? 1 : 0);
        cv.put("user_uid",  data.getUserUid());

        long newId = db.insert("alarms", null, cv);
        db.close();
        return newId;
    }

    /**
     * 사용자 uid를 기반으로 모든 알람 아이템 가져오기
     * @param userUid 사용자 계정 uid
     * @return list 알람 아이템 리스트
     */
    public List<AlarmData> getAllAlarms(String userUid) {
        List<AlarmData> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(TABLE, null, "user_uid = ?", new String[]{userUid}, null, null, "id ASC");
        while (c.moveToNext()) {
            AlarmData a = new AlarmData();
            a.setId(        c.getLong(c.getColumnIndexOrThrow("id")));
            a.setName(      c.getString(c.getColumnIndexOrThrow("name")));
            a.setHour(      c.getInt(   c.getColumnIndexOrThrow("hour")));
            a.setMinute(    c.getInt(   c.getColumnIndexOrThrow("minute")));
            a.setRepeat(    c.getString(c.getColumnIndexOrThrow("repeat")));
            a.setSound(     c.getString(c.getColumnIndexOrThrow("sound")));
            a.setVolume(    c.getInt(   c.getColumnIndexOrThrow("volume")));
            a.setMis_num(   c.getInt(   c.getColumnIndexOrThrow("mission_num")));
            a.setMis_count( c.getInt(   c.getColumnIndexOrThrow("mission_cnt")));
            a.setMisOn(     c.getInt(   c.getColumnIndexOrThrow("mis_on")) == 1);
            a.setSoundOn(   c.getInt(   c.getColumnIndexOrThrow("sound_on")) == 1);
            a.setIsEnabled( c.getInt(   c.getColumnIndexOrThrow("is_enabled")) == 1);
            a.setUserUid(   c.getString(c.getColumnIndexOrThrow("user_uid")));
            list.add(a);
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * 테이블 데이터 수정
     * @param data  수정할 데이터. AlarmData객체
     * @return  row 알람 id값에 따른 수정이 진행될 행 번호
     */
    public int updateAlarm(AlarmData data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", data.getName());
        cv.put("hour", data.getHour());
        cv.put("minute", data.getMinute());
        cv.put("repeat", data.getRepeat());
        cv.put("sound", data.getSound());
        cv.put("volume", data.getVolume());
        cv.put("mission_num", data.getMis_num());
        cv.put("mission_cnt", data.getMis_count());
        cv.put("mis_on", data.getMisOn() ? 1 : 0);
        cv.put("sound_on", data.getSoundOn() ? 1 : 0);
        cv.put("is_enabled", data.getIsEnabled() ? 1 : 0);
        cv.put("user_uid", data.getUserUid());

        int rows = db.update(TABLE, cv, "id = ?", new String[]{String.valueOf(data.getId())});
        db.close();
        return rows;
    }

    /**
     * 테이블 데이터 삭제
     * @param id
     * @return
     */
    public int deleteAlarm(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /**
     * 알람 ID로 하나의 AlarmData 객체 조회
     * @param id 알람 아이디
     * @return AlarmData 객체 또는 null (없는 경우)
     */
    public AlarmData getAlarmById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        AlarmData alarmData = null;

        Cursor c = db.query(TABLE, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                alarmData = new AlarmData();
                alarmData.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                alarmData.setName(c.getString(c.getColumnIndexOrThrow("name")));
                alarmData.setHour(c.getInt(c.getColumnIndexOrThrow("hour")));
                alarmData.setMinute(c.getInt(c.getColumnIndexOrThrow("minute")));
                alarmData.setRepeat(c.getString(c.getColumnIndexOrThrow("repeat")));
                alarmData.setSound(c.getString(c.getColumnIndexOrThrow("sound")));
                alarmData.setVolume(c.getInt(c.getColumnIndexOrThrow("volume")));
                alarmData.setMis_num(c.getInt(c.getColumnIndexOrThrow("mission_num")));
                alarmData.setMis_count(c.getInt(c.getColumnIndexOrThrow("mission_cnt")));
                alarmData.setMisOn(c.getInt(c.getColumnIndexOrThrow("mis_on")) == 1);
                alarmData.setSoundOn(c.getInt(c.getColumnIndexOrThrow("sound_on")) == 1);
                alarmData.setIsEnabled(c.getInt(c.getColumnIndexOrThrow("is_enabled")) == 1);
                alarmData.setUserUid(c.getString(c.getColumnIndexOrThrow("user_uid")));
            }
            c.close();
        }
        db.close();

        return alarmData;
    }
}
