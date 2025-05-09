package com.example.alramapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        rankFrame = findViewById(R.id.rank_frame);

        // Firebase 초기화
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users"); // "users" 경로로 저장된 데이터 불러옴

        loadUserRanking();
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
                   // ImageView containerEmpty1 = userView.findViewById(R.id.container_empty1); // 이미지 변경 대상

                    User user = userList.get(i);

                    textRank.setText(String.valueOf(i + 1));
                    userName.setText(user.userName);
                    rankScore.setText(String.valueOf(user.rankScore));

                    // 펫 이미지 설정
                    switch (user.userPet) {
                        case "profile_cat":
                            userPet.setImageResource(R.drawable.cat);
                            break;
                        case "profile_dog":
                            userPet.setImageResource(R.drawable.dog);
                            break;
                        case "profile_fish":
                            userPet.setImageResource(R.drawable.fish);
                            break;
                        case "profile_bird":
                            userPet.setImageResource(R.drawable.bird);
                            break;
                        default:
                            userPet.setImageResource(R.drawable.default_pet); // 기본 이미지
                    }
                    /*
                    // 1위, 2위, 3위에 맞는 이미지 설정
                    switch (i) {
                        case 0:
                            containerEmpty1.setImageResource(R.drawable.first); // 1위
                            break;
                        case 1:
                            containerEmpty1.setImageResource(R.drawable.second); // 2위
                            break;
                        case 2:
                            containerEmpty1.setImageResource(R.drawable.third); // 3위
                            break;
                        default:
                            containerEmpty1.setImageResource(R.drawable.back); // 나머지 순위는 기본 이미지
                            break;
                    }
                    */

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
