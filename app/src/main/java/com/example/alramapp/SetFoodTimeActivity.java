package com.example.alramapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.alramapp.Alarm.AlarmData;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*
    TODO:  알람 객체를 AlarmManager에 등록, AlarmReceiver로 전달

 */

/**
 * 밥 시간 설정 페이지 구현
 */
public class SetFoodTimeActivity extends AppCompatActivity {


    private static final String TAG = "SetFoodTimeActivity";

    private AlarmData alarmData;
    private Button btn_save, btn_back;
    private TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.set_foodtime_page);



        btn_save = findViewById(R.id.btn_save);
        btn_back = findViewById(R.id.btn_back);
        timePicker = findViewById(R.id.timePicker);
        Intent getIntent = getIntent();
        String name = getIntent.getStringExtra("name");
        String gender = getIntent.getStringExtra("finalGender");

        btn_back.setOnClickListener(v -> finish());
        alarmData = new AlarmData();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                alarmData.setHour(timePicker.getHour());
                alarmData.setMinute(timePicker.getMinute());
                alarmData.setRepeat("매일");
                alarmData.setName("밥 시간 알람");
                alarmData.setIsFood(1);
                alarmData.setIsEnabled(true);
                alarmData.setMisOn(true);
                alarmData.setMis_num(1);
                alarmData.setMis_count(25);
                alarmData.setSoundOn(false);
                alarmData.setSound("사운드 미설정(진동 알람");


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(SetFoodTimeActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                alarmData.setUserUid(user.getUid());

                // DB에 저장
                AlarmDBHelper dbHelper = new AlarmDBHelper(SetFoodTimeActivity.this);
                long newId = dbHelper.insertAlarm(alarmData);
                alarmData.setId((int) newId);

                new AlertDialog.Builder(SetFoodTimeActivity.this)
                        .setTitle("다음과 같이 캐릭터를 생성/수정합니다.")
                        .setMessage("펫 이름: " + name + "\n"
                                    + "펫 성별" + gender + "\n"
                                    + "먹이 시간" + alarmData.getHour() + ":" + alarmData.getMinute())
                        .setPositiveButton("확인", (dialog, which) -> {
                            Intent intent = new Intent(SetFoodTimeActivity.this, MainActivity.class);
                            intent.putExtra("alarmData", alarmData);

                            startActivity(intent);
                            finish();

                        })
                        .setNegativeButton("취소", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();

            }
        });

    }
}