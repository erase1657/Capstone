package com.example.alramapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.alramapp.Alarm.AlarmData;
import com.example.alramapp.Alarm.RecyclerView.AlarmAdapter;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.Database.UserInform;
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
    private ArrayList<AlarmData> alarmList;
    private AlarmAdapter adapter;
    private AlarmDBHelper dbHelper;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_page);

        dbHelper   = new AlarmDBHelper(this);
        alarmList  = new ArrayList<>();
        adapter    = new AlarmAdapter(alarmList);

        rv = findViewById(R.id.recyview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        //버튼
        showDialogButton = findViewById(R.id.guidebtn);
        backButton = findViewById(R.id.backbtn);
        infromButton = findViewById(R.id.infrombtn);
        rankingButton = findViewById(R.id.rankingbtn);
        addAlramButton = findViewById(R.id.btn_add);

        //초기 화면 설정
        loadUserProfileImage();                                                     //사용자 프로필 로드
        loadAlarmsFromDb();                                                         //사용자 알람목록 로드


        //각각의 알람 아이템 클릭 시, '알람 수정' 바텀 시트 다이얼로그 화면에 표시
        adapter.setOnItemClickListener(alarmData -> {
            MyBottomSheetDialog dialog = MyBottomSheetDialog.newInstance(alarmData);
            dialog.show(getSupportFragmentManager(), "EditAlarmDialog");
        });


        // + 버튼 클릭 시, '알람 생성' 바텀 시트 다이얼로그를 화면에 표시
        addAlramButton.setOnClickListener(v -> {
            AlarmData newAlarm = new AlarmData();
            MyBottomSheetDialog dialog = MyBottomSheetDialog.newInstance(newAlarm);
            dialog.show(getSupportFragmentManager(), "BS");
        });



        //뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //가이드 다이얼로그 표시 버튼
        showDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(MainActivity.this); // this는 Activity일 경우
                dialog.setContentView(R.layout.item_guide); // 위에 작성한 XML 파일명
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                ViewPager2 viewPager = dialog.findViewById(R.id.imageViewPager);
                TextView tvTitle = dialog.findViewById(R.id.tv_title);
                TextView tvDescription = dialog.findViewById(R.id.tv_description);
                Button confirmButton = dialog.findViewById(R.id.confirm_button);
                WormDotsIndicator dotsIndicator = dialog.findViewById(R.id.dotpage);

                //다이얼로그별로 이미지, 제목, 설명이 설정된 리스트 slideItems를 만듦
                //추후 수정 필요함 예시로 일단 5개 정도 넣었음
                List<SlideItem> slideItems = new ArrayList<>();
                slideItems.add(new SlideItem(R.drawable.item_guide1, getString(R.string.title1), getString(R.string.description1)));
                slideItems.add(new SlideItem(R.drawable.item_guide2, getString(R.string.title2), getString(R.string.description2)));
                slideItems.add(new SlideItem(R.drawable.item_guide3, getString(R.string.title3), getString(R.string.description3)));
                slideItems.add(new SlideItem(R.drawable.item_guide4, getString(R.string.title4), getString(R.string.description4)));
                slideItems.add(new SlideItem(R.drawable.item_guide5, getString(R.string.title5), getString(R.string.description5)));

                SlideAdapter adapter = new SlideAdapter(slideItems);
                viewPager.setAdapter(adapter);

                // 초기 텍스트 설정(0번째로)
                tvTitle.setText(slideItems.get(0).getTitle());
                tvDescription.setText(slideItems.get(0).getDescription());

                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        tvTitle.setText(slideItems.get(position).getTitle());
                        tvDescription.setText(slideItems.get(position).getDescription());

                    }
                });

                dotsIndicator.attachTo(viewPager);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });


        //MyInform 이동 버튼
        infromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyInfromActivity.class);
                startActivity(intent);
            }
        });


        //Ranking 이동 버튼
        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RankActivity.class);
                startActivity(intent);
            }
        });



    }

    //메서드 부분
    private void loadUserProfileImage() {

        ImageView userPet;
        userPet = findViewById(R.id.img_pet);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        DataAccess dataAccess = new DataAccess();

        dataAccess.observeUserByUid(uid, new DataAccess.UserLoadCallback() {
            @Override
            public void onUserLoaded(UserInform user) {
                String imageName = user.getImage();

                // 추천 방식: switch 또는 map 사용
                int resId = getDrawableResIdFromName(imageName);
                userPet.setImageResource(resId);
            }
        });
    }
    private int getDrawableResIdFromName(String name) {
        switch (name) {
            case "profile_dog":
                return R.drawable.profile_dog;
            case "profile_cat":
                return R.drawable.profile_cat;
            case "profile_bird":
                return R.drawable.profile_bird;
            case "profile_fish":
                return R.drawable.profile_fish;
        }
        return 0;
    }
    private void loadAlarmsFromDb() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();

        alarmList.clear();                                                                          //초기 세팅을 위해 리스트 초기화
        alarmList.addAll(dbHelper.getAllAlarms(uid));                                               //사용자 uid기반 sqllite 알람데이터 호출
        adapter.notifyDataSetChanged();                                                             //어댑터 새로고침
    }




    //인터페이스 구현
    @Override
    public void onSave(AlarmData data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();                            //파이어베이스 인증 객체 생성

        String uid = user.getUid();                                                                 //현재 유저의 uid값 저장
        data.setUserUid(uid);                                                                       //AlarmData의 userUid에 uid값 저장

        long newId = dbHelper.insertAlarm(data);
        if (newId == -1) {
            Toast.makeText(this, "알람 저장 실패", Toast.LENGTH_SHORT).show();
            return;
        }
        data.setId(newId);
        alarmList.add(data);
        adapter.notifyItemInserted(alarmList.size() - 1);
        rv.scrollToPosition(alarmList.size() - 1);

    }
    @Override
    public void onUpdate(AlarmData data) {
        int count = dbHelper.updateAlarm(data);
        if (count == -1) {
            Toast.makeText(this, "알람 수정 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getId() == data.getId()) {
                AlarmData oldItem = alarmList.get(i);
                oldItem.copyFrom(data);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }
    @Override
    public void onDelete(AlarmData data) {
        int count = dbHelper.deleteAlarm(data.getId());
        if (count == -1) {
            Toast.makeText(this, "알람 삭제 실패", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getId() == data.getId()) {
                alarmList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

}
