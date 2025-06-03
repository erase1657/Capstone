package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alramapp.Alarm.SQLlite.AlarmDBHelper;
import com.example.alramapp.Authentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView percentText;
    private ImageView loadingImage;

    private final int DEFAULT_LIFE = 3;
    private Handler handler = new Handler();
    private Runnable progressRunnable;
    private boolean isNextScreenStarted = false;

    private AlarmDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        percentText = findViewById(R.id.percentText);
        loadingImage = findViewById(R.id.loadingImage);

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading_image)
                .priority(Priority.IMMEDIATE)
                .placeholder(R.drawable.default_pet) //
                .into(loadingImage);


        startProgressBar();
        checkLoginAndLifeStatus(); // 병렬로 실행
    }

    private void startProgressBar() {
        int[] progress = {0};

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (progress[0] <= 100 && !isNextScreenStarted) {
                    progressBar.setProgress(progress[0]);
                    percentText.setText(progress[0] + "%");
                    progress[0] += 4;
                    handler.postDelayed(this, 10);
                }
            }
        };

        handler.post(progressRunnable);
    }

    private void checkLoginAndLifeStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user == null) {
            goToNextActivity(LoginActivity.class);
        } else {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            dbHelper = new AlarmDBHelper(this);
            boolean foodcheck = dbHelper.hasFoodAlarm(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long lifeVal = snapshot.child("life").getValue(Long.class);
                    int life = (lifeVal != null) ? lifeVal.intValue() : DEFAULT_LIFE;

                    if (life <= 0) {
                        goToNextActivity(PetRestartActivity.class);
                    } else if (!foodcheck ) {
                        goToNextActivity(SetFoodTimeActivity.class);
                    } else {
                        goToNextActivity(MainActivity.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SplashActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    goToNextActivity(LoginActivity.class);
                }
            });
        }
    }

    private void goToNextActivity(Class<?> targetActivity) {
        if (isNextScreenStarted) return;
        isNextScreenStarted = true;
        handler.removeCallbacks(progressRunnable); // 진행 막기

        Intent intent = new Intent(SplashActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
    }
}
