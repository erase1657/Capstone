package com.example.alramapp;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MissionActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView touchCountText;
    private int touchCount = 0;
    private int totalCount = 50; // 총 터치 횟수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mission); // XML 파일 이름이 activity_mission.xml이면 이대로 사용

        // UI 요소 연결
        progressBar = findViewById(R.id.missionProgressBar);
        touchCountText = findViewById(R.id.touchCountText);

        progressBar.setMax(totalCount);
        progressBar.setProgress(0);
        updateTouchText();

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
