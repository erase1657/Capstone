package com.example.alramapp.Authentication;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.CreateActivity;
import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.Database.UserInform;
import com.example.alramapp.MainActivity;
import com.example.alramapp.MyInfromActivity;
import com.example.alramapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LoginActivity extends AppCompatActivity {
    private Button GoRegister, LoginButton;
    private EditText emailEditText, passwordEditText;
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth; //Firebase 인증 객체 선언
    DataAccess database;
    //인증 상태 확인

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance(); // Firebase 인증 객체 초기화
        database = new DataAccess();

        GoRegister = findViewById(R.id.registerbtn);
        LoginButton = findViewById(R.id.loginbtn);


        emailEditText = findViewById(R.id.editemail);
        passwordEditText = findViewById(R.id.editpassword);

        //회원 가입 페이지 이동
        GoRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }

        });

        //이메일 로그인 버튼
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 입력이 없으면 더 이상 진행하지 않음
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 입력이 없으면 더 이상 진행하지 않음
                }

                signIn(email,password);

            }
        });


    }


    @Override
    public void onStart() {
        super.onStart();

        //현재 사용자가 로그인되어 있는지 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }

    }




    //이메일 기반 인증
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "로그인 성공",
                                    Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {

                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 다시 확인해주세요.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }


    private void reload() {

    }


    //로그인 후 실행될 로직
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            DataAccess database = new DataAccess();
            database.readUserByUid(user.getUid(), new DataAccess.UserLoadCallback() {
                @Override
                public void onUserLoaded(UserInform userInfo) {
                    if (userInfo != null) {
                        String name = userInfo.getName();
                        String gender = userInfo.getGender();
                        String image = userInfo.getImage();

                        boolean isNameEmpty = (name == null || name.trim().isEmpty());
                        boolean isGenderEmpty = (gender == null || gender.trim().isEmpty());
                        boolean isImageEmpty = (image == null || image.trim().isEmpty());

                        if (isNameEmpty || isGenderEmpty || isImageEmpty) { // 세 개 모두 공백일 경우 CreateActivity로 이동
                            Intent intent = new Intent(LoginActivity.this, CreateActivity.class);
                            startActivity(intent);
                            finish();
                        } else {// 정보가 충분하면 메인 액티비티 또는 홈 화면으로 이동
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // 사용자 정보가 없는 경우(에러 처리 또는 새 가입자 처리 등)
                        Toast.makeText(LoginActivity.this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }

    }
}