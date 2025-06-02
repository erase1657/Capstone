package com.example.alramapp.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.AlarmActivity; // AlarmActivity import 확인

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    // UI 쪽에서 이 액션으로 변경 통보를 받고, RecyclerView의 스위치를 내립니다.
    public static final String ACTION_ALARM_STATUS_CHANGED
            = "com.example.alramapp.ACTION_ALARM_STATUS_CHANGED";

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. onReceive 호출 및 전달받은 alarmId 확인 로그
        Log.e(TAG, "!!!!!!!! AlarmReceiver onReceive HAS BEEN CALLED !!!!!!!!" +
                " Action: " + intent.getAction() +
                ", Alarm ID: " + intent.getLongExtra("alarmId", -1));

        long alarmId = intent.getLongExtra("alarmId", -1);
        if (alarmId == -1) {
            Log.e(TAG, "onReceive - Invalid alarmId received. Exiting.");
            return;
        }

        // 2. DB에서 AlarmData 조회
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        AlarmData alarmData = dbHelper.getAlarmById(alarmId);

        if (alarmData == null) {
            Log.e(TAG, "onReceive - AlarmData not found for id: " + alarmId + ". Exiting.");
            return;
        }
        Log.d(TAG, "onReceive - Fetched AlarmData: Name=" + alarmData.getName() +
                ", Enabled=" + alarmData.getIsEnabled() +
                ", Repeat=" + alarmData.getRepeat());

        // 3. 이미 비활성화된 알람이면 스킵 (단, 알람이 울린 직후 비활성화될 수 있으므로, 화면 표시는 우선적으로)
        //    화면 표시 후 상태 변경 로직에서 isEnabled 상태를 업데이트합니다.
        //    만약 알람이 울리기 전에 이미 isEnabled가 false라면 아래 로직은 타지 않습니다.
        if (!alarmData.getIsEnabled() && !"com.example.alramapp.ACTION_SKIP_ENABLED_CHECK".equals(intent.getAction())) { // 특별한 액션이 아니면 체크
            Log.d(TAG, "onReceive - Alarm id=" + alarmId + " is already disabled in DB. Skipping display/reschedule.");
            return;
        }


        // 4. AlarmActivity 시작을 위한 Intent 생성 및 데이터 추가
        Log.d(TAG, "onReceive - Preparing to start AlarmActivity for alarm: " + alarmData.getName());
        Intent alarmActivityIntent = new Intent(context, AlarmActivity.class);

        alarmActivityIntent.putExtra("ALARM_ID", alarmData.getId());
        alarmActivityIntent.putExtra("ALARM_NAME", alarmData.getName());
        alarmActivityIntent.putExtra("ALARM_HOUR", alarmData.getHour());
        alarmActivityIntent.putExtra("ALARM_MINUTE", alarmData.getMinute());
        alarmActivityIntent.putExtra("ALARM_SOUND_NAME", alarmData.getSound()); // 알람 사운드 이름 추가
        // 필요하다면 다른 정보도 추가 (예: 사운드, 미션 정보 등)
        // alarmActivityIntent.putExtra("ALARM_SOUND", alarmData.getSound());

        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 5. AlarmActivity 시작
        Log.d(TAG, "onReceive - Starting AlarmActivity with Name: " + alarmData.getName() +
                ", Time: " + alarmData.getHour() + ":" + alarmData.getMinute());
        try {
            context.startActivity(alarmActivityIntent);
            Log.d(TAG, "onReceive - startActivity for AlarmActivity called successfully.");
        } catch (Exception e) {
            Log.e(TAG, "onReceive - Error starting AlarmActivity: " + e.getMessage(), e);
            // Activity 시작에 실패하면 앱이 꺼질 수 있습니다. Logcat을 확인하세요.
            return; // 액티비티 시작 실패 시 더 이상 진행하지 않음
        }

        // 6. 단발성 알람 처리: isEnabled = false로 업데이트 후 브로드캐스트
        if (!isRepeatingAlarm(alarmData)) {
            Log.d(TAG, "onReceive - Handling one-shot alarm id=" + alarmId);
            alarmData.setIsEnabled(false); // 여기서 상태를 변경
            int updated = dbHelper.updateAlarm(alarmData); // 변경된 상태를 DB에 반영
            Log.d(TAG, "onReceive - Disabled one-shot alarm id=" + alarmId + " in DB, rows updated=" + updated);

            Intent statusIntent = new Intent(ACTION_ALARM_STATUS_CHANGED);
            statusIntent.putExtra("alarmId", alarmId);
            context.sendBroadcast(statusIntent);
            Log.d(TAG, "onReceive - Sent ACTION_ALARM_STATUS_CHANGED broadcast for id=" + alarmId);
        }
        // 7. 반복 알람 처리: 다음 알람 시간 재등록
        else {
            Log.d(TAG, "onReceive - Handling repeating alarm id=" + alarmId);
            String rep = alarmData.getRepeat().trim();
            // 'repeat_day'는 AlarmManagerHelper에서 PendingIntent 생성 시 함께 전달된 값을 사용합니다.
            int repeatDay = intent.getIntExtra("repeat_day", -1);

            if (rep.equals("매일")) {
                // 매일 반복 알람의 경우, AlarmManagerHelper.scheduleNextAfterTrigger는 내부적으로 다음 날로 설정합니다.
                // repeatDay 값을 -1 (또는 다른 특별한 값)로 전달하여 구분합니다.
                AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, -1); // '매일'을 나타내는 특별한 값으로 -1 사용
                Log.d(TAG, "onReceive - Rescheduled 'Daily' repeating alarm for id=" + alarmId);
            } else { // 요일별 반복
                Log.d(TAG, "onReceive - Rescheduling 'Weekly' repeating alarm for id=" + alarmId + ", repeat_day from intent: " + repeatDay);
                if (repeatDay >= Calendar.SUNDAY && repeatDay <= Calendar.SATURDAY) {
                    AlarmManagerHelper.scheduleNextAfterTrigger(context, alarmData, repeatDay);
                } else {
                    Log.w(TAG, "onReceive - Invalid repeat_day (" + repeatDay + ") for weekly repeating alarm. Not rescheduling for id=" + alarmId);
                }
            }
        }
        Log.d(TAG, "onReceive - FINISHED for alarmId: " + alarmId);
    }

    /**
     * repeat 필드를 기준으로 반복 알람인지 단발성 알람인지 구분합니다.
     * "반복 없음", null, 빈 문자열은 단발성으로 간주합니다.
     * @param data 확인할 AlarmData 객체
     * @return 반복 알람이면 true, 단발성이면 false
     */
    private boolean isRepeatingAlarm(AlarmData data) {
        if (data == null || data.getRepeat() == null) {
            return false;
        }
        String rep = data.getRepeat().trim();
        // "매일" 또는 "월,화,수" 등 요일 문자열이 있으면 반복으로 간주
        return !rep.isEmpty() && !"반복 없음".equals(rep);
    }
}