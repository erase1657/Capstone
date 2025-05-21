package com.example.alramapp.Alarm;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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


public class AlarmSetView {

    //알람 생성 인터페이스
    //MyBottomSheetDialog와 다르게 생성만 다룸
    public interface OnAlarmSaveListener {
        void onSave(AlarmData data);
    }
    private View root;
    private AlarmData alarmData;
    private TimePicker timePicker;

    private TextView tv_name, tv_repeat, tv_mission, tv_sound;
    private Button btn_save;
    private SwitchCompat switch_mis, switch_sound;

    private View lable_Mis, lable_Sound;
    private OnAlarmSaveListener saveListener;    // <-- 콜백 객체

    private ThemedToggleButtonGroup repeat;
    private List<String> RepeatList = Arrays.asList("월", "화", "수", "목", "금", "토", "일");


    public AlarmSetView(View root, AlarmData alarmData){
        this.root = root;
        this.alarmData = alarmData;

        switch_sound = root.findViewById(R.id.switch_sound);
        switch_mis = root.findViewById(R.id.switch_mis);
        lable_Mis = root.findViewById(R.id.mission_rable);
        lable_Sound = root.findViewById(R.id.sound_rable);
        tv_name = root.findViewById(R.id.name_alarm);
        btn_save = root.findViewById(R.id.btn_save);
        timePicker = root.findViewById(R.id.timePicker);
        repeat = root.findViewById(R.id.grouprepeat);
        tv_repeat = root.findViewById(R.id.tv_repeat);
        tv_mission = root.findViewById(R.id.tv_mission);
        tv_sound = root.findViewById(R.id.tv_sound);




        //알람 이름 세팅
        tv_name.setText(alarmData.getName());


        // 시간 초기화
        timePicker.setHour(alarmData.getHour());
        timePicker.setMinute(alarmData.getMinute());

        // 초기 요일 UI 세팅
        updateRepeatUIFromData(alarmData.getRepeat());
        // 스위치 초기화

        if (alarmData.getMisOn() && alarmData.getMis_num() == 0) {
            switch_mis.setChecked(false); // 스위치 끔
            alarmData.setMisOn(false);    // 데이터도 반영
            lable_Mis.setAlpha(0.5f);
            lable_Mis.setEnabled(false);
            tv_mission.setText("없음");
        }else{
            switch_mis.setChecked(alarmData.getMisOn());
            lable_Mis.setAlpha(1f);
            lable_Mis.setEnabled(true);
            updateMissionInfo();
        }

        /*switch_mis.setChecked(alarmData.getMisOn());
        switch_sound.setChecked(alarmData.getSoundOn());
*/
        lable_Mis.setAlpha(alarmData.getMisOn() ? 1f : 0.5f);
        lable_Mis.setEnabled(alarmData.getMisOn());
        lable_Sound.setAlpha(alarmData.getSoundOn() ? 1f : 0.5f);
        lable_Sound.setEnabled(alarmData.getSoundOn());

        // 미션, 사운드 텍스트뷰 초기화
        if(alarmData.getMisOn())
            updateMissionInfo();
        else
            tv_mission.setText("없음");

        if(alarmData.getSoundOn())
            updateSoundInfo();
        else
            tv_sound.setText("사운드 미설정 (진동 알람)");

        // 선택된 요일 변경 시 동작
        repeat.setOnSelectListener(btn -> {
            String joinedDays = getRepeatStringFromSelection();
            tv_repeat.setText(joinedDays);
            return Unit.INSTANCE;
        });


        //사운드 스위치
        switch_sound.setOnCheckedChangeListener((btn, isOn) -> {
            alarmData.setSoundOn(isOn);
            if (isOn) {
                updateSoundInfo();
            } else {
                tv_sound.setText("사운드 미설정 (진동 알람)");
            }
            lable_Sound.setAlpha(isOn ? 1f : 0.5f);
            lable_Sound.setEnabled(isOn);
        });


        //미션 스위치
        switch_mis.setOnCheckedChangeListener((btn, isOn) -> {
            alarmData.setMisOn(isOn);
            if (isOn) {
                updateMissionInfo();
            } else {
                tv_mission.setText("없음");
            }
            lable_Mis.setAlpha(isOn ? 1f : 0.5f);
            lable_Mis.setEnabled(isOn);
        });

        btn_save.setOnClickListener(v -> {

            updateAlarmDataFromUI();

            // 로그 및 DB 저장
            Log.d("AlarmSetView", "onSave clicked → " + alarmData);

            if (saveListener != null) {
                saveListener.onSave(alarmData);
            }
        });


    }



