package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Database.DataAccess;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;


public class CreateActivity extends AppCompatActivity {

    private DataAccess database;
    private FirebaseUser currentUser;

    private ImageView profileIcon;  //캐릭터 이미지
    private Button BackBtn, lBtn, rBtn, fBtn, mBtn, createBtn;
    private EditText editName;  //캐릭터 이름

    private int[] profileImages = {
            R.drawable.profile_cat,
            R.drawable.profile_fish,
            R.drawable.profile_bird,
            R.drawable.profile_dog
    };

    private int currentProfileIndex = 0; // 현재 표시 중인 프로필 이미지의 인덱스입니다.
    private String selectedGender = null; // 선택된 성별 값 (null, "f", "m")

    private int fButtonSelectedDrawable = R.drawable.btn_f_c; // 암컷 버튼 이미지(활성)
    private int fButtonNormalDrawable = R.drawable.btn_c;     // 암컷 버튼 이미지(기본)

    private int mButtonSelectedDrawable = R.drawable.btn_m_c; // 수컷 버튼 이미지(활성)
    private int mButtonNormalDrawable = R.drawable.btn_m;     // 수컷 버튼 이미지(기본)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_page);


        profileIcon = findViewById(R.id.profile_cat);
        lBtn = findViewById(R.id.arrow_left);
        rBtn = findViewById(R.id.arrow_right);
        fBtn = findViewById(R.id.f_button);
        mBtn = findViewById(R.id.m_button);
        editName = findViewById(R.id.editname);
        createBtn = findViewById(R.id.create_button);
        BackBtn = findViewById(R.id.backbtn);

        // 초기 캐릭터 설정(cat인 상태)
        profileIcon.setImageResource(profileImages[currentProfileIndex]);

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // < 화살표 버튼 클릭 리스너 설정
        lBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentProfileIndex--;  // 현재 이미지 인덱스를 1 감소시킵니다.

                if (currentProfileIndex < 0) {  // 인덱스가 0보다 작아지면 (처음 이미지에서 왼쪽으로 이동 시) 마지막 이미지로 순환합니다.
                    currentProfileIndex = profileImages.length - 1;
                }

                profileIcon.setImageResource(profileImages[currentProfileIndex]); // 변경된 인덱스의 이미지로 ImageView를 업데이트합니다.
            }
        }); //lBtn

        // > 화살표 버튼 클릭 리스너 설정
        rBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentProfileIndex++;  // 현재 이미지 인덱스를 1 증가시킵니다.

                if (currentProfileIndex >= profileImages.length) { // 인덱스가 이미지 목록 크기보다 같거나 커지면 (마지막 이미지에서 오른쪽으로 이동 시) 처음 이미지로 순환합니다.
                    currentProfileIndex = 0;
                }

                profileIcon.setImageResource(profileImages[currentProfileIndex]);   // 변경된 인덱스의 이미지로 ImageView를 업데이트합니다.
            }
        });

        //여성 선택 버튼
        fBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "f";
                updateGenderButtonUI(); //버튼 UI 변경
            }
        });

        //남성 선택 버튼
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "m";
                updateGenderButtonUI(); //버튼 UI 변경
            }
        });

        database = new DataAccess();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // '완료' 버튼 클릭 리스너 설정
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText에서 입력된 이름을 가져와서 앞뒤 공백을 제거합니다.
                String profileName = editName.getText().toString().trim();


                if (profileName.isEmpty()) { // 이름이 입력되었는지 확인합니다.
                    Toast.makeText(CreateActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedGender == null) { // 성별이 선택되었는지 확인합니다.
                    Toast.makeText(CreateActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 프로필 이미지 이름(키)를 문자열로 저장하는 예시
                String[] profileImageNames = {"profile_cat", "profile_fish", "profile_bird", "profile_dog"};
                String selectedProfileImageName = profileImageNames[currentProfileIndex];

                String uid = currentUser.getUid();
                DatabaseReference userRef = database.dataref.child("users").child(uid);

                //업데이트할 객체
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", profileName);
                updates.put("gender", selectedGender);
                updates.put("image", selectedProfileImageName);
                updates.put("life", 5);
                updates.put("score", 0);


                userRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateActivity.this, "프로필이 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateActivity.this, MyInfromActivity.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CreateActivity.this, "프로필 저장에 실패했습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // 성별 선택 시 버튼 이미지 변경
    private void updateGenderButtonUI() {
        // 'f' 버튼 상태 업데이트
        if ("f".equals(selectedGender)) { // 문자열 비교 시 equals() 사용이 안전합니다.
            fBtn.setBackgroundResource(fButtonSelectedDrawable); // 'f'가 선택되면 선택된 이미지로 변경
            mBtn.setBackgroundResource(mButtonNormalDrawable); // 'm'은 기본 이미지로 변경
        }

        // 'm' 버튼 상태 업데이트
        else if ("m".equals(selectedGender)) { // 문자열 비교 시 equals() 사용이 안전합니다.
            fBtn.setBackgroundResource(fButtonNormalDrawable); // 'f'는 기본 이미지로 변경
            mBtn.setBackgroundResource(mButtonSelectedDrawable); // 'm'이 선택되면 선택된 이미지로 변경
        }
        else {
            // selectedGender가 null인 경우 (아무것도 선택되지 않은 초기 상태 등)
            fBtn.setBackgroundResource(fButtonNormalDrawable);
            mBtn.setBackgroundResource(mButtonNormalDrawable);
        }

    }



}