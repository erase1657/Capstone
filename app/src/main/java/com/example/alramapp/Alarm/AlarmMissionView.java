package com.example.alramapp.Alarm;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.example.alramapp.Alarm.SQLlite.AlarmData;
import com.example.alramapp.GuideDialog.SlideAdapter;
import com.example.alramapp.GuideDialog.SlideItem;
import com.example.alramapp.R;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;


public class AlarmMissionView {

    private View root;
    private AlarmData alarmData;
    private Context context;
    private View[] missionList;
    private Button[] increaseBtn;
    private Button[] decreaseBtn;
    private EditText[] countEditText;
    private int[] counts;
    private Button saveBtn;     //저장
    private Button showDialog;
    private int selectedIndex = 0;

    public AlarmMissionView(View root, AlarmData alarmData, Context context){
        this.root = root;
        this.alarmData = alarmData;
        this.context = context;

        missionList = new View[] {
                root.findViewById(R.id.mission1),
                root.findViewById(R.id.mission2),

        };

        increaseBtn = new Button[] {
                root.findViewById(R.id.upbtn_mission1),
                root.findViewById(R.id.upbtn_mission2),

        };

        decreaseBtn = new Button[] {
                root.findViewById(R.id.downbtn_mission1),
                root.findViewById(R.id.downbtn_mission2),

        };

        countEditText = new EditText[] {
                root.findViewById(R.id.et_mission1),
                root.findViewById(R.id.et_mission2),

        };
        saveBtn = root.findViewById(R.id.btn_save_mis);
        showDialog = root.findViewById(R.id.btn_guide);

        //가이드 다이얼로그 표시 버튼
        showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(context); // this는 Activity일 경우
                dialog.setContentView(R.layout.item_guide); // 위에 작성한 XML 파일명
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                ViewPager2 viewPager = dialog.findViewById(R.id.imageViewPager);
                TextView tvTitle = dialog.findViewById(R.id.tv_title);
                TextView tvDescription = dialog.findViewById(R.id.tv_description);
                Button confirmButton = dialog.findViewById(R.id.confirm_button);
                WormDotsIndicator dotsIndicator = dialog.findViewById(R.id.dotpage);

                //다이얼로그별로 이미지, 제목, 설명이 설정된 리스트 slideItems를 만듦
                //추후 수정 필요함
                List<SlideItem> slideItems = new ArrayList<>();
                slideItems.add(new SlideItem(R.drawable.item_guide4, context.getString(R.string.title_mission1), context.getString(R.string.description_mission1)));
                slideItems.add(new SlideItem(R.drawable.mission_guide2, context.getString(R.string.title_mission2), context.getString(R.string.description_mission2)));
                slideItems.add(new SlideItem(R.drawable.mission_guide3, context.getString(R.string.title_mission3), context.getString(R.string.description_mission3)));
                slideItems.add(new SlideItem(R.drawable.mission_guide4, context.getString(R.string.title_mission4), context.getString(R.string.description_mission4)));


                SlideAdapter adapter = new SlideAdapter(slideItems);
                viewPager.setAdapter(adapter);

                // 초기 텍스트 설정(0번째로)
                tvTitle.setText(slideItems.get(0).getTitle());
                tvDescription.setText(slideItems.get(0).getDescription());

                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        tvTitle.setText(slideItems.get(position).getTitle());
                        tvDescription.setText(slideItems.get(position).getDescription());

                    }
                });

                dotsIndicator.attachTo(viewPager);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        // 카운트 값을 배열로 초기화
        counts = new int[]{10, 10};  // 원하는 초기값으로 변경
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
                if(counts[index]<25){
                    counts[index]++;
                }else{
                    Toast.makeText(context, "25개를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                updateCountText(index);
            });

            // 감소 버튼
            decreaseBtn[i].setOnClickListener(v -> {
                if (counts[index] > 10) { // 0 이하 방지
                    counts[index]--;
                    updateCountText(index);
                }else{
                    Toast.makeText(context, "10개 이하로 줄일 수 없습니다.", Toast.LENGTH_SHORT).show();
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
        if (selectedIndex < 0 || selectedIndex >= missionList.length) {
           return;
        }
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