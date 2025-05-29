package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PetRestartActivity extends AppCompatActivity {

    private Button restartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_restart);

        restartButton = findViewById(R.id.restart_button);

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(PetRestartActivity.this, CreateActivity.class);
            intent.putExtra("isRestart", true); // 부활 모드 표시
            startActivity(intent);
            finish();
        });
    }
}
