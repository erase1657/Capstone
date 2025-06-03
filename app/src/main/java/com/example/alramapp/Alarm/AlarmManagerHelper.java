package com.example.alramapp.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.example.alramapp.Alarm.AlarmData;

import java.util.Calendar;


public class AlarmManagerHelper {
    // ====== 테스트모드 플래그 =======
    public static final boolean ALARM_TEST_MODE = false; // 테스트: true, 실서비스: false
    public static final int TEST_ALARM_MINUTES = 1;     // 테스트 시 n분 뒤에 알람

    /**
     * 알람 등록 메인 메서드
     */
    public static void register(Context context, AlarmData alarmData) {
        String repeatStr = alarmData.getRepeat();
        if (repeatStr == null || repeatStr.trim().isEmpty() || repeatStr.equals("반복 없음")) {
            registerAlarm(context, alarmData);
        } else if (repeatStr.equals("매일")) {
            registerDailyAlarms(context, alarmData);
        } else {
            registerWeeklyAlarms(context, alarmData);
        }
    }

    /**
     * 단발성 알람 등록
     */
    public static void registerAlarm(Context context, AlarmData alarmData) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;
        requestExactAlarmPermissionIfNeeded(context, am);

        PendingIntent pi = createPendingIntent(context, alarmData, -1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        cal.set(Calendar.MINUTE, alarmData.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DATE, 1);
        }

