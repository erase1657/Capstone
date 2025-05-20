package com.example.alramapp.Alarm;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.alramapp.R;


public class AlarmMissionView {

    private View root;
    private AlarmData alarmData;

    private View[] missionList;
    private Button[] increaseBtn;
    private Button[] decreaseBtn;
    private EditText[] countEditText;
    private int[] counts;
    private Button saveBtn;     //저장

    private int selectedIndex = 0;

    public AlarmMissionView(View root, AlarmData alarmData){
        this.root = root;
        this.alarmData = alarmData;

        missionList = new View[] {
                root.findViewById(R.id.mission1),
                root.findViewById(R.id.mission2),
                root.findViewById(R.id.mission3),
                root.findViewById(R.id.mission4)
        };

        increaseBtn = new Button[] {
                root.findViewById(R.id.upbtn_mission1),
                root.findViewById(R.id.upbtn_mission2),
                root.findViewById(R.id.upbtn_mission3),
                root.findViewById(R.id.upbtn_mission4)
        };

        decreaseBtn = new Button[] {
                root.findViewById(R.id.downbtn_mission1),
                root.findViewById(R.id.downbtn_mission2),
                root.findViewById(R.id.downbtn_mission3),
                root.findViewById(R.id.downbtn_mission4)
        };

        countEditText = new EditText[] {
                root.findViewById(R.id.et_mission1),
                root.findViewById(R.id.et_mission2),
                root.findViewById(R.id.et_mission3),
                root.findViewById(R.id.et_mission4)
        };
        saveBtn = root.findViewById(R.id.btn_save_mis);

        // 카운트 값을 배열로 초기화
        counts = new int[]{10, 10, 10, 10};  // 원하는 초기값으로 변경
        initListeners();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMissionData();
            }
        });


    }
    // 클릭 이벤트 등록
    private void initListeners() {
        for (int i=0; i<missionList.length; i++) {
            final int index = i;

            // 미션 레이아웃 클릭시 활성화 처리
            missionList[i].setOnClickListener(v -> setActiveItem(index));

            // 증가 버튼
            increaseBtn[i].setOnClickListener(v -> {
                counts[index]++;
                updateCountText(index);
            });

            // 감소 버튼
            decreaseBtn[i].setOnClickListener(v -> {
                if (counts[index] > 0) { // 0 이하 방지
                    counts[index]--;
                    updateCountText(index);
                }
            });
        }
    }
    private void updateCountText(int index) {
        countEditText[index].setText(String.valueOf(counts[index]));
    }

    // 활성화된 아이템 지정 (배경 색 또는 스타일 변경)
    private void setActiveItem(int activeIndex) {
        selectedIndex = activeIndex;
        for (int i=0; i<missionList.length; i++) {
            if (i == activeIndex) {
                // 활성화 (색상은 필요에 따라 drawable 또는 color로 변경)
                missionList[i].getBackground().setTint(Color.parseColor("#5774DD"));
            } else {
                // 비활성화
                missionList[i].getBackground().setTint(Color.parseColor("#2A2828"));
            }
        }
    }

    public void saveMissionData() {
        if (selectedIndex < 0 || selectedIndex >= missionList.length) return; // 방어적 코드

        // 현재 선택된 인덱스 기준으로 값 읽기
        int missionNumber = selectedIndex + 1; // 미션 번호는 인덱스+1로 가정
        int count;
        try {
            count = Integer.parseInt(countEditText[selectedIndex].getText().toString());
        } catch (NumberFormatException e) {
            count = 0; // 또는 적절한 기본값
        }

        // 값 저장
        alarmData.setMis_num(missionNumber);
        alarmData.setMis_count(count);
    }

}