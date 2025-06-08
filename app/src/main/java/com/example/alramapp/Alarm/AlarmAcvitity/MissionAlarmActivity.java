package com.example.alramapp.Alarm.AlarmAcvitity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Alarm.SQLlite.AlarmData;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;


public class MissionAlarmActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MissionAlarmActivity", "onCreate - ENTERED");
        // 1. AlarmId 및 기타 데이터 받기
        Intent intent = getIntent();
        long alarmId = intent.getLongExtra("alarmId", -1);

        // 2. Alarm 정보 로드(DB 필요)
        AlarmDBHelper dbHelper = new AlarmDBHelper(this);
        AlarmData alarmData = dbHelper.getAlarmById(alarmId);

        int missionCode = alarmData.getMis_num(); // 1:터치, 2:흔들기
        Log.d("MissionAlarmActivity", "미션 코드: " + missionCode);

        // 3. 미션코드별로 Activity 분기
        Class<?> missionActivity;
        switch(missionCode) {
            case 1:
                missionActivity = MissionTouchActivity.class;
                Log.d("MissionAlarmActivity", "터치 미션 시작");
                break;
            case 2:
                missionActivity = MissionShakeActivity.class;
                break;
            default:
                missionActivity = MissionTouchActivity.class; // 기본값은 터치
        }
        //4. 액티비티에 전달할 데이터 초기화
        int missionCondition = alarmData.getMis_count();    //미션 조건
        String user_uid = alarmData.getUserUid();          //유저 uid
        String rep = alarmData.getRepeat();                 //반복
        boolean is_enabled = alarmData.getIsEnabled();      //활성화 여부
        // 5. 해당 미션 Activity로 전달
        Intent missionIntent = new Intent(this, missionActivity);
        missionIntent.putExtra("condition", missionCondition);
        missionIntent.putExtra("repeat" , rep);
        missionIntent.putExtra("user_uid", user_uid);
        missionIntent.putExtra("is_enabled", is_enabled);
        missionIntent.putExtra("alarmData", alarmData);
        startActivity(missionIntent);

        // 6. 런처 Activity는 종료
        finish();
    }

}
