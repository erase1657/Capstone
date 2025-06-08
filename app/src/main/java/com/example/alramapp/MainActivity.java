package com.example.alramapp;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.alramapp.Alarm.SQLlite.AlarmData;
import com.example.alramapp.Alarm.AlarmManagerHelper;
import com.example.alramapp.Alarm.AlarmReceiver;
import com.example.alramapp.Alarm.AlarmList.AlarmAdapter;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.RealTimeDatabase.DataAccess;
import com.example.alramapp.RealTimeDatabase.UserInform;
import com.example.alramapp.Alarm.MyBottomSheetDialog;
import com.example.alramapp.GuideDialog.SlideAdapter;
import com.example.alramapp.GuideDialog.SlideItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity implements MyBottomSheetDialog.OnSaveListener {

    private Button showDialogButton, backButton, infromButton, rankingButton, addAlramButton;
    private LinearLayout lable_foodtime;
    private ArrayList<AlarmData> alarmList;
    private AlarmAdapter adapter;
    private AlarmDBHelper dbHelper;
    private RecyclerView rv;
    private FrameLayout navbar;
    private LinearLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_page);

        RequestPermissions.checkAndRequestPermissions(this);

        dbHelper   = new AlarmDBHelper(this);
        alarmList  = new ArrayList<>();
        adapter    = new AlarmAdapter(alarmList);

        rv = findViewById(R.id.recyview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // UI 초기화
        updateFoodTimeTextView();
        loadUserProfileImage();
        loadAlarmsFromDb();

        // 알람 수정 다이얼로그
        adapter.setOnItemClickListener(alarmData -> {
            MyBottomSheetDialog dialog = MyBottomSheetDialog.newInstance(alarmData);
            dialog.show(getSupportFragmentManager(), "EditAlarmDialog");
        });

        // 알람 추가 버튼
        addAlramButton = findViewById(R.id.btn_add);
        addAlramButton.setOnClickListener(v -> {
            AlarmData newAlarm = new AlarmData();
            MyBottomSheetDialog dialog = MyBottomSheetDialog.newInstance(newAlarm);
            dialog.show(getSupportFragmentManager(), "AddAlarm");
        });

        //밥 시간 알람 수정 레이블
        lable_foodtime = findViewById(R.id.lable_foodtime);
        lable_foodtime.setOnClickListener(v -> {
            // 항상 DB에서 읽어오게!
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;
            AlarmData foodAlarm = dbHelper.getFoodAlarm(user.getUid());

            if (foodAlarm == null) {
                Toast.makeText(this, "식사 알람 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            MyBottomSheetDialog dialog = MyBottomSheetDialog.newInstance(foodAlarm);
            dialog.show(getSupportFragmentManager(), "FoodTime");
        });


        // 뒤로가기 버튼
        backButton = findViewById(R.id.backbtn);
        backButton.setOnClickListener(v -> finish());

        // 가이드 다이얼로그 호출
        showDialogButton = findViewById(R.id.guidebtn);
        showDialogButton.setOnClickListener(v -> showGuideDialog());

        // MyInform, Ranking 이동
        infromButton = findViewById(R.id.infrombtn);
        infromButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyInfromActivity.class))
        );
        rankingButton = findViewById(R.id.rankingbtn);
        rankingButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RankActivity.class))
        );

        rootView = findViewById(R.id.main);
        navbar = findViewById(R.id.navbar);


        //사용자 규격 하단바 위치 자동 맞춤
        ViewCompat.setOnApplyWindowInsetsListener(rootView, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                // 네비게이션바의 바닥 영역 값 구하기
                int insetBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

                // 기존 LayoutParams를 불러온 뒤 하단마진 조정
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) navbar.getLayoutParams();
                params.bottomMargin = insetBottom;
                navbar.setLayoutParams(params);

                return insets;
            }
        });
    }

    // ----------------------------------------------------
    // BroadcastReceiver: 단발 알람이 울린 후 isEnabled 꺼주기
    // ----------------------------------------------------
    private final BroadcastReceiver alarmStatusChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long changedId = intent.getLongExtra("alarmId", -1);
            if (changedId == -1) return;

            // 리스트에서 해당 ID 찾아 isEnabled=false, 아이템만 갱신
            for (int i = 0; i < alarmList.size(); i++) {
                AlarmData item = alarmList.get(i);
                if (item.getId() == changedId) {
                    item.setIsEnabled(false);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestPermissions.REQUEST_CODE_DEFAULT) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 [" + permissions[i] + "] 을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            // 권한 허용 완료 후 추가 처리도 여기에!
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestPermissions.REQUEST_CODE_OVERLAY) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "오버레이 권한을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
            // 오버레이 권한 허용 후 추가 처리도 여기에!
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ACTION 상수 사용하도록 수정
        IntentFilter filter = new IntentFilter(AlarmReceiver.ACTION_ALARM_STATUS_CHANGED);
        ContextCompat.registerReceiver(
                this,
                alarmStatusChangedReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(alarmStatusChangedReceiver);
    }

    // ----------------------------------------------------
    // DB에서 알람 불러오기
    // ----------------------------------------------------
    private void loadAlarmsFromDb() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms(uid));
        adapter.notifyDataSetChanged();
    }

    // ----------------------------------------------------
    // 프로필 이미지 로드
    // ----------------------------------------------------
    private void loadUserProfileImage() {
        ImageView userPet = findViewById(R.id.img_pet);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        new DataAccess().observeUserByUid(uid, new DataAccess.UserLoadCallback() {
            @Override
            public void onUserLoaded(UserInform user) {
                userPet.setImageResource(getDrawableResIdFromName(user.getImage()));
            }
        });
    }

    private int getDrawableResIdFromName(String name) {
        switch (name) {
            case "profile_dog":   return R.drawable.profile_dog;
            case "profile_cat":   return R.drawable.profile_cat;
            case "profile_bird":  return R.drawable.profile_bird;
            case "profile_fish":  return R.drawable.profile_fish;
            default:              return 0;
        }
    }

    // ----------------------------------------------------
    // BottomSheetDialog 콜백: 알람 저장
    // ----------------------------------------------------
    @Override
    public void onSave(AlarmData data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "사용자 인증 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        data.setUserUid(user.getUid());

        long newId = dbHelper.insertAlarm(data);

        if (newId == -1) {
            Toast.makeText(this, "알람 저장 실패" + newId, Toast.LENGTH_SHORT).show();
            return;
        }
        data.setId(newId);
        AlarmManagerHelper.register(this, data);


        alarmList.add(data);
        adapter.notifyItemInserted(alarmList.size() - 1);
        rv.scrollToPosition(alarmList.size() - 1);
    }

    // ----------------------------------------------------
    // BottomSheetDialog 콜백: 알람 수정
    // ----------------------------------------------------
    @Override
    public void onUpdate(AlarmData data) {
        int count = dbHelper.updateAlarm(data);
        if (count == -1) {
            Toast.makeText(this, "알람 수정 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        // 기존 알람 취소 후 재등록
        if (data.getRepeat() == null || data.getRepeat().trim().isEmpty()) {
            AlarmManagerHelper.cancelAlarm(this, data);
        } else {
            AlarmManagerHelper.cancelRepeatingAlarms(this, data);
        }
        AlarmManagerHelper.register(this, data);

        // 리스트 내 항목 갱신
        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getId() == data.getId()) {
                alarmList.get(i).copyFrom(data);
                adapter.notifyItemChanged(i);
                break;
            }
        }
        if (data.getIsFood() == 1) updateFoodTimeTextView();
        Log.d("MainActivity", "Alarm updated: " + data.toString());
    }

    // ----------------------------------------------------
    // BottomSheetDialog 콜백: 알람 삭제
    // ----------------------------------------------------
    @Override
    public void onDelete(AlarmData data) {
        int count = dbHelper.deleteAlarm(data.getId());
        if (count == -1) {
            Toast.makeText(this, "알람 삭제 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        if (data.getRepeat() == null || data.getRepeat().trim().isEmpty()) {
            AlarmManagerHelper.cancelAlarm(this, data);
        } else {
            AlarmManagerHelper.cancelRepeatingAlarms(this, data);
        }

        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getId() == data.getId()) {
                alarmList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }

    }
    private void updateFoodTimeTextView() {
        TextView tvFoodTime = findViewById(R.id.tv_foodtime);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvFoodTime.setText("--:--");
            return;
        }
        AlarmData foodAlarm = dbHelper.getFoodAlarm(user.getUid());
        if (foodAlarm == null) {
            tvFoodTime.setText("--:--");
            return;
        }
        int hour = foodAlarm.getHour();
        int minute = foodAlarm.getMinute();
        tvFoodTime.setText(String.format("%02d:%02d", hour, minute));
    }

    // ----------------------------------------------------
    // 가이드 다이얼로그 띄우기
    // ----------------------------------------------------
    private void showGuideDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.item_guide);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ViewPager2 viewPager = dialog.findViewById(R.id.imageViewPager);
        TextView tvTitle     = dialog.findViewById(R.id.tv_title);
        TextView tvDesc      = dialog.findViewById(R.id.tv_description);
        WormDotsIndicator dots = dialog.findViewById(R.id.dotpage);
        Button confirmBtn    = dialog.findViewById(R.id.confirm_button);

        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.item_guide1, getString(R.string.title1), getString(R.string.description1)));
        slideItems.add(new SlideItem(R.drawable.item_guide2, getString(R.string.title2), getString(R.string.description2)));
        slideItems.add(new SlideItem(R.drawable.item_guide3, getString(R.string.title3), getString(R.string.description3)));
        slideItems.add(new SlideItem(R.drawable.item_guide4, getString(R.string.title4), getString(R.string.description4)));

        SlideAdapter slideAdapter = new SlideAdapter(slideItems);
        viewPager.setAdapter(slideAdapter);
        dots.attachTo(viewPager);

        // 초기 텍스트
        tvTitle.setText(slideItems.get(0).getTitle());
        tvDesc.setText(slideItems.get(0).getDescription());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pos) {
                tvTitle.setText(slideItems.get(pos).getTitle());
                tvDesc.setText(slideItems.get(pos).getDescription());
            }
        });

        confirmBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}