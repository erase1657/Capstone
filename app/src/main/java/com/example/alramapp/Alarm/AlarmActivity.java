// AlarmActivity.java 전체 코드 (수정본)

package com.example.alramapp.Alarm;

import android.content.Intent;
import android.media.MediaPlayer; // MediaPlayer import
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.R;
import com.example.swipebutton_library.SwipeButton; // SwipeButton import
import com.example.swipebutton_library.OnActiveListener; // OnActiveListener import

import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = "AlarmActivity"; // 로그 태그 추가

    private AlarmDBHelper dbHelper;
    private AlarmData alarmData;
    private TextView tvAlarmName;
    private TextView tvAlarmTime;
    private SwipeButton swipeButton; // 스와이프 버튼 변수
    private MediaPlayer mediaPlayer; // MediaPlayer 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        setContentView(R.layout.wakeup_alarm);


        Log.d(TAG, "onCreate - ENTERED");

        tvAlarmName = findViewById(R.id.tv_wakeup_name);
        tvAlarmTime = findViewById(R.id.tv_wakeup_time); // XML에서 TextView로 변경한 ID
        swipeButton = findViewById(R.id.swipbutton); // wakeup_alarm.xml에 있는 SwipeButton ID

        //알람 매니저에서 알람 id 값을 넘겨 받음
        Intent intent = getIntent();
        long id = intent.getLongExtra("alarmId", -1);


        //넘겨 받은 알람 id값을 기반으로 데이터베이스 탐색 메서드 getAlarmById호출
        dbHelper = new AlarmDBHelper(this);
        alarmData = dbHelper.getAlarmById(id);

        //데이터베이스에서 가져온 값으로 변수 초기화
        String alarmName = alarmData.getName();     //알람 이름
        int alarmHour = alarmData.getHour();        //시
        int alarmMinute = alarmData.getMinute();    //분
        String soundName = alarmData.getSound();    //소리
        String rep = alarmData.getRepeat();         //반복 주기

        //UI에 값 설정
        tvAlarmName.setText(alarmName);
        tvAlarmTime.setText(String.format(Locale.getDefault(), "%02d:%02d", alarmHour, alarmMinute));




        swipeButton.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
                Log.d(TAG, "SwipeButton activated!");

                //단발성 알림이라면 메인화면의 알람리스트에서 알람스위치를 끔
                if (rep.equals("반복 없음")) {
                    alarmData.setIsEnabled(false);
                    dbHelper.updateAlarm(alarmData);
                }

                //알람 소리 중지
                stopAlarmSound();

                finish(); // 현재 AlarmActivity 종료
            }
        });

    }


    private void stopAlarmSound() {
        Log.d(TAG, "stopAlarmService - Stopping AlarmSoundService");
        Intent intent = new Intent(this, AlarmSoundService.class);
        // 추가적으로 여러 알람이 중첩 가능하면 alarmId 값도 넘겨서, 특정 알람만 종료하도록 설계할 수 있음
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - Stopping and releasing MediaPlayer.");
    }
}