package com.example.alramapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.Database.UserInform;
import com.example.alramapp.RecycleView.SlideAdapter;
import com.example.alramapp.RecycleView.SlideItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_page);



        Button showDialogButton, backButton, infromButton, rankingButton;


        //버튼
        showDialogButton = findViewById(R.id.guidebtn);
        backButton = findViewById(R.id.backbtn);
        infromButton = findViewById(R.id.infrombtn);
        rankingButton = findViewById(R.id.rankingbtn);



        loadUserProfileImage();



        //뒤로가기
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //가이드 다이얼로그 표시
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
                List<SlideItem> slideItems = new ArrayList<>();
                slideItems.add(new SlideItem(R.drawable.item_guide1, getString(R.string.title1), getString(R.string.description1)));
                slideItems.add(new SlideItem(R.drawable.item_guide2, getString(R.string.title2), getString(R.string.description2)));
                slideItems.add(new SlideItem(R.drawable.item_guide3, getString(R.string.title3), getString(R.string.description3)));
                slideItems.add(new SlideItem(R.drawable.item_guide4, getString(R.string.title4), getString(R.string.description4)));
                slideItems.add(new SlideItem(R.drawable.item_guide5, getString(R.string.title5), getString(R.string.description5)));


                SlideAdapter adapter = new SlideAdapter(slideItems);
                viewPager.setAdapter(adapter);


                // 초기 텍스트 설정
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


        //MyInform 이동
        infromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyInfromActivity.class);
                startActivity(intent);
            }
        });


        //Ranking 이동
        rankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RankActivity.class);
                startActivity(intent);
            }
        });
    }

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


}
