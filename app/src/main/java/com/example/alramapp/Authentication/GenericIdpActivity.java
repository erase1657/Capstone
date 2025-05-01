package com.example.alramapp.Authentication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class GenericIdpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 필요하면 setContentView(R.layout.xxxx);

        firebaseAuth = FirebaseAuth.getInstance();

        String provider = getIntent().getStringExtra("provider");

        if (provider != null) {
            switch (provider) {
                case "twitter":
                    twitterSignIn();
                    break;
                case "github":
                    githubSignIn();
                    break;
                default:
                    finish();
                    break;
            }
        } else {
            finish();
        }
    }
    private void twitterSignIn() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", "fr");

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    // 로그인 성공 시 처리, 예시:
                    FirebaseUser user = authResult.getUser();
                    // TODO: 로그인 성공 후 작업 (예: 로그인 완료 알림, 다음 액티비티 이동)
                    finish(); // 이 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    // 로그인 실패 시 처리
                    e.printStackTrace();
                    finish();
                });
    }

    private void githubSignIn() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");
        provider.addCustomParameter("login", "your-email@gmail.com");
        List<String> scopes = new ArrayList<>();
        scopes.add("user:email");
        provider.setScopes(scopes);

        firebaseAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    // 로그인 성공 시 처리
                    FirebaseUser user = authResult.getUser();
                    // TODO: 로그인 성공 후 작업
                    finish();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    finish();
                });
    }



    public void getPendingAuthResult() {
        // [START auth_oidc_pending_result]
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                                    // The OAuth secret can be retrieved by calling:
                                    // ((OAuthCredential)authResult.getCredential()).getSecret().
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            // See below.
        }
        // [END auth_oidc_pending_result]
    }

    public void signInWithProvider(OAuthProvider.Builder provider) {
        // [START auth_oidc_provider_signin]
        firebaseAuth
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // User is signed in.
                                // IdP data available in
                                // authResult.getAdditionalUserInfo().getProfile().
                                // The OAuth access token can also be retrieved:
                                // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                                // The OAuth secret can be retrieved by calling:
                                // ((OAuthCredential)authResult.getCredential()).getSecret().
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure.
                            }
                        });
        // [END auth_oidc_provider_signin]
    }




}
