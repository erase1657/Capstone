package com.example.alramapp.Alarm;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.R;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;

import java.util.Calendar;
import java.util.Objects;

public class DefaultAlarmActivity extends AppCompatActivity {
    private static final String TAG = "DefaultAlarmActivity";

    // 뷰
    private TextView alarmName, alarmId, time, repeat, mission, sound, user_uid;
    private SwipeButton swipeButton;
    private MediaPlayer mediaPlayer;

    // DB 헬퍼와 데이터
    private AlarmDBHelper dbHelper;
    private AlarmData alarmData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        setContentView(R.layout.default_alarm_page);

        // 1) 뷰 바인딩
        alarmName   = findViewById(R.id.alarmnaem);
        alarmId     = findViewById(R.id.alarmid);
        time        = findViewById(R.id.time);
        repeat      = findViewById(R.id.repeat);
        mission     = findViewById(R.id.mission);
        sound       = findViewById(R.id.sound);
        user_uid    = findViewById(R.id.user_uid);
        swipeButton = findViewById(R.id.swipbutton);

        // 2) Intent 에서 alarmId 와 repeat_day(요일) 받아오기
        Intent intent = getIntent();
        long id = intent.getLongExtra("alarmId", -1);
        int repeatDay = intent.getIntExtra("repeat_day", -1);
        // repeatDay: 1=일요일,2=월요일…7=토요일, -1 이면 “요일 정보 없음(단발 또는 테스트/매일 모드)”

        // 요일 문자열로 변환
        String dayStr = "";
        if (repeatDay >= Calendar.SUNDAY && repeatDay <= Calendar.SATURDAY) {
            switch (repeatDay) {
                case Calendar.SUNDAY:    dayStr = "일"; break;
                case Calendar.MONDAY:    dayStr = "월"; break;
                case Calendar.TUESDAY:   dayStr = "화"; break;
                case Calendar.WEDNESDAY: dayStr = "수"; break;
                case Calendar.THURSDAY:  dayStr = "목"; break;
                case Calendar.FRIDAY:    dayStr = "금"; break;
                case Calendar.SATURDAY:  dayStr = "토"; break;
            }
            Log.d(TAG, "Alarm triggered for weekday: " + dayStr + "요일 (" + repeatDay + ")");
        } else {
            Log.d(TAG, "Alarm triggered with no specific weekday (repeatDay=" + repeatDay + ")");
        }

        // 3) DB 에서 AlarmData 조회
        dbHelper = new AlarmDBHelper(this);
        alarmData = dbHelper.getAlarmById(id);
        if (alarmData == null) {
            Log.w(TAG, "No AlarmData found for id=" + id);
            finish();
            return;
        }

        // 4) 화면에 데이터 세팅
        alarmName.setText(alarmData.getName());
        alarmId.setText(String.valueOf(alarmData.getId()));
        time.setText(String.format("%02d:%02d",
                alarmData.getHour(), alarmData.getMinute()));

        // 반복 설정 표시 (DB에 저장된 repeat 문자열)
        String rep = alarmData.getRepeat();
        String displayRep;
        if (rep == null || rep.trim().isEmpty()
                || rep.equals("반복 없음") || rep.equals("없음")) {
            displayRep = "반복 없음";
        }
        else if (rep.equals("매일")) {
            displayRep = "매일";
        }
        else {
            // “월,수,금” 형태 → 보기 좋게 “월, 수, 금”
            displayRep = rep.replaceAll(",", ", ");
        }

        // 여기에 만약 실제로 울린 요일 정보를 함께 보여주고 싶다면
        // dayStr 값이 non-empty 일 때 displayRep 뒤에 붙입니다.
        if (!dayStr.isEmpty()) {
            displayRep += " (" + dayStr + "요일)";
        }
        repeat.setText(displayRep);

        // mission, sound, user_uid 표시
        if (alarmData.getMisOn()) {
            mission.setText("미션 활성화: "
                    + alarmData.getMis_num()
                    + " / " + alarmData.getMis_count());
        } else {
            mission.setText("미션 비활성화");
        }
        sound.setText(alarmData.getSound());
        user_uid.setText(alarmData.getUserUid());

        // 5) 알람음 재생
        initAndStartAlarmSound(alarmData.getSound());

        // 6) 스와이프 버튼 처리
        swipeButton.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
                // 단발성(“반복 없음”)인 경우 DB에도 disabled 처리
                if (rep == null || rep.trim().isEmpty()
                        || rep.equals("반복 없음") || rep.equals("없음")) {
                    alarmData.setIsEnabled(false);
                    dbHelper.updateAlarm(alarmData);
                }
                // 알람음 중지
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                finish();
            }
        });
    }

    /**
     * 알람 사운드 초기화 & 재생
     */
    private void initAndStartAlarmSound(String soundName) {
        int resId = 0;
        if (soundName != null) {
            switch (soundName) {
                case "샘플 알람음1": resId = R.raw.sample1; break;
                case "샘플 알람음2": resId = R.raw.sample2; break;
                case "샘플 알람음3": resId = R.raw.sample3; break;
                case "사운드 미설정(진동 알람)":
                    // 진동 로직 등 추가 가능
                    return;
            }
        }
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(this, resId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

}