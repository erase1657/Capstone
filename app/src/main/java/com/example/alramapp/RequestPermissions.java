package com.example.alramapp; // 프로젝트 구조에 맞게 패키지명을 변경하세요

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class RequestPermissions {
    public static final int REQUEST_CODE_DEFAULT = 100;
    public static final int REQUEST_CODE_OVERLAY = 1234;

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.WAKE_LOCK,
            // 필요한 퍼미션 추가
    };

    // 권한 요청 (Activity의 onCreate 등에서 호출)
    public static void checkAndRequestPermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_CODE_DEFAULT);
        }
        // SYSTEM_ALERT_WINDOW는 따로 처리
        if (!Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }

    // 권한 거부시 안내 등을 추가로 할 수 있는 함수도 필요 시 확장
}