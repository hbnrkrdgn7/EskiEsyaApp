package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Logo animasyonu
        View cardLogo = findViewById(R.id.cardLogo);
        View tvAppName = findViewById(R.id.tvAppName);
        View tvTagline = findViewById(R.id.tvTagline);

        // Başlangıçta görünmez
        cardLogo.setAlpha(0f);
        tvAppName.setAlpha(0f);
        tvTagline.setAlpha(0f);

        // Logo fade in + scale
        AnimationSet logoAnim = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        logoAnim.addAnimation(scale);
        logoAnim.addAnimation(fadeIn);
        logoAnim.setDuration(800);
        logoAnim.setFillAfter(true);
        cardLogo.startAnimation(logoAnim);
        cardLogo.setAlpha(1f);

        // App name fade in (gecikmeli)
        AlphaAnimation nameAnim = new AlphaAnimation(0f, 1f);
        nameAnim.setDuration(600);
        nameAnim.setStartOffset(400);
        nameAnim.setFillAfter(true);
        tvAppName.startAnimation(nameAnim);
        tvAppName.setAlpha(1f);

        // Tagline fade in (daha gecikmeli)
        AlphaAnimation tagAnim = new AlphaAnimation(0f, 1f);
        tagAnim.setDuration(600);
        tagAnim.setStartOffset(700);
        tagAnim.setFillAfter(true);
        tvTagline.startAnimation(tagAnim);
        tvTagline.setAlpha(1f);

        // 2.5 saniye sonra yönlendir
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (user != null) {
                // Kullanıcı zaten giriş yapmış -> Ana sayfaya git
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Giriş yapmamış -> Login sayfasına git
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}
