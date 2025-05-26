package com.example.alramapp.Alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alramapp.Alarm.RecyclerView.SoundAdapter;
import com.example.alramapp.R;

import java.util.Arrays;
import java.util.List;

public class AlarmSoundView {
    private View root;
    private AlarmData alarmData;

    private RecyclerView soundList;
    private SoundAdapter soundAdapter;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String PREF_KEY_SELECTED_SOUND = "selected_sound";
    private String currentSelectedSoundName = null;

    public AlarmSoundView(View root, AlarmData alarmData) {
        this.root = root;
        this.alarmData = alarmData;
       /* this.vibrator = (Vibrator) root.getContext().getSystemService(Context.VIBRATOR_SERVICE);*/
        soundList = root.findViewById(R.id.soundlist);

        List<String> soundlist = Arrays.asList(
                "사운드 미설정(진동 알람)",
                "샘플 알람음1",
                "샘플 알람음2",
                "샘플 알람음3"
        );

        soundAdapter = new SoundAdapter(root.getContext(), soundlist, soundName -> {
            if (soundName != null && soundName.equals(currentSelectedSoundName)) {
                // 같은 사운드를 다시 클릭한 경우 → 재생 중지, 배경 해제
                stopSound();
                alarmData.setSound(null);
                currentSelectedSoundName = null;
                soundAdapter.setSelectedSound(null); // 어댑터에 선택 해제 알리기
            } else {
                // 새로운 사운드 선택 시
                alarmData.setSound(soundName);
                currentSelectedSoundName = soundName;
                saveSoundData();
                playSound(soundName);

                soundAdapter.setSelectedSound(soundName);  // 어댑터에 선택 표시 알리기
            }
        });

        soundList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        soundList.setAdapter(soundAdapter);

        // 저장된 사운드 불러와서 초기 선택 및 UI 반영
        loadSoundData();
    }

    private void playSound(String soundName) {
        // 이전에 재생 중이면 중지 및 해제
        stopSound();

        if (soundName == null) {
            // null이면 재생하지 않음
            return;
        }
        /*if ("사운드 미설정(진동 알람)".equals(soundName)) {
            // 진동만 울리기
            vibrate();
            // vibratePattern(); // 패턴 진동을 원한다면!
            return;
        }*/

        int soundResId = getSoundResId(soundName);
        if (soundResId != 0) {
            mediaPlayer = MediaPlayer.create(root.getContext(), soundResId);
            mediaPlayer.setLooping(true);  // 반복 재생 설정
            mediaPlayer.start();
        }
        // 사운드 미설정 등 0일 때는 재생 안 함
    }

    public void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
  /*      if (vibrator != null) {
            vibrator.cancel();  // 진동 중지!
        }*/
    }
   /* private void vibrate() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)); // 2초 진동
        } else {
            vibrator.vibrate(2000); // Deprecated for old devices
        }
    }*/

    private int getSoundResId(String soundName) {
        if (soundName == null) {
            return 0; // 재생하지 않음
        }
        return switch (soundName) {
            case "샘플 알람음1" -> R.raw.sample1; // res/raw/sample1.mp3 (파일명에 맞게 변경)
            case "샘플 알람음2" -> R.raw.sample2;
            case "샘플 알람음3" -> R.raw.sample3;
            default -> 0; // 재생하지 않음, 예: "사운드 미설정(진동 알람)"
        };
    }

    public void saveSoundData() {
        if (alarmData == null || alarmData.getSound() == null) {
            return;
        }

        SharedPreferences prefs = root.getContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY_SELECTED_SOUND, alarmData.getSound());
        editor.apply();
    }

    public void loadSoundData() {
        SharedPreferences prefs = root.getContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedSound = prefs.getString(PREF_KEY_SELECTED_SOUND, null);

        if (savedSound != null) {
            alarmData.setSound(savedSound);
            currentSelectedSoundName = savedSound;
            soundAdapter.setSelectedSound(savedSound);
        }
    }

    /** 미디어 플레이어 리소스 해제용 메서드 **/
    public void release() {
        stopSound();
    }
}