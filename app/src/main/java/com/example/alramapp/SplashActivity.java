package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alramapp.Authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 앱 스플래시 화면
 * 앱 첫 실행시 뜨도록 구현
 */

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView percentText;
    private ImageView loadingImage;

    private final int LOADING_DURATION = 500; // 총 0.5초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        percentText = findViewById(R.id.percentText);
        loadingImage = findViewById(R.id.loadingImage);

        // Glide를 사용하여 GIF 표시
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading_image)
                .into(loadingImage);

        animateProgressBar();
    }

    private void animateProgressBar() {
        Handler handler = new Handler();
        int[] progress = {0};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progress[0] <= 100) {
                    progressBar.setProgress(progress[0]);
                    percentText.setText(progress[0] + "%");
                    progress[0] += 2;
                    handler.postDelayed(this, 40); // 점점 자연스럽게
                } else {
                    checkLoginStatus();
                }
            }
        };

        handler.post(runnable);
    }

    private void checkLoginStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent;
        if (user != null) {
            intent = new Intent(this, MissionActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
