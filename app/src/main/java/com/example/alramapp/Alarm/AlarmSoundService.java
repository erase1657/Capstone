package com.example.alramapp.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.alramapp.Alarm.AlarmAcvitity.DefaultAlarmActivity;
import com.example.alramapp.Alarm.AlarmAcvitity.MissionAlarmActivity;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.Alarm.SQLlite.AlarmData;
import com.example.alramapp.R;

public class AlarmSoundService extends Service {
    public static final String ACTION_STOP = "com.example.alramapp.action.STOP";
    private static final String CHANNEL_ID = "alarm_channel";
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long alarmId = intent.getLongExtra("alarmId", -1);


        AlarmDBHelper dbHelper = new AlarmDBHelper(this);
        AlarmData alarmData = dbHelper.getAlarmById(alarmId);
        boolean alarmType = alarmData.getMisOn();

        String soundName = alarmData.getSound();

        int soundResId = 0;

        switch (soundName) {
            case "샘플 알람음1": soundResId = R.raw.sample1; break;
            case "샘플 알람음2": soundResId = R.raw.sample2; break;
            case "샘플 알람음3": soundResId = R.raw.sample3; break;
            default:
                // 진동 구현
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {0, 1000, 500, 1000, 500}; // {대기, on, off, on, off ...}
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0); // 0: 반복, -1: 한 번
                    vibrator.vibrate(effect);
                } else {
                    vibrator.vibrate(pattern, 0);
                }
                break;
        }
        // MediaPlayer로 반복재생
        if(soundResId != 0){
            mediaPlayer = MediaPlayer.create(this, soundResId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        // ==== Notification (필수, 고정!) =====
        createNotificationChannel();

        Intent alarmIntent;
        if (alarmType) {
            //미션 알람 실행
            alarmIntent = new Intent(this, MissionAlarmActivity.class);
        } else {
            // 일반 알람 실행
            alarmIntent = new Intent(this, DefaultAlarmActivity.class);
        }

        alarmIntent.putExtra("alarmId", alarmId);

        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingActivity = PendingIntent.getActivity(
                this, (int)alarmId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cat_sleeping)
                .setContentTitle(alarmData.getName())
                .setContentText("⏰ 알람 시간이 되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true) // 고정! 슬라이드로 종료 불가
                .setFullScreenIntent(pendingActivity, true);

        // 서비스 포그라운드로 시작
        startForeground((int) alarmId, builder.build());

        return START_NOT_STICKY;

    }
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager nm = getSystemService(NotificationManager.class);
            NotificationChannel ch = nm.getNotificationChannel(CHANNEL_ID);
            if (ch == null) {
                ch = new NotificationChannel(
                        CHANNEL_ID, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH);
                ch.setDescription("알람 채널");
                ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("AlarmSoundService","Ondestroy호출");
        super.onDestroy();
        stopForeground(true);

        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(vibrator != null) {
            vibrator.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
