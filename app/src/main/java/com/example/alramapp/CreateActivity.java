package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.Database.UserInform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
/*
    TODO: 1. 기존 캐릭터가 사망했을 때, 재 생성 구현(프로필, 파이어베이스 데이터베이스 업데이트)
          2. 캐릭터 생성 후, 밥 시간 설정 페이지로 이동

 */


public class CreateActivity extends AppCompatActivity {

    private DataAccess database;
    private FirebaseUser currentUser;

    private ImageView profileIcon;
    private Button BackBtn, lBtn, rBtn, fBtn, mBtn, createBtn;
    private EditText editName;
    private TextView createTitle;

    private int[] profileImages = {
            R.drawable.profile_cat,
            R.drawable.profile_fish,
            R.drawable.profile_bird,
            R.drawable.profile_dog
    };
    private String[] profileImageNames = {
            "profile_cat", "profile_fish", "profile_bird", "profile_dog"
    };

    private int currentProfileIndex = 0;
    private String selectedGender = null;
    private String originalGender = null;
    private String originalImageName = null;

    private int fButtonSelectedDrawable = R.drawable.btn_f_c;
    private int fButtonNormalDrawable = R.drawable.btn_c;

    private int mButtonSelectedDrawable = R.drawable.btn_m_c;
    private int mButtonNormalDrawable = R.drawable.btn_m;

    private boolean isRestart = false;
    private boolean isModify = false;

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
        createTitle = findViewById(R.id.create_title); // 타이틀 텍스트뷰 참조

        database = new DataAccess();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileIcon.setImageResource(profileImages[currentProfileIndex]);

        //  Intent 정보 받아오기
        Intent intent = getIntent();
        isRestart = intent.getBooleanExtra("isRestart", false);
        isModify = intent.getBooleanExtra("isModify", false);

        //  타이틀 텍스트 설정
        if (isRestart) {
            createTitle.setText("펫 다시 만들기");
        } else if (isModify) {
            createTitle.setText("펫 정보 수정");
        } else {
            createTitle.setText("펫 생성");
        }

        //  데이터 로딩 (isModify일 경우)
        if (isModify) {
            loadUserDataForModify();
        }

        BackBtn.setOnClickListener(v -> finish());

        lBtn.setOnClickListener(v -> {
            currentProfileIndex = (currentProfileIndex - 1 + profileImages.length) % profileImages.length;
            profileIcon.setImageResource(profileImages[currentProfileIndex]);
        });

        rBtn.setOnClickListener(v -> {
            currentProfileIndex = (currentProfileIndex + 1) % profileImages.length;
            profileIcon.setImageResource(profileImages[currentProfileIndex]);
        });

        fBtn.setOnClickListener(v -> {
            selectedGender = "f";
            updateGenderButtonUI();
        });

        mBtn.setOnClickListener(v -> {
            selectedGender = "m";
            updateGenderButtonUI();
        });

        createBtn.setOnClickListener(v -> {
            String profileName = editName.getText().toString().trim();

            if (profileName.isEmpty()) {
                Toast.makeText(CreateActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String finalGender = (selectedGender != null) ? selectedGender : originalGender;
            if (finalGender == null) {
                Toast.makeText(CreateActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedProfileImageName = (isModify && !isImageChanged()) ? originalImageName : profileImageNames[currentProfileIndex];

            String uid = currentUser.getUid();
            DatabaseReference userRef = database.dataref.child("users").child(uid);

            userRef.get().addOnSuccessListener(dataSnapshot -> {
                boolean isCreating = !dataSnapshot.exists();
                int currentLife = 3;
                int currentScore = 0;

                if (dataSnapshot.exists()) {
                    Long lifeVal = dataSnapshot.child("life").getValue(Long.class);
                    Long scoreVal = dataSnapshot.child("score").getValue(Long.class);
                    if (lifeVal != null) currentLife = lifeVal.intValue();
                    if (scoreVal != null) currentScore = scoreVal.intValue();
                }

                if (isRestart) currentLife = 3;
                if (isRestart) currentScore = 0;

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", profileName);
                updates.put("gender", finalGender);
                updates.put("image", selectedProfileImageName);
                updates.put("life", currentLife);
                updates.put("score", currentScore);

                userRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            if (isRestart) {
                                Toast.makeText(CreateActivity.this, "펫을 다시 만들었습니다!", Toast.LENGTH_SHORT).show();
                                Intent foodTimeIntent = new Intent(CreateActivity.this, SetFoodTimeActivity.class);
                                foodTimeIntent.putExtra("finalGender", finalGender);
                                foodTimeIntent.putExtra("name", profileName);
                                startActivity(foodTimeIntent);
                                finish();
                            } else if(isCreating){
                                Toast.makeText(CreateActivity.this, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent foodTimeIntent = new Intent(CreateActivity.this, SetFoodTimeActivity.class);
                                foodTimeIntent.putExtra("finalGender", finalGender);
                                foodTimeIntent.putExtra("name", profileName);
                                startActivity(foodTimeIntent);
                                finish();
                            }else {
                                Toast.makeText(CreateActivity.this, "프로필이 수정 되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();  // 수정 완료 후 복귀
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(CreateActivity.this, "데이터 저장 실패", Toast.LENGTH_SHORT).show());
            });
        });
    }

    private void loadUserDataForModify() {
        database.readUserByUid(currentUser.getUid(), new DataAccess.UserLoadCallback() {
            @Override
            public void onUserLoaded(UserInform userInfo) {
                if (userInfo != null) {
                    editName.setText(userInfo.getName());

                    selectedGender = userInfo.getGender();
                    originalGender = selectedGender;
                    updateGenderButtonUI();

                    originalImageName = userInfo.getImage();
                    for (int i = 0; i < profileImageNames.length; i++) {
                        if (profileImageNames[i].equals(originalImageName)) {
                            currentProfileIndex = i;
                            break;
                        }
                    }
                    profileIcon.setImageResource(profileImages[currentProfileIndex]);
                }
            }
        });
    }

    private void updateGenderButtonUI() {
        if ("f".equals(selectedGender)) {
            fBtn.setBackgroundResource(fButtonSelectedDrawable);
            mBtn.setBackgroundResource(mButtonNormalDrawable);
        } else if ("m".equals(selectedGender)) {
            fBtn.setBackgroundResource(fButtonNormalDrawable);
            mBtn.setBackgroundResource(mButtonSelectedDrawable);
        }
    }

    private boolean isImageChanged() {
        return !profileImageNames[currentProfileIndex].equals(originalImageName);
    }
}
