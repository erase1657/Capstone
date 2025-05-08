package com.example.alramapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alramapp.Authentication.LoginActivity;

public class CreateActivity extends AppCompatActivity {

    private ImageView profileIcon;
    private Button lBtn, rBtn, fBtn, mBtn, createBtn;
    private EditText editName;

    private int[] profileImages = {
            R.drawable.profile_cat,
            R.drawable.profile_fish,
            R.drawable.profile_bird,
            R.drawable.profile_dog
    };

    private int currentProfileIndex = 0; // 현재 표시 중인 프로필 이미지의 인덱스입니다.
    private String selectedGender = null; // 선택된 성별을 저장할 변수입니다. (null, "f", "m")

    // 성별 버튼의 선택/비선택 상태에 따른 배경 이미지 리소스 ID입니다.
    // ㅈㅎ9224님의 drawable 폴더에 있는 실제 리소스 ID로 변경해주세요!
    private int fButtonSelectedDrawable = R.drawable.f_btn_c; // 암컷 버튼 선택 시 이미지
    private int fButtonNormalDrawable = R.drawable.f_btn;             // 암컷 버튼 기본 이미지
    private int mButtonSelectedDrawable = R.drawable.m_btn_c; // 수컷 버튼 선택 시 이미지
    private int mButtonNormalDrawable = R.drawable.m_btn;             // 수컷 버튼 기본 이미지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_page);

        profileIcon = findViewById(R.id.profile_cat); // XML의 ImageView ID 사용
        lBtn = findViewById(R.id.arrow_left);
        rBtn = findViewById(R.id.arrow_right);
        fBtn = findViewById(R.id.f_button);
        mBtn = findViewById(R.id.m_button);
        editName = findViewById(R.id.editname);
        createBtn = findViewById(R.id.create_button);

        // 액티비티 시작 시 초기 프로필 이미지를 설정합니다.
        profileIcon.setImageResource(profileImages[currentProfileIndex]);

        // < 화살표 버튼 클릭 리스너 설정
        lBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 이미지 인덱스를 1 감소시킵니다.
                currentProfileIndex--;
                // 인덱스가 0보다 작아지면 (처음 이미지에서 왼쪽으로 이동 시) 마지막 이미지로 순환합니다.
                if (currentProfileIndex < 0) {
                    currentProfileIndex = profileImages.length - 1;
                }
                // 변경된 인덱스의 이미지로 ImageView를 업데이트합니다.
                profileIcon.setImageResource(profileImages[currentProfileIndex]);
            }
        }); //lBtn

        // > 화살표 버튼 클릭 리스너 설정
        rBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 이미지 인덱스를 1 증가시킵니다.
                currentProfileIndex++;
                // 인덱스가 이미지 목록 크기보다 같거나 커지면 (마지막 이미지에서 오른쪽으로 이동 시) 처음 이미지로 순환합니다.
                if (currentProfileIndex >= profileImages.length) {
                    currentProfileIndex = 0;
                }
                // 변경된 인덱스의 이미지로 ImageView를 업데이트합니다.
                profileIcon.setImageResource(profileImages[currentProfileIndex]);
            }
        });   //rBtn

        // 'f' (암컷) 성별 선택 버튼 클릭 리스너 설정
        fBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "f"; // 선택된 성별을 "f"로 설정합니다.
                updateGenderButtonUI(); // 성별 버튼의 배경 이미지를 업데이트하는 함수를 호출합니다.
            }
        });

        // 'm' (수컷) 성별 선택 버튼 클릭 리스ナー 설정
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "m"; // 선택된 성별을 "m"으로 설정합니다.
                updateGenderButtonUI(); // 성별 버튼의 배경 이미지를 업데이트하는 함수를 호출합니다.
            }
        });

        // '완료' 버튼 클릭 리스너 설정
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText에서 입력된 이름을 가져와서 앞뒤 공백을 제거합니다.
                String profileName = editName.getText().toString().trim();

                // 이름이 입력되었는지 확인합니다.
                if (profileName.isEmpty()) {
                    Toast.makeText(CreateActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 이름이 비어있으면 함수 실행을 중단합니다.
                }
                // 성별이 선택되었는지 확인합니다.
                if (selectedGender == null) {
                    Toast.makeText(CreateActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 성별이 선택되지 않았으면 함수 실행을 중단합니다.
                }

                // 이름과 성별 모두 확인되면 이동할 액티비티  Intent를 생성합니다.  // 수정해야함
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);

                // 다음 액티비티로 전달할 데이터를 Intent에 담습니다.
                intent.putExtra("profile_image_res_id", profileImages[currentProfileIndex]); // 선택된 프로필 이미지 리소스 ID
                intent.putExtra("profile_name", profileName); // 입력된 이름
                intent.putExtra("profile_gender", selectedGender); // 선택된 성별

                // 다음 액티비티를 시작합니다.
                startActivity(intent);

                // (선택 사항) 현재 CreateActivity를 종료하여 뒤로 가기 시 이 화면으로 돌아오지 않도록 합니다.
                finish();
                
            }
        }); //완료 버튼

    }//onCreate

    // 성별 버튼의 배경 이미지를 선택된 성별에 따라 업데이트하는 함수입니다.
    private void updateGenderButtonUI() {
        // 'f' 버튼 상태 업데이트
        if ("f".equals(selectedGender)) { // 문자열 비교 시 equals() 사용이 안전합니다.
            fBtn.setBackgroundResource(fButtonSelectedDrawable); // 'f'가 선택되면 선택된 이미지로 변경
            mBtn.setBackgroundResource(mButtonNormalDrawable); // 'm'은 기본 이미지로 변경
        }

        // 'm' 버튼 상태 업데이트
        else if ("m".equals(selectedGender)) { // 문자열 비교 시 equals() 사용이 안전합니다.
            fBtn.setBackgroundResource(fButtonNormalDrawable); // 'f'는 기본 이미지로 변경
            mBtn.setBackgroundResource(mButtonSelectedDrawable); // 'm'이 선택되면 선택된 이미지로 변경
        }
        else {
            // selectedGender가 null인 경우 (아무것도 선택되지 않은 초기 상태 등)
            fBtn.setBackgroundResource(fButtonNormalDrawable);
            mBtn.setBackgroundResource(mButtonNormalDrawable);
        }

    }//updateGenderButtonUI함수 끝
}