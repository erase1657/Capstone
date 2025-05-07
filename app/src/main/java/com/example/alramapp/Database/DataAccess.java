package com.example.alramapp.Database;

import static android.content.ContentValues.TAG;

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
    private DatabaseReference dataref = database.getReference();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    //유저 정보 쓰기
    public void UpdateUser(FirebaseUser user) {

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






}
