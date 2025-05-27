package com.example.alramapp.Alarm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.R;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;

/*
    TODO:
 */

/**
 * 기본 알람 화면 구현
 */
public class DefaultAlarmActivity extends AppCompatActivity {
    private SwipeButton swipeButton;
    private TextView alarmName, alarmId, time, repeat, mission, sound, user_uid;
    private MediaPlayer mediaPlayer;
    private long Id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.default_alarm_page);

        Intent intent = getIntent();
        Id = intent.getLongExtra("alarmId", -1);



        alarmName = findViewById(R.id.alarmnaem);  // 오타 수정 필요
        alarmId = findViewById(R.id.alarmid);
        time = findViewById(R.id.time);
        repeat = findViewById(R.id.repeat);
        mission = findViewById(R.id.mission);
        sound = findViewById(R.id.sound);
        user_uid = findViewById(R.id.user_uid);
        swipeButton = findViewById(R.id.swipbutton); // 뷰 id도 확인

        String a = intent.getStringExtra("alarmName");
        long b = intent.getLongExtra("alarmId", -1);
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        int c = intent.getIntExtra("mis_num", 0);
        int d = intent.getIntExtra("mis_cnt", 0);
        boolean misOn = intent.getBooleanExtra("mis_on", false);
        String e = intent.getStringExtra("sound");
        String f = intent.getStringExtra("user_uid");
        int repeatDay = intent.getIntExtra("repeat_day", -1);

        String display;
        if (repeatDay == -1) {
            display = "매일";
        } else {
            String[] weekdays = {"", "일", "월", "화", "수", "목", "금", "토"};
            display = weekdays[repeatDay];
        }
        repeat.setText(display);

        alarmName.setText(a != null ? a : "이름없음");
        alarmId.setText(String.valueOf(b));
        time.setText(String.format("%02d:%02d", hour, minute));
        mission.setText(misOn ? ("미션 활성화: " + c + " / " + d)  : "미션 비활성화");
        sound.setText(e != null ? e : "");
        user_uid.setText(f != null ? f : "");

        android.util.Log.d("DefaultAlarmActivity", "Alarm Activity started with alarmId=" + b);

        initAndStartAlarmSound(e);

        swipeButton.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {


                // 단발성일 때만 꺼주기
                if ("반복 없음".equals(repeat)) {
                    AlarmDBHelper dbHelper = new AlarmDBHelper(DefaultAlarmActivity.this);
                    AlarmData data = dbHelper.getAlarmById(Id);
                    if (data != null) {
                        data.setIsEnabled(false);
                        dbHelper.updateAlarm(data);

                    }
                }
                mediaPlayer.stop();
                finish();
            }
        });



    }

    // 알람 소리 초기화, 재생
    private void initAndStartAlarmSound(String soundName) {
        // 기본 사운드 파일 리소스 지정, 실제 soundName에 따라 변경 가능
        int soundResId = 0; // res/raw/alarm_sound.mp3 파일 필요

        // soundName에 따른 사운드 재생 분기 처리 (필요시)
        if (soundName != null) {
            switch (soundName) {
                case "샘플 알람음1":
                    soundResId = R.raw.sample1;
                    break;
                case "샘플 알람음2":
                    soundResId = R.raw.sample2;
                    break;
                case "샘플 알람음3":
                    soundResId = R.raw.sample3;
                    break;
                case "사운드 미설정(진동 알람)":
                    // 사운드 없음, 진동 처리 필요 시 추가
                    return;
            }
        }

        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

}