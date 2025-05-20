package com.example.alramapp.Alarm;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.alramapp.R;

public class AlarmSoundView {
    private View root;
    private AlarmData alarmData;

    private ListView soundListView;
    private String[] soundList = {"기본 알람", "새소리", "물방울", "전자음", "벨소리"};

    private int selectedIndex = -1;
    public AlarmSoundView(View root, AlarmData alarmData){
        this.root = root;
        this.alarmData = alarmData;




        soundListView = root.findViewById(R.id.soundlist);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.item_soundlist,R.id.sound_name, soundList);
        soundListView.setAdapter(adapter);


        soundListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            selectedIndex = position;
            String selectedSound = soundList[position];
            alarmData.setSound(selectedSound);
        });



    }
    public void saveSoundData() {
        if (selectedIndex >= 0 && selectedIndex < soundList.length) {
            String selectedSound = soundList[selectedIndex];
            alarmData.setSound(selectedSound);
            System.out.println("저장된 사운드: " + selectedSound); // 🔍 로그 확인용
        } else {
            System.out.println("선택된 사운드 없음");
        }
    }

}