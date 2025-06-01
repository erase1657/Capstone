// AlarmActivity.java

package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView; // TextView import 추가
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale; // Locale import 추가

public class AlarmActivity extends AppCompatActivity {

    private TextView tvAlarmName; // 알람 이름을 표시할 TextView (레이아웃에 정의 필요)
    private TextView tvAlarmTime; // 알람 시간을 표시할 TextView (레이아웃에 정의 필요)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wakeup_alarm);  //

        // 레이아웃 파일(wakeup_alarm.xml)에 정의된 TextView ID를 사용해야 합니다.
        // 예시 ID: tv_wakeup_name, tv_wakeup_time
        tvAlarmName = findViewById(R.id.tv_wakeup_name); // wakeup_alarm.xml 에 해당 ID의 TextView 필요
        tvAlarmTime = findViewById(R.id.tv_wakeup_time); // wakeup_alarm.xml 에 해당 ID의 TextView 필요

        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        if (intent != null) {
            String alarmName = intent.getStringExtra("ALARM_NAME");
            int alarmHour = intent.getIntExtra("ALARM_HOUR", -1); // 기본값 -1 (오류 확인용)
            int alarmMinute = intent.getIntExtra("ALARM_MINUTE", -1); // 기본값 -1 (오류 확인용)
            // long alarmId = intent.getLongExtra("ALARM_ID", -1); // 필요시 ID도 받을 수 있음

            // 가져온 데이터 화면에 표시
            if (alarmName != null) {
                tvAlarmName.setText(alarmName);
            } else {
                tvAlarmName.setText("알람"); // 이름이 없는 경우 기본값
            }

            if (alarmHour != -1 && alarmMinute != -1) {
                tvAlarmTime.setText(String.format(Locale.getDefault(), "%02d:%02d", alarmHour, alarmMinute));
            } else {
                tvAlarmTime.setText("시간 정보 없음"); // 시간 정보가 없는 경우
            }
        }
    }
}