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

    /**
     * 알람 등록 메인 메서드
     * repeat 필드가 "없음" 혹은 빈 값이면 단일 알람 등록,
     * "매일"이면 매일 반복 7개 알람 등록,
     * 그 외 쉼표로 구분된 요일일 경우 해당 요일만 알람 반복 등록
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
        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null) return;

        requestExactAlarmPermissionIfNeeded(context, alarmManager);

        PendingIntent pendingIntent = createPendingIntent(context, alarmData, -1);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        calendar.set(Calendar.MINUTE, alarmData.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 알람 시간이 현재보다 이전이면 다음 날로 변경
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        Log.d("AlarmManagerHelper", "Registering single alarm id=" + alarmData.getId() + " at " + calendar.getTime());

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * 매일 반복 알람 등록 (일요일 ~ 토요일)
     */
    private static void registerDailyAlarms(Context context, AlarmData alarmData) {
        for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; dayOfWeek++) {
            registerWeeklyAlarmForDay(context, alarmData, dayOfWeek);
        }
        Log.d("AlarmManagerHelper", "매일 알람:" + alarmData.getId());
    }

    /**
     * 쉼표로 구분된 요일 반복 알람 등록
     */
    private static void registerWeeklyAlarms(Context context, AlarmData alarmData) {
        String repeatStr = alarmData.getRepeat();
        if (repeatStr == null || repeatStr.trim().isEmpty()) {
            registerAlarm(context, alarmData);
            return;
        }

        String[] days = repeatStr.split(",");
        for (String day : days) {
            int dayOfWeek = dayOfWeekFromString(day.trim());
            if (dayOfWeek == -1) continue;
            registerWeeklyAlarmForDay(context, alarmData, dayOfWeek);
        }
    }

    /**
     * 특정 요일별 반복 알람 등록
     */
    private static void registerWeeklyAlarmForDay(Context context, AlarmData alarmData, int dayOfWeek) {
        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        calendar.set(Calendar.MINUTE, alarmData.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int today = calendar.get(Calendar.DAY_OF_WEEK);

        int daysUntilAlarm = dayOfWeek - today;
        if (daysUntilAlarm < 0) {
            daysUntilAlarm += 7;
        }
        // 오늘이 알람일 경우 시간이 지났으면 다음 주로
        if (daysUntilAlarm == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            daysUntilAlarm = 7;
        }
        calendar.add(Calendar.DATE, daysUntilAlarm);

        PendingIntent pendingIntent = createPendingIntent(context, alarmData, dayOfWeek);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Log.d("AlarmManagerHelper", "Registered weekly alarm id=" + alarmData.getId() + " for dayOfWeek=" +
                dayOfWeek + " at " + calendar.getTime());
    }

    /**
     * 단일 알람 취소
     */
    public static void cancelAlarm(Context context, AlarmData alarmData) {
        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context, alarmData, -1);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d("AlarmManagerHelper", "Cancelled single alarm id=" + alarmData.getId());
    }

    /**
     * 반복 알람(요일별) 취소
     */
    public static void cancelRepeatingAlarms(Context context, AlarmData alarmData) {
        String repeatStr = alarmData.getRepeat();
        if (repeatStr == null || repeatStr.trim().isEmpty() || repeatStr.equals("없음")) {
            cancelAlarm(context, alarmData);
            return;
        }

        if (repeatStr.equals("매일")) {
            // 매일 취소(일~토)
            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; dayOfWeek++) {
                cancelAlarmForDay(context, alarmData, dayOfWeek);
            }
            return;
        }

        String[] days = repeatStr.split(",");
        for (String day : days) {
            int dayOfWeek = dayOfWeekFromString(day.trim());
            if (dayOfWeek == -1) continue;
            cancelAlarmForDay(context, alarmData, dayOfWeek);
        }
    }

    /**
     * 특정 요일 반복 알람 취소 보조 메서드
     */
    private static void cancelAlarmForDay(Context context, AlarmData alarmData, int dayOfWeek) {
        AlarmManager alarmManager = getAlarmManager(context);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = createPendingIntent(context, alarmData, dayOfWeek);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d("AlarmManagerHelper", "Cancelled weekly alarm id=" + alarmData.getId() + " for dayOfWeek=" + dayOfWeek);
    }

    /**
     * 알람 식별자 및 요일 구분을 하는 PendingIntent 생성
     * repeatDayOfWeek : -1 → 단일 알람, 1~7 → 요일별 반복 알람
     */
    private static PendingIntent createPendingIntent(Context context, AlarmData alarmData, int repeatDayOfWeek) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmId",      alarmData.getId());
        intent.putExtra("alarmName",    alarmData.getName());
        intent.putExtra("hour",         alarmData.getHour());
        intent.putExtra("minute",       alarmData.getMinute());
        intent.putExtra("mis_on",       alarmData.getMisOn());
        intent.putExtra("mis_num",      alarmData.getMis_num());
        intent.putExtra("mis_cnt",      alarmData.getMis_count());
        intent.putExtra("sound_on",     alarmData.getSoundOn());
        intent.putExtra("sound",        alarmData.getSound());
        intent.putExtra("user_uid",     alarmData.getUserUid());

        if (repeatDayOfWeek != -1) {
            intent.putExtra("repeat_day", repeatDayOfWeek);
        }

        int requestCode = generateRequestCode(alarmData.getId(), repeatDayOfWeek);

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * 알람 고유 requestCode 생성
     */
    private static int generateRequestCode(long alarmId, int repeatDayOfWeek) {
        if (repeatDayOfWeek == -1) {
            return (int) alarmId;
        } else {
            // 예: 알람ID * 10 + 요일(1~7)
            return (int) alarmId * 10 + repeatDayOfWeek;
        }
    }

    /**
     * 요일 문자열을 Calendar요일 상수로 변환
     */
    private static int dayOfWeekFromString(String dayStr) {
        return switch (dayStr) {
            case "일" -> Calendar.SUNDAY;
            case "월" -> Calendar.MONDAY;
            case "화" -> Calendar.TUESDAY;
            case "수" -> Calendar.WEDNESDAY;
            case "목" -> Calendar.THURSDAY;
            case "금" -> Calendar.FRIDAY;
            case "토" -> Calendar.SATURDAY;
            default -> -1;
        };
    }

    /**
     * 알람 관리자 가져오기
     */
    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 정확한 알람 권한 요청 (Android 12+)
     */
    private static void requestExactAlarmPermissionIfNeeded(Context context, AlarmManager alarmManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
    /**
     * 반복 알람(onReceive 후) 다음 회차만 스케줄
     * @param context
     * @param alarmData
     * @param repeatDayOfWeek -1=매일, 1~7=요일별 반복
     */
    public static void scheduleNextAfterTrigger(Context context, AlarmData alarmData, int repeatDayOfWeek) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // PendingIntent 는 기존에 쓰던 것과 동일해야 함
        PendingIntent pi = createPendingIntent(context, alarmData, repeatDayOfWeek);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        cal.set(Calendar.MINUTE, alarmData.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (repeatDayOfWeek == -1) {
            // 매일 반복 → 다음 날
            cal.add(Calendar.DATE, 1);
        } else {
            // 요일별 반복 → 다음 주 해당 요일까지
            int today = cal.get(Calendar.DAY_OF_WEEK);
            int diff = repeatDayOfWeek - today;
            if (diff <= 0) {
                diff += 7;
            }
            cal.add(Calendar.DATE, diff);
        }

        long triggerAt = cal.getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }

        Log.d("AlarmManagerHelper",
                "scheduleNextAfterTrigger id=" + alarmData.getId() +
                        " repeatDay=" + repeatDayOfWeek +
                        " at=" + cal.getTime());
    }

}