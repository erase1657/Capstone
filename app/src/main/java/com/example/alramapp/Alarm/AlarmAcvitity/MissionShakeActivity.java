package com.example.alramapp.Alarm.AlarmAcvitity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Alarm.SQLlite.AlarmData;
import com.example.alramapp.Alarm.AlarmSoundService;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.RealTimeDatabase.DataAccess;
import com.example.alramapp.R;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class MissionShakeActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Mission2Activity";
    private DataAccess database;
    private FirebaseUser currentUser;
    private DatabaseReference userScoreRef, userLifeRef;
    private AlarmDBHelper dbHelper;
    private AlarmData alarmData;
    private String rep, uid;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ProgressBar progressBar;
    private TextView shakeCountText,tv_mission;
    private ImageView petImageView;
    private ImageView bowlImageView;
    private Button giveUpButton;
    private SwipeButton swipeButton; // SwipeButton 변수 선언

    private int shakeCount = 0;
    private int totalCount;
    private boolean missionCompleted = false;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int MIN_TIME_BETWEEN_SHAKES_MS = 500;
    private long lastShakeTime = 0;
    private float lastX, lastY, lastZ;
    private boolean isInitialized = false;


    // Firebase (체력 감소 기능을 위해)
    // private DataAccess database;
    // private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mission_alarm_page);



        // UI 요소 연결
        progressBar = findViewById(R.id.missionProgressBar);
        shakeCountText = findViewById(R.id.CountText); // mission2.xml의 ID와 일치해야 함
        petImageView = findViewById(R.id.img_pet);
        bowlImageView = findViewById(R.id.img_bowl);
        giveUpButton = findViewById(R.id.giveup); // "포기" 버튼 ID
        swipeButton = findViewById(R.id.swipbutton_mission_complete); // SwipeButton ID
        tv_mission = findViewById(R.id.mission);

        dbHelper = new AlarmDBHelper(this);

        totalCount = getIntent().getIntExtra("condition",15);
        uid = getIntent().getStringExtra("user_uid");
        alarmData = (AlarmData) getIntent().getSerializableExtra("alarmData");
        rep = getIntent().getStringExtra("repeat");

        userScoreRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("score");
        userLifeRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("life");

        showMission();

        // 센서 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // "포기" 버튼 리스너
        giveUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveup();
                stopAlarmSound();
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null && sensorManager != null && !missionCompleted) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Accelerometer listener registered.");
            isInitialized = false; // onResume 시 센서 값 초기화 플래그 리셋
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Accelerometer listener unregistered.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (missionCompleted) { // 이미 미션 완료 시 더 이상 센서값 처리 안함
            onPause();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // ... (이전 답변의 흔들기 감지 로직은 동일하게 유지)
            long currentTime = System.currentTimeMillis();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (!isInitialized) {
                lastX = x;
                lastY = y;
                lastZ = z;
                isInitialized = true;
                return;
            }

            float deltaX = Math.abs(lastX - x);
            float deltaY = Math.abs(lastY - y);
            float deltaZ = Math.abs(lastZ - z);

            if ((currentTime - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MS) {
                if (deltaX > SHAKE_THRESHOLD_GRAVITY || deltaY > SHAKE_THRESHOLD_GRAVITY || deltaZ > SHAKE_THRESHOLD_GRAVITY) {
                    shakeCount++;
                    lastShakeTime = currentTime;
                    progressBar.setProgress(shakeCount);
                    shakeCountText.setText(shakeCount + "/" + totalCount + " 개 남았어요!");

                    if (shakeCount == totalCount) {
                        missionCompleted = true; // 미션 완료 플래그 설정

                        MissionComplete();

                        swipeButton.setOnActiveListener(new OnActiveListener() {
                            @Override
                            public void onActive() {
                                Log.d(TAG, "SwipeButton activated!");



                                //단발성 알림이라면 메인화면의 알람리스트에서 알람스위치를 끔
                                if (rep.equals("반복 없음")) {

                                    alarmData.setIsEnabled(false);
                                    dbHelper.updateAlarm(alarmData);
                                }

                                stopAlarmSound(); //알람 소리 종료
                                finish(); // 현재 AlarmActivity 종료
                            }
                        });

                    }
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    /**
     * 초기 화면 세팅
     */
    private void showMission(){
        tv_mission.setText("화면을 흔들어 주세요!");
        //진행 바 초기화
        progressBar.setMax(totalCount);
        progressBar.setProgress(0);

        //진행 문구 초기화
        shakeCountText.setText(shakeCount + "/" + totalCount + " 개 남았어요!");

        //알람 종료 스와이프 버튼(초기는 GONE상태)
        swipeButton.setVisibility(View.GONE);

    }
    private void MissionComplete(){

        shakeCountText.setText("미션 완료!");

        // 이미지 변경
        petImageView.setImageResource(R.drawable.cat_smiling); // 준비된 '행복한 고양이' 이미지 리소스명으로 변경
        bowlImageView.setImageResource(R.drawable.bowl_fill);    // 준비된 '밥이 채워진 밥그릇' 이미지 리소스명으로 변경

        // 버튼 상태 변경
        giveUpButton.setVisibility(View.GONE); // "포기" 버튼 숨기기
        swipeButton.setVisibility(View.VISIBLE); // 스와이프 버튼 보이기

        //점수 변경
        userScoreRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentScore = currentData.getValue(Integer.class);
                currentData.setValue(currentScore + 1);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError == null && committed) {
                    Toast.makeText(MissionShakeActivity.this, "점수가 업데이트 성공", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MissionShakeActivity.this, "점수 업데이트 실패", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    /**
     * 알람 사운드 중지
     */
    private void stopAlarmSound() {
        Log.d(TAG, "stopAlarmService - Stopping AlarmSoundService");
        Intent intent = new Intent(this, AlarmSoundService.class);
        // 추가적으로 여러 알람이 중첩 가능하면 alarmId 값도 넘겨서, 특정 알람만 종료하도록 설계할 수 있음
        stopService(intent);
    }

    /**
     * 라이프값 변경 메서드
     */
    private void giveup(){
        userLifeRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentLife = currentData.getValue(Integer.class);
                if(currentLife > 0){
                    currentData.setValue(currentLife - 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError == null && committed) {
                    Toast.makeText(MissionShakeActivity.this, "포기 선택 라이프 -1", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MissionShakeActivity.this, "라이프 업데이트 실패", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


}