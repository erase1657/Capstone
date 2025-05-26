package com.example.alramapp.Alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;

import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {
    // UI 쪽에서 이 액션으로 변경 통보를 받고, RecyclerView의 스위치를 내립니다.
    public static final String ACTION_ALARM_STATUS_CHANGED
            = "com.example.alramapp.ACTION_ALARM_STATUS_CHANGED";

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "onReceive called!! alarmId=" + intent.getLongExtra("alarmId", -1));
        long alarmId = intent.getLongExtra("alarmId", -1);
        if (alarmId == -1) {
            Log.d(TAG, "Invalid alarmId received");
            return;
        }

        // 1) DB에서 AlarmData 조회
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        AlarmData alarmData = dbHelper.getAlarmById(alarmId);
        if (alarmData == null) {
            Log.d(TAG, "AlarmData not found for id=" + alarmId);
            return;
        }

        // 이미 꺼진 알람이면 스킵
        if (!alarmData.getIsEnabled()) {
            Log.d(TAG, "Alarm id=" + alarmId + " is disabled. Skip.");
            return;
        }

        // 2) 실제 알람 화면 띄우기
        startAlarmActivity(context, intent);

        // 3) 단발성 알람이면 isEnabled=false로 업데이트 후 브로드캐스트
        if (!isRepeatingAlarm(alarmData)) {
            alarmData.setIsEnabled(false);
            int updated = dbHelper.updateAlarm(alarmData);
            Log.d(TAG, "Disabled one-shot alarm id=" + alarmId + ", rows=" + updated);

            Intent statusIntent = new Intent(ACTION_ALARM_STATUS_CHANGED);
            statusIntent.putExtra("alarmId", alarmId);
            context.sendBroadcast(statusIntent);
        }

        // 4) 반복 알람이라면 다음 회차 재등록
        else {
            String rep = alarmData.getRepeat().trim();
            // 매일 반복이면 repeatDay=-1
            if (rep.equals("매일")) {
                AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, -1);
            }
            // 요일별 반복이면 intent 에서 repeat_day extra 로 받아오고
            else {
                int repeatDay = intent.getIntExtra("repeat_day", -1);
                if (repeatDay >= Calendar.SUNDAY && repeatDay <= Calendar.SATURDAY) {
                    AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, repeatDay);
                }
            }
        }
    }

    private void startAlarmActivity(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, DefaultAlarmActivity.class);
        alarmIntent.putExtras(Objects.requireNonNull(intent.getExtras()));
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alarmIntent);
    }

    /**
     * repeat 필드가 없는 경우(false→단발성, true→반복성)
     * "없음", null, 빈 문자열은 단발성으로 간주
     */
    private boolean isRepeatingAlarm(AlarmData data) {
        String rep = data.getRepeat();
        return rep != null && !rep.trim().isEmpty() && !"없음".equals(rep.trim());
    }
}