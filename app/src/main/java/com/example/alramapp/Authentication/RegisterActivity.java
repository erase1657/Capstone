package com.example.alramapp.Authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Database.DataAccess;
import com.example.alramapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private EditText emailEditText,passwordCheckEditText,passwordEditText;
    private Button CreateAccountButton, Backbtn;
    private ImageView Id_check, Password_check1, Password_check2;
    private Button Seepassword1,Seepassword2;


    private FirebaseAuth mAuth; //Firebase 인증 객체 선언
    private DataAccess database;
    //인증 상태 확인
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        mAuth = FirebaseAuth.getInstance(); // Firebase 인증 객체 초기화
        database= new DataAccess();

        Backbtn = findViewById(R.id.backbtn);

        emailEditText =  findViewById(R.id.register_email_input);
        passwordEditText = findViewById(R.id.register_password_input);
        passwordCheckEditText = findViewById(R.id.register_password_check);
        CreateAccountButton = findViewById(R.id.completebtn);

        Id_check = findViewById(R.id.id_check);
        Password_check1 = findViewById(R.id.password_check1);
        Password_check2 = findViewById(R.id.password_check2);

        Seepassword1 = findViewById(R.id.seepassword1);
        Seepassword2 = findViewById(R.id.seepassword2);

        // EditText에 TextChangedListener 추가
        emailEditText.addTextChangedListener(new SimpleTextWatcher());
        passwordEditText.addTextChangedListener(new SimpleTextWatcher());
        passwordCheckEditText.addTextChangedListener(new SimpleTextWatcher());


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                createAccount(email,password);
            }
        });

        Backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
    }//인증 상태 확인 종료

    //신규 사용자 이메일 기반 계정 생성 메서드
    private void createAccount(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();
                            if(user != null){
                                database.UpdateUser(user); //가입회원 정보 데이터베이스에 업데이트
                            } else {
                                Log.w(TAG, "FirebaseUser is null after sign up");
                            }

                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(RegisterActivity.this, "계정 생성 성공! 로그인 해주세요.",
                                    Toast.LENGTH_SHORT).show();

                            updateUI(user);

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {

                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "계정 생성 실패. 중복된 이메일입니다.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void reload() { }

    private void updateUI(FirebaseUser user) {

    }

    // 유효성 검사 체크를 위한 실시간 검사 클래스
    private class SimpleTextWatcher implements android.text.TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateInputFields();
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }

    // 입력 필드 유효성 검사 메서드
    @SuppressLint("ClickableViewAccessibility")
    private void validateInputFields() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordCheck = passwordCheckEditText.getText().toString().trim();

        boolean isValidEmail = isValidEmail(email);
        boolean isValidPasswordLength = password.length() >= 6; // 비밀번호 최소 6자 이상
        boolean doPasswordsMatch = password.equals(passwordCheck); // 두 비밀번호 일치 여부

        // 유효성 메시지 가시성 설정
        if (isValidEmail) {
            Id_check.setVisibility(View.VISIBLE);
        } else {
            Id_check.setVisibility(View.INVISIBLE); // 이메일이 유효하지 않으면 숨김
        }

        if (isValidPasswordLength) {
            Password_check1.setVisibility(View.VISIBLE);
        } else {
            Password_check1.setVisibility(View.INVISIBLE); // 비밀번호 길이가 부족하면 숨김
        }

        if (!password.isEmpty() && doPasswordsMatch && isValidPasswordLength) {
            Password_check2.setVisibility(View.VISIBLE);
        } else {
            Password_check2.setVisibility(View.INVISIBLE); // 비밀번호 불일치 시 숨김
        }

        // 버튼 활성화/비활성화 설정
        if (isValidEmail && isValidPasswordLength && doPasswordsMatch) {
            CreateAccountButton.setEnabled(true);
            CreateAccountButton.setBackgroundResource(R.drawable.login_btn); // 활성화된 배경 이미지 설정
            CreateAccountButton.setTextColor(Color.BLACK); // 텍스트 색상 설정 (활성화된 상태)
        } else {
            CreateAccountButton.setEnabled(false);
            CreateAccountButton.setBackgroundResource(R.drawable.false_enable_btn); // 비활성화된 배경 이미지 설정
            CreateAccountButton.setTextColor(Color.parseColor("#535353")); // 텍스트 색상 설정 (비활성화된 상태)
        }
        Seepassword1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 비밀번호 표시
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT); // 일반 텍스트로 설정
                        passwordEditText.setTransformationMethod(null); // 변환기 제거 (비밀번호 숨김 해제)
                        return true; // 이벤트가 처리됨

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 비밀번호 숨김
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 비밀번호 타입으로 설정
                        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // 변환기로 다시 설정
                        return true; // 이벤트가 처리됨
                }
                return false; // 다른 경우에는 기본 동작 수행
            }
        });
        Seepassword2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 비밀번호 표시
                        passwordCheckEditText.setInputType(InputType.TYPE_CLASS_TEXT); // 일반 텍스트로 설정
                        passwordCheckEditText.setTransformationMethod(null); // 변환기 제거 (비밀번호 숨김 해제)
                        return true; // 이벤트가 처리됨

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 비밀번호 숨김
                        passwordCheckEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 비밀번호 타입으로 설정
                        passwordCheckEditText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // 변환기로 다시 설정
                        return true; // 이벤트가 처리됨
                }
                return false; // 다른 경우에는 기본 동작 수행
            }
        });
    }

    // 이메일 유효성 검사 메서드 (간단한 정규 표현식 사용)
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
}