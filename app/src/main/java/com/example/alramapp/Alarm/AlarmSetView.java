package com.example.alramapp.Alarm;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.SwitchCompat;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import kotlin.Unit;
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;

//요일 설정, 시간 설정
//미션, 사운드 설정페이지에서 설정한 값들 textView 갱신
public class AlarmSetView {

    //알람 생성 인터페이스
    //MyBottomSheetDialog와 다르게 생성만 다룸
    public interface OnAlarmSaveListener {
        void onSave(AlarmData data);
    }
    private View root;
    private AlarmData alarmData;
    private TimePicker timePicker;

    private TextView tv_name, tv_day, tv_mission, tv_sound;
    private Button btn_save;
    private OnAlarmSaveListener saveListener;    // <-- 콜백 객체

    private ThemedToggleButtonGroup repeat;
    private List<String> dayList = Arrays.asList("월", "화", "수", "목", "금", "토", "일");


    public AlarmSetView(View root, AlarmData alarmData){
        this.root = root;
        this.alarmData = alarmData;


        tv_name = root.findViewById(R.id.name_alarm);
        btn_save = root.findViewById(R.id.btn_save);

        timePicker = root.findViewById(R.id.timePicker);
        repeat = root.findViewById(R.id.tags);
        tv_day = root.findViewById(R.id.day);
        tv_mission = root.findViewById(R.id.tv_mission);
        tv_sound = root.findViewById(R.id.tv_sound);

        // 기존 데이터가 있으면 UI에 세팅하기
        timePicker.setHour(alarmData.getHour());
        timePicker.setMinute(alarmData.getMinute());
        tv_name.setText(alarmData.getName());

        // repeat 버튼은 alarmData.getRepeat() 문자열로부터 선택된 요일 파싱 후 토글 상태 변경
        String repeatStr = alarmData.getRepeat();
        if (repeatStr != null && !repeatStr.equals("없음")) {
            // 예: "월, 화, 수" → Set<String> selectedDays
            Set<String> selectedDays = new HashSet<>(Arrays.asList(repeatStr.split(", ")));
            for (int i = 0; i < repeat.getChildCount(); i++) {
                ThemedButton btn = (ThemedButton) repeat.getChildAt(i);
                btn.setSelected(selectedDays.contains(btn.getText()));
            }
            tv_day.setText(repeatStr);
        } else {
            tv_day.setText("없음");
        }

        //선택된 요일 체크 리스너
        repeat.setOnSelectListener((ThemedButton btn) -> {
            List<String> selectedDays = getSelectedDaysInOrder();
            String joinedDays = TextUtils.join(", ", selectedDays);
            tv_day.setText(joinedDays);

            return Unit.INSTANCE;
        });


        btn_save.setOnClickListener(v -> {
            alarmData.setName(tv_name.getText().toString());
            alarmData.setHour(timePicker.getHour());
            alarmData.setMinute(timePicker.getMinute());
            alarmData.setRepeat(TextUtils.join(", ", getSelectedDaysInOrder()));
            alarmData.setIsEnabled(true);  // 기본값
            alarmData.setUserUid(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

            // 로그 및 DB 저장
            Log.d("AlarmSetView", "onSave clicked → " + alarmData);

            if (saveListener != null) {
                saveListener.onSave(alarmData);
            }
        });


    }

    // 선택된 요일 리스트를 월~일 순서대로 반환하는 메서드
    public List<String> getSelectedDaysInOrder(){

        Set<String> sel = new HashSet<>();
        for(ThemedButton b: repeat.getSelectedButtons()){
            sel.add(b.getText());
        }
        List<String> out = new ArrayList<>();
        for(String d: dayList){
            if (sel.contains(d)) out.add(d);
        }
        if (out.isEmpty()) out.add("없음");
        return out;
    }

    public void setOnAlarmSaveListener(OnAlarmSaveListener listener){
        this.saveListener = listener;
    }


    //set페이지에 미션 정보를 텍스트뷰에 갱신
    public void updateMissionInfo() {
        int num = alarmData.getMis_num();
        int count = alarmData.getMis_count();
        if (num > 0) {
            tv_mission.setText("미션 " + num + "번 - " + count + "회");
        } else {
            tv_mission.setText("미션 미설정");
        }
    }

    //set페이지에 사운드 정보를 텍스트뷰에 갱신
    public void updateSoundInfo() {
        String selectedSound = alarmData.getSound();
        if (selectedSound != null && !selectedSound.isEmpty()) {
            tv_sound.setText(selectedSound);  // soundLabel은 사운드를 표시할 TextView
        }else{
            tv_sound.setText("사운드 미설정 (진동 알람)");
        }
    }

    public void updateAlarmDataFromUI() {
        alarmData.setName(tv_name.getText().toString());
        alarmData.setHour(timePicker.getHour());
        alarmData.setMinute(timePicker.getMinute());
        alarmData.setRepeat(TextUtils.join(", ", getSelectedDaysInOrder()));
        // 추가 필드들도 세팅ㄷ
    }
}