        Log.d("AlarmManagerHelper", "Register single id=" + alarmData.getId() + " at " + cal.getTime());
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }

    /**
     * 매일 반복 알람 등록
     */
    private static void registerDailyAlarms(Context context, AlarmData alarmData) {
        if (ALARM_TEST_MODE) {
            // 테스트: 1분 뒤 단발
            registerTestAlarm(context, alarmData);
        } else {
            for (int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++) {
                registerWeeklyAlarmForDay(context, alarmData, d);
            }
            Log.d("AlarmManagerHelper", "Registered daily alarms for id=" + alarmData.getId());
        }
    }

    /**
     * 요일별 반복 알람 등록
     */
    private static void registerWeeklyAlarms(Context context, AlarmData alarmData) {
        String repeatStr = alarmData.getRepeat();
        if (repeatStr == null || repeatStr.trim().isEmpty()) {
            registerAlarm(context, alarmData);
            return;
        }
        if (ALARM_TEST_MODE) {
            // 테스트: 1분 뒤 단발
            registerTestAlarm(context, alarmData);
            return;
        }

        String[] days = repeatStr.split(",");
        for (String day : days) {
            int dow = dayOfWeekFromString(day.trim());
            if (dow != -1) {
                registerWeeklyAlarmForDay(context, alarmData, dow);
            }
        }
    }

    /**
     * 특정 요일 알람 등록 (반복)
     */
    private static void registerWeeklyAlarmForDay(Context context, AlarmData alarmData, int dayOfWeek) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;
        requestExactAlarmPermissionIfNeeded(context, am);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        cal.set(Calendar.MINUTE, alarmData.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int today = cal.get(Calendar.DAY_OF_WEEK);
        int diff = dayOfWeek - today;
        if (diff < 0) diff += 7;
        if (diff == 0 && cal.getTimeInMillis() <= System.currentTimeMillis()) {
            diff = 7;
        }
        cal.add(Calendar.DATE, diff);

        PendingIntent pi = createPendingIntent(context, alarmData, dayOfWeek);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }

        Log.d("AlarmManagerHelper",
                "Registered weekly id=" + alarmData.getId() +
                        " day=" + dayOfWeek +
                        " at " + cal.getTime());
    }

    /**
     * 단일 알람 취소
     */
    public static void cancelAlarm(Context context, AlarmData alarmData) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;

        PendingIntent pi = createPendingIntent(context, alarmData, -1);
        am.cancel(pi);
        pi.cancel();

        Log.d("AlarmManagerHelper", "Cancelled single id=" + alarmData.getId());
    }

    /**
     * 반복 알람 취소
     */
    public static void cancelRepeatingAlarms(Context context, AlarmData alarmData) {
        String repeatStr = alarmData.getRepeat();
        if (repeatStr == null || repeatStr.trim().isEmpty() || repeatStr.equals("없음")) {
            cancelAlarm(context, alarmData);
            return;
        }
        if (repeatStr.equals("매일")) {
            for (int d = Calendar.SUNDAY; d <= Calendar.SATURDAY; d++) {
                cancelAlarmForDay(context, alarmData, d);
            }
        } else {
            String[] days = repeatStr.split(",");
            for (String day : days) {
                int dow = dayOfWeekFromString(day.trim());
                if (dow != -1) {
                    cancelAlarmForDay(context, alarmData, dow);
                }
            }
        }
    }

    private static void cancelAlarmForDay(Context context, AlarmData alarmData, int dayOfWeek) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;

        PendingIntent pi = createPendingIntent(context, alarmData, dayOfWeek);
        am.cancel(pi);
        pi.cancel();

        Log.d("AlarmManagerHelper",
                "Cancelled weekly id=" + alarmData.getId() +
                        " day=" + dayOfWeek);
    }

    /**
     * 다음 회차만 스케줄(매일/요일별 반복용)
     * @param repeatDayOfWeek -1=매일, 1~7=요일별
     */
    public static void scheduleNextAfterTrigger(Context context, AlarmData alarmData, int repeatDayOfWeek) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;
        requestExactAlarmPermissionIfNeeded(context, am);

        PendingIntent pi = createPendingIntent(context, alarmData, repeatDayOfWeek);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        cal.set(Calendar.MINUTE, alarmData.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (repeatDayOfWeek == -1) {
            // 매일
            cal.add(Calendar.DATE, 1);
        } else {
            int today = cal.get(Calendar.DAY_OF_WEEK);
            int diff = repeatDayOfWeek - today;
            if (diff <= 0) diff += 7;
            cal.add(Calendar.DATE, diff);
        }

        long triggerAt = cal.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
        Log.d("AlarmManagerHelper",
                "scheduleNext id=" + alarmData.getId() +
                        " repeatDay=" + repeatDayOfWeek +
                        " at=" + cal.getTime());
    }

    /**
     * 테스트용 1분 뒤 단발 등록
     */
    private static void registerTestAlarm(Context context, AlarmData alarmData) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;
        requestExactAlarmPermissionIfNeeded(context, am);

        PendingIntent pi = createPendingIntent(context, alarmData, -1);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, TEST_ALARM_MINUTES);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Log.d("AlarmManagerHelper", "[TEST] register id=" + alarmData.getId() + " at " + now.getTime());
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), pi);
    }

    /**
     * 테스트용 다음 회차 1분 뒤 재등록
     */
    public static void scheduleTestNext(Context context, AlarmData alarmData) {
        AlarmManager am = getAlarmManager(context);
        if (am == null) return;

        PendingIntent pi = createPendingIntent(context, alarmData, -1);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, TEST_ALARM_MINUTES);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        long triggerAt = now.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
        Log.d("AlarmManagerHelper", "[TEST] scheduleNext id=" + alarmData.getId() + " at " + now.getTime());
    }

    //--------------------------------------------------------------------------------------------------

    private static PendingIntent createPendingIntent(Context context,
                                                     AlarmData alarmData,
                                                     int repeatDayOfWeek) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmId",   alarmData.getId());
        intent.putExtra("alarmName", alarmData.getName());
        intent.putExtra("hour",      alarmData.getHour());
        intent.putExtra("minute",    alarmData.getMinute());
        intent.putExtra("mis_on",    alarmData.getMisOn());
        intent.putExtra("mis_num",   alarmData.getMis_num());
        intent.putExtra("mis_cnt",   alarmData.getMis_count());
        intent.putExtra("sound_on",  alarmData.getSoundOn());
        intent.putExtra("sound",     alarmData.getSound());
        intent.putExtra("user_uid",  alarmData.getUserUid());
        if (repeatDayOfWeek != -1) {
            intent.putExtra("repeat_day", repeatDayOfWeek);
        }
        int req = generateRequestCode(alarmData.getId(), repeatDayOfWeek);
        return PendingIntent.getBroadcast(
                context, req, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static int generateRequestCode(long alarmId, int repeatDayOfWeek) {
        if (repeatDayOfWeek == -1) {
            return (int) alarmId;
        } else {
            return (int) (alarmId * 10 + repeatDayOfWeek);
        }
    }

    private static int dayOfWeekFromString(String dayStr) {
        return switch (dayStr) {
            case "일" -> Calendar.SUNDAY;
            case "월" -> Calendar.MONDAY;
            case "화" -> Calendar.TUESDAY;
            case "수" -> Calendar.WEDNESDAY;
            case "목" -> Calendar.THURSDAY;
            case "금" -> Calendar.FRIDAY;
            case "토" -> Calendar.SATURDAY;
            default  -> -1;
        };
    }

    private static AlarmManager getAlarmManager(Context ctx) {
        return (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    }

    private static void requestExactAlarmPermissionIfNeeded(Context context, AlarmManager am) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}