    public void setOnAlarmSaveListener(OnAlarmSaveListener listener){
        this.saveListener = listener;
    }


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


    /**
     * 다이얼로그에서 받은 값들을 alarmData객체에 세팅(setter)
     */
    public void updateAlarmDataFromUI() {
        alarmData.setName(tv_name.getText().toString());
        alarmData.setHour(timePicker.getHour());
        alarmData.setMinute(timePicker.getMinute());
        alarmData.setRepeat(getRepeatStringFromSelection());
        alarmData.setIsEnabled(true);  // 기본값
        alarmData.setUserUid(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        alarmData.setMisOn(switch_mis.isChecked());
        alarmData.setSoundOn(switch_sound.isChecked());

        boolean mis_checked = switch_mis.isChecked();
        int misNum = alarmData.getMis_num();

        if (mis_checked && misNum > 0) {
            // 미션 스위치도 켜져 있고 미션 값도 유효한 경우
            alarmData.setMisOn(true);
        } else {
            // 스위치가 꺼져있거나, 켜져 있지만 미션 값이 없는 경우 미션 OFF 처리
            alarmData.setMisOn(false);
        }
    }

    /**
     * alarmData.getRepeat() 같은 문자열을 읽어서
     * 토글버튼 선택 상태를 초기화하고, 텍스트뷰에 표시함
     */
    public void updateRepeatUIFromData(String repeatStr) {
        Set<String> selectedDays = parseRepeatStringToSet(repeatStr);

        List<ThemedButton> buttons = repeat.getButtons();
        for (ThemedButton btn : buttons) {
            String day = btn.getText().toString().trim();
            int id = btn.getId(); // 중요: 버튼의 리소스 ID

            if (selectedDays.contains(day)) {
                repeat.selectButton(id); // ✅ 리소스 ID를 사용
            } else {
                btn.setSelected(false);
                btn.setActivated(false); // 색상 스타일도 리셋
            }
        }

        // 텍스트뷰 갱신
        List<String> ordered = getSelectedDaysInOrder();
        String text = ordered.isEmpty()
                ? "없음"
                : (ordered.size() == RepeatList.size() ? "매일" : TextUtils.join(", ", ordered));
        tv_repeat.setText(text);
    }
    /**
     * 현재 선택된 버튼들을 월~일 순서에 맞게 읽어 리스트로 반환
     * 선택 없으면 빈 리스트 (필요 시 "없음" 처리 분리하여 명확하게)
     */
    public List<String> getSelectedDaysInOrder() {
        List<String> selectedTexts = new ArrayList<>();
        for (ThemedButton btn : repeat.getButtons()) {
            if (btn.isSelected()) {
                selectedTexts.add(btn.getText().toString().trim());
            }
        }

        List<String> orderedSelected = new ArrayList<>();
        for (String day : RepeatList) {
            if (selectedTexts.contains(day)) {
                orderedSelected.add(day);
            }
        }
        return orderedSelected;
    }
    /**
     * 현재 선택된 요일 리스트를 문자열로 반환
     * 선택된 것이 없으면 "없음" 반환
     * 모든 요일이 선택이라면 "매일" 반환
     */
    public String getRepeatStringFromSelection() {
        List<String> selectedDays = getSelectedDaysInOrder();
        if (selectedDays.isEmpty()) {
            return "없음";
        }
        if (selectedDays.size() == RepeatList.size()) {
            return "매일";
        }
        return TextUtils.join(", ", selectedDays);
    }

    /**
     * 문자열 ("월, 화, 수") → Set<String> 변환
     * "없음" 또는 null은 빈 Set 으로 처리
     */
    private Set<String> parseRepeatStringToSet(String repeatStr) {
        if (repeatStr == null || repeatStr.equals("없음")) {
            return new HashSet<>();
        }
        String[] days = repeatStr.split(",\\s*");
        return new HashSet<>(Arrays.asList(days));
    }
}

