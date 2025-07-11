package com.example.alramapp.RealTimeDatabase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataAccess {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseReference dataref = database.getReference();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    //유저 정보 쓰기
    public void RegisterUserUpadate(FirebaseUser user) {

        UserInform Info = new UserInform();
        if(user != null){
            Info.setUid(user.getUid());
            Info.setEmail(user.getEmail());
            Info.setImage("");
            Info.setName("");
            Info.setGender("");
            Info.setLife(0);
            Info.setScore(0);

        }else{
            System.out.println("user is null");
            return;
        }

        dataref.child("users").child(Info.getUid()).setValue(Info);
    }

    public interface UserLoadCallback {
        void onUserLoaded(UserInform user);
    }


    //한번만 호출되는 메서드(잘 사용되지 않는 데이터를 읽을때)
    public void readUserByUid(String uid, final UserLoadCallback callback){
        if(uid == null || uid.isEmpty()) {
            callback.onUserLoaded(null);
            return;
        }
        dataref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInform userInfo = snapshot.getValue(UserInform.class);
                callback.onUserLoaded(userInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DataAccess", "Failed to read user data.", error.toException());
                callback.onUserLoaded(null);
            }
        });
    }

    //데이터가 변경될때마다 실시간 호출되는 메서드
    public void observeUserByUid(String uid, final UserLoadCallback callback) {
        dataref.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInform userInfo = snapshot.getValue(UserInform.class);
                callback.onUserLoaded(userInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("DataAccess", "Failed to observe user data.", error.toException());
                callback.onUserLoaded(null);
            }
        });
    }




}
