package com.example.alramapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swipebutton_library.SwipeButton;

public class MissionActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView touchCountText;
    private int touchCount = 0;
    private int totalCount = 50; // 총 터치 횟수
    private ImageView petImageView;
    private ImageView bowlImageView;
    private Button giveUpButton; // "포기" 버튼
    private SwipeButton completeSwipeButton; // 미션 완료 후 나타날 스와이프 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mission); // XML 파일 이름이 activity_mission.xml이면 이대로 사용

        // UI 요소 연결
        progressBar = findViewById(R.id.missionProgressBar);
        touchCountText = findViewById(R.id.touchCountText);
        petImageView = findViewById(R.id.img_pet);       // mission.xml 에 정의된 고양이 ImageView ID
        bowlImageView = findViewById(R.id.img_bowl);
        giveUpButton = findViewById(R.id.backbtn); // "포기" 버튼 ID
        completeSwipeButton = findViewById(R.id.swipbutton_mission_complete); // XML에 추가한 스와이프 버튼 ID

        if (progressBar != null) {
            progressBar.setMax(totalCount);
            progressBar.setProgress(0);
        }
        updateTouchText();

        if (completeSwipeButton != null) {
            completeSwipeButton.setVisibility(View.GONE);
        }

        // 전체 화면 터치 이벤트 감지
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (touchCount < totalCount) {
                        touchCount++;
                        progressBar.setProgress(touchCount);
                        updateTouchText();

                        if (touchCount == totalCount) {
                            touchCountText.setText("미션 완료!");
                            // 이미지 변경
                            if (petImageView != null) {
                                petImageView.setImageResource(R.drawable.happy_cat); // 준비된 '행복한 고양이' 이미지 리소스명으로 변경
                            }
                            if (bowlImageView != null) {
                                bowlImageView.setImageResource(R.drawable.full_bowl);    // 준비된 '밥이 채워진 밥그릇' 이미지 리소스명으로 변경
                            }
                            // 버튼 상태 변경
                            if (giveUpButton != null) {
                                giveUpButton.setVisibility(View.GONE); // "포기" 버튼 숨기기
                            }
                            if (completeSwipeButton != null) {
                                completeSwipeButton.setVisibility(View.VISIBLE); // 스와이프 버튼 보이기
                            }
                            // 버튼 상태 변경
                            if (giveUpButton != null) {
                                giveUpButton.setVisibility(View.GONE); // "포기" 버튼 숨기기
                            }
                            if (completeSwipeButton != null) {
                                completeSwipeButton.setVisibility(View.VISIBLE); // 스와이프 버튼 보이기
                            }
                        }

                    }
                    return true;
                }
                return false;
            }
        });

        // 포기 버튼 기능 (선택사항)
        Button giveUpButton = findViewById(R.id.backbtn); // 포기 버튼 아이디 변경 시 여기에 반영
        giveUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 현재 액티비티 종료
            }
        });
    }

    private void updateTouchText() {
        touchCountText.setText(touchCount + "/" + totalCount + " 개 남았어요!");
    }
}
