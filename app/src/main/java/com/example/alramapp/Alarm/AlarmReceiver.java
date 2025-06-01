package com.example.alramapp.Alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.AlarmActivity;

import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {
    // UI 쪽에서 이 액션으로 변경 통보를 받고, RecyclerView의 스위치를 내립니다.
    public static final String ACTION_ALARM_STATUS_CHANGED
            = "com.example.alramapp.ACTION_ALARM_STATUS_CHANGED";

    private static final String TAG = "AlarmReceiver";

    // AlarmReceiver.java 의 onReceive 메소드 내부 (일부)

    // AlarmReceiver.java 의 onReceive 메소드 내부 (일부)

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
        AlarmData alarmData = dbHelper.getAlarmById(alarmId); //
        if (alarmData == null) {
            Log.d(TAG, "AlarmData not found for id=" + alarmId);
            return;
        }

        // 이미 꺼진 알람이면 스킵
        if (!alarmData.getIsEnabled()) {
            Log.d(TAG, "Alarm id=" + alarmId + " is disabled. Skip.");
            return;
        }

        // 2) 실제 알람 화면 띄우기 (startAlarmActivity 메소드 호출 또는 직접 구현)
        // Intent 생성 및 데이터 추가
        Intent alarmActivityIntent = new Intent(context, AlarmActivity.class); // 시작할 액티비티를 AlarmActivity.class로 설정

        // AlarmData 객체에서 필요한 정보 추출하여 Intent에 담기
        alarmActivityIntent.putExtra("ALARM_ID", alarmData.getId());
        alarmActivityIntent.putExtra("ALARM_NAME", alarmData.getName()); //
        alarmActivityIntent.putExtra("ALARM_HOUR", alarmData.getHour()); //
        alarmActivityIntent.putExtra("ALARM_MINUTE", alarmData.getMinute()); //
        // 필요에 따라 다른 정보들도 추가할 수 있습니다. (예: 반복 정보, 미션 정보 등)
        // alarmActivityIntent.putExtra("ALARM_REPEAT", alarmData.getRepeat());

        // 기존 intent의 extra들을 모두 넘기고 싶다면 아래 코드를 사용할 수 있으나,
        // 명시적으로 필요한 데이터만 넘기는 것이 좋습니다.
        // alarmActivityIntent.putExtras(Objects.requireNonNull(intent.getExtras()));

        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(alarmActivityIntent);

        // 3) 단발성 알람 처리 등 (기존 로직 유지)
        // ... (기존 코드 생략) ...
    }

// 기존 startAlarmActivity 메소드는 직접 호출하는 방식으로 변경되었으므로,
// 해당 메소드는 삭제하거나 위의 방식으로 onReceive 내에 통합할 수 있습니다.
/*
private void startAlarmActivity(Context context, Intent intent) {
    // 이 메소드는 이제 onReceive 내에서 직접 처리됩니다.
}
*/

    private void startAlarmActivity(Context context, Intent intent) {
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
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
        return rep != null && !rep.trim().isEmpty() && !"반복 없음".equals(rep.trim());
    }
}