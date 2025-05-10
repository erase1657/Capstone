package com.example.alramapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankActivity extends AppCompatActivity {

    private LinearLayout rankFrame;
    private DatabaseReference usersRef; // Realtime Database
    private FirebaseFirestore firestore; // Firestore 사용 시

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking_page);

        rankFrame = findViewById(R.id.rank_frame);
        backButton = findViewById(R.id.backbtn);

        // Firebase 초기화
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users"); // "users" 경로로 저장된 데이터 불러옴

        loadUserRanking();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadUserRanking() {
        usersRef.orderByChild("rank_score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rankFrame.removeAllViews(); // 기존 뷰 초기화
                List<User> userList = new ArrayList<>();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String name = userSnap.child("name").getValue(String.class);
                    String pet = userSnap.child("image").getValue(String.class);
                    Long scoreLong = userSnap.child("score").getValue(Long.class);
                    int score = scoreLong != null ? scoreLong.intValue() : 0;

                    userList.add(new User(name, pet, score));
                }

                // 내림차순 정렬 (점수 높은 순)
                Collections.sort(userList, (a, b) -> Integer.compare(b.rankScore, a.rankScore));

                // UI에 반영
                for (int i = 0; i < userList.size(); i++) {
                    View userView = getLayoutInflater().inflate(R.layout.user_frame, null);

                    TextView textRank = userView.findViewById(R.id.text_rank);
                    ImageView userPet = userView.findViewById(R.id.user_pet);
                    TextView userName = userView.findViewById(R.id.user_name);
                    TextView rankScore = userView.findViewById(R.id.rank_score);


                    User user = userList.get(i);

                    textRank.setText(String.valueOf(i + 1));
                    userName.setText(user.userName);
                    rankScore.setText(String.valueOf(user.rankScore));

                    // 펫 이미지 설정
                    switch (user.userPet) {
                        case "profile_cat":
                            userPet.setImageResource(R.drawable.img_cat_nobg);
                            break;
                        case "profile_dog":
                            userPet.setImageResource(R.drawable.img_dog_nobg);
                            break;
                        case "profile_fish":
                            userPet.setImageResource(R.drawable.img_fish_nobg);
                            break;
                        case "profile_bird":
                            userPet.setImageResource(R.drawable.img_bird_nobg);
                            break;
                        default:
                            userPet.setImageResource(R.drawable.default_pet); // 기본 이미지
                    }

                    // 1위, 2위, 3위에 맞는 이미지 설정
                    switch (i) {
                        case 0:
                            textRank.setBackgroundResource(R.drawable.rank_first);
                            break;
                        case 1:
                            textRank.setBackgroundResource(R.drawable.rank_second); // 2위
                            break;
                        case 2:
                            textRank.setBackgroundResource(R.drawable.rank_third); // 3위
                            break;
                        default:
                            textRank.setBackgroundResource(0);
                    }


                    rankFrame.addView(userView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RankActivity.this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 사용자 정보 클래스
    private static class User {
        String userName;
        String userPet;
        int rankScore;

        public User(String userName, String userPet, int rankScore) {
            this.userName = userName;
            this.userPet = userPet;
            this.rankScore = rankScore;
        }
    }
}
