package com.example.alramapp.Alarm;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.alramapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.checkerframework.checker.nullness.qual.NonNull;


public class MyBottomSheetDialog extends BottomSheetDialogFragment {

    //알람 업데이트, 삭제 인터페이스
    //AlaramSetView와 다르게 생성만 다룸
    public interface OnSaveListener {
        void onSave(AlarmData data);
        void onUpdate(AlarmData data);  // 알람 수정
        void onDelete(AlarmData data);  // 알람 삭제
    }
    private OnSaveListener listener;
    private FrameLayout container;
    private View setView, soundView, missionView;
    private AlarmData alarmData;

    private AlarmSetView alarmSetView;
    private AlarmSoundView alarmSoundView;
    private AlarmMissionView alarmMissionView;
    private SwitchCompat switch_mis, switch_sound;

    private static final String ARG_ALARM = "alarm_arg";

    public static MyBottomSheetDialog newInstance(AlarmData alarmData) {
        MyBottomSheetDialog dialog = new MyBottomSheetDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALARM, alarmData);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSaveListener) {
            listener = (OnSaveListener) context;
        } else {
            throw new RuntimeException(
                    "호스트 Activity가 MyBottomSheetDialog.OnSaveListener를 구현해야 합니다");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ALARM)) {
            alarmData = (AlarmData) getArguments().getSerializable(ARG_ALARM);
        } else {
            alarmData = new AlarmData();  // 새 알람 생성
        }


        View root = inflater.inflate(R.layout.bottomsheetdialog, container, false);
        this.container = root.findViewById(R.id.container);

        // 페이지별 레이아웃 inflate(최초 한 번만)
        setView = inflater.inflate(R.layout.fragment_alarm_set, this.container, false);
        soundView = inflater.inflate(R.layout.fragment_alarm_sound, this.container, false);
        missionView = inflater.inflate(R.layout.fragment_alarm_mission, this.container, false);


        Button deleteButton = setView.findViewById(R.id.btn_delete);
        Button btnSave = setView.findViewById(R.id.btn_save);
        switch_mis = setView.findViewById(R.id.switch_mis);
        switch_sound = setView.findViewById(R.id.switch_sound);


        //수정때만 삭제 버튼이 보이게 설정
        deleteButton.setVisibility(alarmData.getId() != 0 ? View.VISIBLE : View.GONE);

        //취소 버튼 클릭시 다이얼로그 닫음
        setView.findViewById(R.id.btn_close).setOnClickListener(v-> dismiss());

        //뒤로가기 버튼 클릭시 해당 페이지로 전환(alarm_set.xml)
        soundView.findViewById(R.id.btn_back).setOnClickListener(v -> switchView(setView,-1));
        missionView.findViewById(R.id.btn_back).setOnClickListener(v -> switchView(setView,-1));


        alarmSetView = new AlarmSetView(setView, alarmData);
        alarmSoundView = new AlarmSoundView(soundView, alarmData);
        alarmMissionView = new AlarmMissionView(missionView, alarmData,requireContext());



        // 최초엔 메인화면(alarm_set.xml)
        switchView(setView,0);



        alarmSetView.setOnAlarmSaveListener(data -> {
            listener.onSave(data);
            dismiss();
        });



        // 스위치가 켜졌을때 각 레이블 클릭시 해당 페이지로 전환(alarm_mission.xml, alarm_sound.xml)
        setView.findViewById(R.id.mission_rable)
                .setOnClickListener(v -> {
                    if ( switch_mis.isChecked() ) {
                        // 미션 스위치가 ON 일 때만 이동
                        switchView(missionView, 1);
                    }
                });

        setView.findViewById(R.id.sound_rable)
                .setOnClickListener(v -> {
                    if ( switch_sound.isChecked() ) {
                        // 사운드 스위치가 ON 일 때만 이동
                        switchView(soundView, 1);
                    }
                });



        //미션 페이지에서 확인버튼 클릭시 값을 저장하고 set페이지로 이동
        missionView.findViewById(R.id.btn_save_mis).setOnClickListener(v -> {
            alarmMissionView.saveMissionData();
            alarmSetView.updateMissionInfo();
            switchView(setView, -1);
        });



        //사운드 페이지에서 확인버튼 클릭시 값을 저장하고 set페이지로 이동
        soundView.findViewById(R.id.save_sound).setOnClickListener(v -> {

            alarmSoundView.saveSoundData();
            alarmSetView.updateSoundInfo();
            switchView(setView, -1);

        });


        //저장 버튼클릭 알람의 id(아이템별)값에 따라 저장 또는 수정
        btnSave.setOnClickListener(v -> {

            alarmSetView.updateAlarmDataFromUI();

            if (alarmData.getId() == 0) {
                listener.onSave(alarmData); // 새 알람 삽입
            } else {
                listener.onUpdate(alarmData); // 수정
            }
            dismiss();

        });
        //삭제 버튼
        deleteButton.setOnClickListener(v -> {
            if (listener != null && alarmData.getId() != 0) {
                listener.onDelete(alarmData);
                dismiss();
            }
        });



        return root;
    }





    //아래부터는 페이지 전환, 등장 애니메이션을 다룸(따로 건들일 필요 없음)


    //바텀시트 다이얼로그가 나타나는 모습
    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);

            parent.setBackgroundColor(Color.TRANSPARENT);

            //설정 페이지가 나타날때의 애니메이션
            view.setTranslationY(1000);
            view.animate()
                    .translationY(0)
                    .setDuration(800) // 시간 조절
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // 바닥시트의 높이를 화면 전체로 맞춤
            parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.requestLayout();

            // 확장 상태로 무조건 보이게
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            // 스와이프해서 내려갈 때 완전히 내려가도록
            behavior.setSkipCollapsed(true);
        }
    }

    //바텀시트 다이얼로그 안의 여러 뷰들이 서로 전환될때의 모습
    private void switchView(View newView, int direction) {
        int width = container.getWidth();
        if (width == 0) width = container.getResources().getDisplayMetrics().widthPixels;

        if (container.getChildCount() > 0) {
            View currentView = container.getChildAt(0);

            // direction: 1 => 오른쪽으로 이동
            // direction: -1 => 왼쪽으로 이동

            currentView.animate()
                    .translationX(-width * direction)
                    .setDuration(200)
                    .withEndAction(() -> container.removeView(currentView))
                    .start();

            newView.setTranslationX(width * direction);
            container.addView(newView);

            newView.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();

            newView.clearFocus();
        } else {
            container.addView(newView);
        }
    }

}