package com.example.alramapp.Alarm.AlarmAcvitity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
import com.example.alramapp.R;
import com.example.swipebutton_library.OnActiveListener;
import com.example.swipebutton_library.SwipeButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;


public class MissionTouchActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView touchCountText, tv_mission;
    private AlarmDBHelper dbHelper;
    private AlarmData alarmData;
    private DatabaseReference userScoreRef, userLifeRef;


    private SwipeButton swipeButton; // 스와이프 버튼 변수
    private ImageView petImageView;
    private ImageView bowlImageView;
    private Button giveUpButton; // "포기" 버튼
    private int touchCount = 0;
    private int totalCount;
    private String rep, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mission_alarm_page);

        progressBar = findViewById(R.id.missionProgressBar);
        touchCountText = findViewById(R.id.CountText);
        petImageView = findViewById(R.id.img_pet);
        bowlImageView = findViewById(R.id.img_bowl);
        giveUpButton = findViewById(R.id.giveup);
        swipeButton = findViewById(R.id.swipbutton_mission_complete);
        tv_mission = findViewById(R.id.mission);
        giveUpButton = findViewById(R.id.giveup); // 포기 버튼 아이디 변경 시 여기에 반영

        dbHelper = new AlarmDBHelper(this);

        totalCount = getIntent().getIntExtra("condition",15);
        uid = getIntent().getStringExtra("user_uid");
        rep = getIntent().getStringExtra("repeat");
        alarmData = (AlarmData) getIntent().getSerializableExtra("alarmData");

        //파이어베이스 데이터베이스 점수 값 초기화(연결)
        userScoreRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("score");

        //파이어베이스 데이터베이스 라이프 값 초기화(연결)
        userLifeRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("life");

        //sqlLite 알람 아이디값 초기화(연결)



        //초기화면 세팅(미션 종류, 미션 조건 프로그레시브바,텍스트뷰 세팅)
        showMission();

        // 전체 화면 터치 이벤트 감지
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) { // 터치 이벤트만 인식

                    if (touchCount < totalCount) {//조건을 만족할때까지
                        touchCount++;
                        progressBar.setProgress(touchCount);
                        touchCountText.setText(touchCount + "/" + totalCount + " 개 남았어요!");

                        if (touchCount == totalCount) {

                            MissionComplete(); //알람 종료 버튼 등장, 점수 획득

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
                    return true;
                }
                return false;
            }
        });



        // 포기 버튼 기능 (선택사항)

        giveUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveup();
                stopAlarmSound();
                finish(); // 현재 액티비티 종료
            }
        });
    }

    /**
     * 초기 화면 세팅
     */
    private void showMission(){
        tv_mission.setText("화면을 터치해 주세요!");
        //진행 바 초기화
        progressBar.setMax(totalCount);
        progressBar.setProgress(0);

        //진행 문구 초기화
        touchCountText.setText(touchCount + "/" + totalCount + " 개 남았어요!");

        //알람 종료 스와이프 버튼(초기는 GONE상태)
        swipeButton.setVisibility(View.GONE);

    }

    private void MissionComplete(){

        touchCountText.setText("미션 완료!");

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
                    Toast.makeText(MissionTouchActivity.this, "점수가 업데이트 성공", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MissionTouchActivity.this, "점수 업데이트 실패", Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(MissionTouchActivity.this, "포기 선택 라이프 -1", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MissionTouchActivity.this, "라이프 업데이트 실패", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}
