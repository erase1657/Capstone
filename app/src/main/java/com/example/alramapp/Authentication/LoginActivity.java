package com.example.alramapp.Authentication;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LoginActivity extends AppCompatActivity {
    private Button GoRegister;
    private Button LoginButton;
    private Button Google, Github, Twitter, Facebook;

    private EditText emailEditText;
    private EditText passwordEditText;
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth; //Firebase 인증 객체 선언

    //인증 상태 확인

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance(); // Firebase 인증 객체 초기화

        GoRegister = findViewById(R.id.registerbtn);
        LoginButton = findViewById(R.id.loginbtn);
        Google = findViewById(R.id.googlebtn);
        Facebook = findViewById(R.id.facebookbtn);
        Github = findViewById(R.id.githubbtn);
        Twitter = findViewById(R.id.twitterbtn);

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

        //구글 로그인 버튼
        Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, GoogleSignInActivity.class);
                startActivity(intent);
            }
        });


        //페이스북 로그인 버튼
        Facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FacebookLoginActivity.class);
                startActivity(intent);
            }
        });

        Twitter.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GenericIdpActivity.class);
            intent.putExtra("provider", "twitter");
            startActivity(intent);
        });

        //깃헙 로그인 버튼 클릭
        Github.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GenericIdpActivity.class);
            intent.putExtra("provider", "github");
            startActivity(intent);
        });

    }
    public void getUserProfile() {
        // [START get_user_profile]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }
        // [END get_user_profile]
    }

    @Override
    public void onStart() {
        super.onStart();

        //현재 사용자가 로그인되어 있는지 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }

    }//인증 상태 확인 종료




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
        // [END sign_in_with_email]
    }


    private void reload() {

    }

    private void updateUI(FirebaseUser user) {

    }
}