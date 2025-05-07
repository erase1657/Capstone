package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Authentication.LoginActivity;
import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.Database.UserInform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;


public class MyInfromActivity extends AppCompatActivity {

    private DataAccess database;
    private FirebaseUser user;
    private Button ModifyBtn, InformBtn, QuestionBtn, LogoutBtn;
    private ImageView ProfileImage, GenderImage;
    private TextView NameValue, ScoreValue;
    private BaseRatingBar LifeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myinfrom_page);

        //버튼
        ModifyBtn = findViewById(R.id.modifycharacterbtn);
        InformBtn = findViewById(R.id.logininformbtn);
        QuestionBtn = findViewById(R.id.questionbtn);
        LogoutBtn = findViewById(R.id.logoutbtn);

        //이미지
        ProfileImage = findViewById(R.id.profileimage);
        GenderImage = findViewById(R.id.genderimage);

        //텍스트
        NameValue = findViewById(R.id.nametextview);
        ScoreValue = findViewById(R.id.socretextview);

        //레이팅 바
        LifeValue = findViewById(R.id.liferating);

        updatePage();


        ModifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifycharacter();
            }
        });

        InformBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showinform();
            }
        });

        QuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendquestion();
            }
        });

        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    void updatePage() {
        database = new DataAccess();
        user = FirebaseAuth.getInstance().getCurrentUser();

        database.readUserByUid(user.getUid(), new DataAccess.UserLoadCallback() {
            @Override
            public void onUserLoaded(UserInform userInfo) {
                if (userInfo != null) {

                    String name = userInfo.getName();
                    String gender = userInfo.getGender();
                    String image = userInfo.getImage();
                    int score = userInfo.getScore();
                    int life = userInfo.getLife();

                    //이름 설정
                    NameValue.setText(name);

                    // 성별 이미지 설정
                    if ("f".equalsIgnoreCase(gender)) {
                        GenderImage.setImageResource(R.drawable.f_btn); // 성별 이미지 리소스 예
                    } else if ("m".equalsIgnoreCase(gender)) {
                        GenderImage.setImageResource(R.drawable.m_btn);
                    }




                    //캐릭터 이미지 설정
                    int resId = getProfileImageResId(image);
                    if (resId != 0) {
                        ProfileImage.setImageResource(resId);
                    }

                    //점수 설정
                    ScoreValue.setText("생존 점수: " + score);

                    //생명 설정

                    LifeValue.setRating(life);


                }
            }
        });
    }

    //캐릭터 정보 변경
    void modifycharacter() {
        Intent intent = new Intent(this, CreateActivity.class);
        intent.putExtra("", true);
        startActivity(intent);
    }

    //계정 정보 (다이얼로그)
    void showinform() {
        Log.d("MyInfromActivity", "showinform() 호출됨");

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            new AlertDialog.Builder(this)
                    .setTitle("회원 정보")
                    .setMessage("이메일: " + email + "\n" + "UID값: " + uid)
                    .setNegativeButton("회원탈퇴", (dialog, which) -> {
                        dialog.dismiss();
                        confirmDeleteAccount();
                    })
                    .setPositiveButton("확인", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }



    //로그아웃
    void logout() {
        FirebaseAuth.getInstance().signOut();
        new AlertDialog.Builder(this)
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();

    }

    //문의
    void sendquestion() {
        String supportEmail = "erase1657@naver.com";

        new AlertDialog.Builder(this)
                .setTitle("문의하기")
                .setMessage("문의 이메일: " + supportEmail)
                .setPositiveButton("문의하기", (dialog, which) -> {
                    sendInquiryEmail(supportEmail);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    //이메일 문의
    private void sendInquiryEmail(String toEmail) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "앱 문의합니다");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "안녕하세요,\n문의내용을 여기에 작성해주세요.");

        try {
            startActivity(Intent.createChooser(emailIntent, "이메일 앱 선택"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "이메일 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //회원탈퇴 (재확인 다이얼로그)
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말로 회원 탈퇴를 하시겠습니까? 모든 데이터가 삭제됩니다.")
                .setPositiveButton("탈퇴하기", (dialog, which) -> {
                    dialog.dismiss();
                    deleteAccount();
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    //회원 탈퇴(파이어베이스 인증 삭제)
    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 계정 삭제 성공 후 DB 데이터 삭제 실행
                            deleteUserDataFromDatabase(uid);

                            // 로그인 화면으로 이동
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "회원 탈퇴에 실패했습니다. 재인증이 필요할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    //회원탈퇴 (데이터베이스 값 삭제)
    private void deleteUserDataFromDatabase(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "사용자 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "사용자 데이터 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //프로필 값 찾기
    private int getProfileImageResId(String imageName) {
        if (imageName == null) return 0;

        switch(imageName) {
            case "profile_cat":
                return R.drawable.profile_cat;
            case "profile_fish":
                return R.drawable.profile_fish;
            case "profile_bird":
                return R.drawable.profile_bird;
            case "profile_dog":
                return R.drawable.profile_dog;
            default:
                return 0;
        }
    }
}
