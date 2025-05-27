package com.example.alramapp.Alarm;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.R;

/*
TODO: 1. 미션 완료 시, 진행 바 부분이 사라지고(visibility  = gone 세팅하면 될듯)
          알람 종료 버튼이 나타나게 해야함(swipe 버튼)
      2. 미션 완료 시, 캐릭터가 웃는 모습, 밥이 채워진 이미지로 변해야 함
      3. 피그에는 있지만 "OO이 밥 주기 미션"이라는 타이틀 빠져있음
      4. xml에 보이는것과 달리 앱 실행시 하단 부분 다 잘림(사진을 줄이거나 따로 다이얼로그를 띄우게 하던지 수정)
      5. 흔들기 미션도 구현 -> 앞으로 미션이 추가된다면 따로 클래스를 분리하는것도 고려
*/

public class MissionAlarmActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView touchCountText;
    private int touchCount = 0;
    private int totalCount = 50; // 총 터치 횟수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mission_alarm_page); // XML 파일 이름이 activity_mission.xml이면 이대로 사용

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
