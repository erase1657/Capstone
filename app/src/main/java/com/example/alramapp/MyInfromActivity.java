package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MyInfromActivity extends AppCompatActivity {

    private Button modifyBtn, informBtn, questionBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myinfrom_page);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        modifyBtn = findViewById(R.id.modifycharacterbtn);
        informBtn = findViewById(R.id.logininformbtn);
        questionBtn = findViewById(R.id.questionbtn);
        logoutBtn = findViewById(R.id.logoutbtn);

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifycharacter();
            }
        });

        informBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showinform();
            }
        });

        questionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendquestion();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    void modifycharacter(){

    }

    void showinform(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String uid = user.getUid();

            new AlertDialog.Builder(this)
                    .setTitle("회원 정보")
                    .setMessage("이메일: " + email + "\n" + "UID값: " + uid)
                    .setPositiveButton("확인", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }


    }
    void sendquestion(){
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

    void logout(){
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
}
