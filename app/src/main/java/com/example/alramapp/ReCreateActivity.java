package com.example.alramapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


/*
    TODO: 1. 사망 사유 업데이트
          2. 다시 생성하기 버튼 클릭 시, CreateActivity로 이동

 */


/**
 * 캐릭터 사망시 재 생성 '안내'를 돕기위한 페이지 구현
 */
public class ReCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.recreate_page);

    }
}