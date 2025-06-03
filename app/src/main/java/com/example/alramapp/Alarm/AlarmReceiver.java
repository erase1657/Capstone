package com.example.alramapp.Alarm;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.R;

import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_STATUS_CHANGED
            = "com.example.alramapp.ACTION_ALARM_STATUS_CHANGED";
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmId   = intent.getLongExtra("alarmId", -1);
        int  repeatDay = intent.getIntExtra("repeat_day", -1);
        Log.d(TAG, "onReceive() id=" + alarmId + " repeatDay=" + repeatDay);

        if (alarmId == -1) {
            Log.w(TAG, "Invalid alarmId");
            return;
        }

        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        AlarmData alarmData = dbHelper.getAlarmById(alarmId);
        if (alarmData == null) {
            Log.w(TAG, "No AlarmData for id=" + alarmId);
            return;
        }
        if (!alarmData.getIsEnabled()) {
            Log.d(TAG, "Alarm id=" + alarmId + " is disabled. Skip.");
            return;
        }

        // 1) 알람 화면 띄우기
        showAlarmNotification(context, alarmData);

        // 2) 반복성 여부
        boolean isRepeat = isRepeatingAlarm(alarmData);

        // ★ 테스트 모드: 반복 알람(매일/요일) 모두 1분 뒤 재등록
        if (AlarmManagerHelper.ALARM_TEST_MODE && isRepeat) {
            AlarmManagerHelper.scheduleTestNext(context, alarmData);
            return;
        }

        // 3) 실제 서비스 모드 재등록
        if (isRepeat) {
            String rep = alarmData.getRepeat().trim();
            if (rep.equals("매일")) {
                AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, -1);
            } else {
                if (repeatDay >= Calendar.SUNDAY && repeatDay <= Calendar.SATURDAY) {
                    AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, repeatDay);
                } else {
                    Log.w(TAG, "Invalid repeatDay=" + repeatDay);
                }
            }
        }
        // 4) 단발성 알람: 비활성 처리 → DB 업데이트 → UI 브로드캐스트
        else {
            alarmData.setIsEnabled(false);
            int updated = dbHelper.updateAlarm(alarmData);
            Log.d(TAG, "Disabled one-shot id=" + alarmId + ", rows=" + updated);

            Intent status = new Intent(ACTION_ALARM_STATUS_CHANGED);
            status.putExtra("alarmId", alarmId);
            context.sendBroadcast(status);
        }
    }

    private void startAlarmActivity(Context context, Intent intent) {
        Intent i = new Intent(context, DefaultAlarmActivity.class);
        i.putExtras(Objects.requireNonNull(intent.getExtras()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    /** "없음"/null/빈 → 단발, 그 외는 반복 */
    private boolean isRepeatingAlarm(AlarmData data) {
        String rep = data.getRepeat();
        return rep != null
                && !rep.trim().isEmpty()
                && !rep.trim().equals("없음")
                && !rep.trim().equals("반복 없음");
    }
    private void showAlarmNotification(Context ctx, AlarmData data) {
        String channelId = "alarm_channel";
        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1) 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = nm.getNotificationChannel(channelId);
            if (ch == null) {
                ch = new NotificationChannel(
                        channelId,
                        "Alarm Channel",
                        NotificationManager.IMPORTANCE_HIGH
                );
                ch.setDescription("알람용 채널");
                ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }
        }

        // 2) 풀스크린 인텐트 PendingIntent
        Intent fullIntent = new Intent(ctx, DefaultAlarmActivity.class);
        fullIntent.putExtra("alarmId", data.getId());
        fullIntent.putExtra("repeat_day", data.getRepeat());
        fullIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );
        PendingIntent fullPI = PendingIntent.getActivity(
                ctx,
                (int)data.getId(),
                fullIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3) Notification 빌드
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.cat_sleeping)
                .setContentTitle(data.getName())
                .setContentText("⏰ 알람 시간이 되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullPI, true);

        // 4) 알림 표시
        nm.notify((int)data.getId(), b.build());
    }
}