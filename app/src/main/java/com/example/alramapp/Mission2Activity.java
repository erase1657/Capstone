package com.example.alramapp; // 사용자님의 패키지 이름

import android.content.Context;
import android.content.Intent; // Intent import 추가
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

import androidx.appcompat.app.AppCompatActivity;

// SwipeButton 관련 import 추가
import com.example.swipebutton_library.SwipeButton;
import com.example.swipebutton_library.OnActiveListener;

// Firebase 관련 (체력 감소 기능을 원하시면 import 유지)
// import com.example.alramapp.Database.DataAccess;
// import com.example.alramapp.Database.UserInform;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.database.DatabaseReference;

public class Mission2Activity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "Mission2Activity";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private ProgressBar progressBar;
    private TextView shakeCountText;
    private ImageView petImageView;
    private ImageView bowlImageView;
    private Button giveUpButton;
    private SwipeButton swipeButton; // SwipeButton 변수 선언

    private int shakeCount = 0;
    private final int TOTAL_SHAKES = 50;
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
        setContentView(R.layout.mission2);

        // Firebase 인스턴스 초기화 (체력 감소 기능 사용 시)
        // database = new DataAccess();
        // mAuth = FirebaseAuth.getInstance();

        // UI 요소 연결
        progressBar = findViewById(R.id.missionProgressBar);
        shakeCountText = findViewById(R.id.shakeCountText); // mission2.xml의 ID와 일치해야 함
        petImageView = findViewById(R.id.img_pet);
        bowlImageView = findViewById(R.id.img_bowl);
        giveUpButton = findViewById(R.id.backbtn); // "포기" 버튼 ID
        swipeButton = findViewById(R.id.swipbutton_mission_complete); // SwipeButton ID

        if (progressBar != null) {
            progressBar.setMax(TOTAL_SHAKES);
            progressBar.setProgress(0);
        }
        updateShakeCountText();

        // SwipeButton 초기에는 숨기기 (XML에서 gone으로 설정했어도 중복으로 안전하게)
        if (swipeButton != null) {
            swipeButton.setVisibility(View.GONE);

            // SwipeButton 리스너 설정
            swipeButton.setOnActiveListener(new OnActiveListener() {
                @Override
                public void onActive() {
                    Log.d(TAG, "SwipeButton activated! Navigating to MainActivity.");
                    // 미션 완료 후 센서 리스너가 계속 동작하지 않도록 확실히 해제
                    if (sensorManager != null) {
                        sensorManager.unregisterListener(Mission2Activity.this);
                    }

                    // MainActivity로 돌아가기
                    Intent intent = new Intent(Mission2Activity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish(); // 현재 Mission2Activity 종료
                }
            });
        } else {
            Log.e(TAG, "SwipeButton (swipbutton_mission_complete) not found in layout!");
        }

        // 센서 초기화
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                Toast.makeText(this, "가속도계 센서가 없습니다.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Accelerometer sensor not available.");
                finish();
            }
        } else {
            Toast.makeText(this, "센서 관리자를 가져올 수 없습니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "SensorManager not available.");
            finish();
        }

        // "포기" 버튼 리스너
        if (giveUpButton != null) {
            giveUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!missionCompleted) {
                        // decreaseLife(); // 체력 감소 로직 (필요시 이전 답변 참고하여 추가)
                        Log.d(TAG, "Give up button clicked.");
                        Toast.makeText(Mission2Activity.this, "미션을 포기했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Mission already completed, but give up button was somehow clicked.");
                    }
                    finish();
                }
            });
        }
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
            return;
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
                    Log.d(TAG, "Shake detected! Count: " + shakeCount);
                    updateShakeCountText();
                    if (progressBar != null) {
                        progressBar.setProgress(shakeCount);
                    }

                    if (shakeCount >= TOTAL_SHAKES) {
                        missionCompleted = true; // 미션 완료 플래그 설정
                        if (shakeCountText != null) {
                            shakeCountText.setText("흔들기 미션 완료!");
                        }
                        Log.d(TAG, "Mission Completed!");
                        Toast.makeText(this, "흔들기 미션 완료!", Toast.LENGTH_SHORT).show();

                        if (sensorManager != null) { // 센서 리스너 해제
                            sensorManager.unregisterListener(this);
                            Log.d(TAG, "Mission completed, accelerometer listener unregistered.");
                        }

                        if (petImageView != null) {
                            petImageView.setImageResource(R.drawable.happy_cat);
                        }
                        if (bowlImageView != null) {
                            bowlImageView.setImageResource(R.drawable.full_bowl);
                        }

                        // ★★★ 버튼 상태 변경 로직 활성화 ★★★
                        if (giveUpButton != null) {
                            giveUpButton.setVisibility(View.GONE); // "포기" 버튼 숨기기
                        }
                        if (swipeButton != null) {
                            swipeButton.setVisibility(View.VISIBLE); // 스와이프 버튼 보이기
                        }
                    }
                }
            }
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void updateShakeCountText() {
        if (shakeCountText != null) {
            if (!missionCompleted) {
                shakeCountText.setText(shakeCount + "/" + TOTAL_SHAKES + "회 흔드세요!");
            }
        }
    }

    // 체력 감소 메소드 (필요시 이전 답변 참고하여 여기에 구현)
    /*
    private void decreaseLife() { ... }
    */
}