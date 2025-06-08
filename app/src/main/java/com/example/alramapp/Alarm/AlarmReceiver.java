package com.example.alramapp.Alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.Alarm.SQLlite.AlarmData;

import java.util.Calendar;

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

        // ------ [수정] 알람사운드+알림+액티비티 Service로 실행 ---------
        Intent svcIntent = new Intent(context, AlarmSoundService.class);
        svcIntent.putExtra("alarmId", alarmId);
        // 추가 필요한 값(예: repeatDay, 소리 등) 있으면 여기에 putExtra로 전달

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(svcIntent);
        } else {
            context.startService(svcIntent);
        }

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

    /** "없음"/null/빈 → 단발, 그 외는 반복 */
    private boolean isRepeatingAlarm(AlarmData data) {
        String rep = data.getRepeat();
        return rep != null
                && !rep.trim().isEmpty()
                && !rep.trim().equals("없음")
                && !rep.trim().equals("반복 없음");
    }

}