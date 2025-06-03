// AlarmActivity.java 전체 코드 (수정본)

package com.example.alramapp;

import android.content.Intent;
import android.media.MediaPlayer; // MediaPlayer import
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swipebutton_library.SwipeButton; // SwipeButton import
import com.example.swipebutton_library.OnActiveListener; // OnActiveListener import

import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = "AlarmActivity"; // 로그 태그 추가

    private TextView tvAlarmName;
    private TextView tvAlarmTime;
    private SwipeButton swipeButton; // 스와이프 버튼 변수
    private MediaPlayer mediaPlayer; // MediaPlayer 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wakeup_alarm);
        Log.d(TAG, "onCreate - ENTERED");

        tvAlarmName = findViewById(R.id.tv_wakeup_name);
        tvAlarmTime = findViewById(R.id.tv_wakeup_time); // XML에서 TextView로 변경한 ID
        swipeButton = findViewById(R.id.swipbutton); // wakeup_alarm.xml에 있는 SwipeButton ID

        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, "Intent extras: " + (intent.getExtras() != null ? intent.getExtras().toString() : "null"));
            String alarmName = intent.getStringExtra("ALARM_NAME");
            int alarmHour = intent.getIntExtra("ALARM_HOUR", -1);
            int alarmMinute = intent.getIntExtra("ALARM_MINUTE", -1);
            String soundName = intent.getStringExtra("ALARM_SOUND_NAME"); // 알람 사운드 이름 받기

            if (alarmName != null) {
                tvAlarmName.setText(alarmName);
            } else {
                tvAlarmName.setText("알람");
            }

            if (alarmHour != -1 && alarmMinute != -1) {
                tvAlarmTime.setText(String.format(Locale.getDefault(), "%02d:%02d", alarmHour, alarmMinute));
            } else {
                tvAlarmTime.setText("시간 정보 없음");
            }

            Log.d(TAG, "Received Sound Name: " + soundName);
            initAndStartAlarmSound(soundName); // 사운드 재생 메소드 호출

        } else {
            Log.e(TAG, "Intent is null!");
        }

        // 스와이프 버튼 리스너 설정 (문제 2 해결)
        if (swipeButton != null) {
            swipeButton.setOnActiveListener(new OnActiveListener() {
                @Override
                public void onActive() {
                    Log.d(TAG, "SwipeButton activated!");
                    stopAlarmSound(); // 알람 소리 중지

                    Intent mainActivityIntent = new Intent(AlarmActivity.this, MainActivity.class);
                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(mainActivityIntent);
                    finish(); // 현재 AlarmActivity 종료
                }
            });
        } else {
            Log.e(TAG, "SwipeButton not found!");
        }
    }

    private void initAndStartAlarmSound(String soundName) {
        if (soundName == null || soundName.isEmpty() || "사운드 미설정(진동 알람)".equals(soundName)) {
            Log.d(TAG, "Sound name is null, empty, or default no sound. No sound will be played.");
            // 필요시 기본 진동 처리 추가 가능
            return;
        }

        int soundResId = 0;
        switch (soundName) {
            case "샘플 알람음1":
                soundResId = R.raw.sample1; // res/raw 폴더에 sample1.mp3 등이 있어야 함
                break;
            case "샘플 알람음2":
                soundResId = R.raw.sample2;
                break;
            case "샘플 알람음3":
                soundResId = R.raw.sample3;
                break;
            default:
                Log.w(TAG, "Unknown sound name: " + soundName);
                break;
        }

        if (soundResId != 0) {
            Log.d(TAG, "Playing sound resource ID: " + soundResId);
            if (mediaPlayer != null) { // 기존 MediaPlayer가 있다면 해제
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, soundResId);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // 반복 재생
                mediaPlayer.start();
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for resource ID: " + soundResId);
            }
        } else {
            Log.d(TAG, "No valid sound resource ID found for sound name: " + soundName);
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            Log.d(TAG, "Stopping and releasing MediaPlayer.");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - Stopping and releasing MediaPlayer.");
        stopAlarmSound(); // 액티비티 종료 시 MediaPlayer 리소스 해제
    }
}