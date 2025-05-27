package com.example.alramapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
    TODO: 1. 타임피커로 밥 시간 설정
          2. 설정된 시간을 알람 객체 생성 후, AlarmData에 set
          3. 알람 객체를 AlarmManager에 등록, AlarmReceiver로 전달
          4. sqlLite에 저장
 */

/**
 * 밥 시간 설정 페이지 구현
 */
public class SetFoodTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.set_foodtime_page);

    }
}