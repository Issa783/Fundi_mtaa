package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Delay for splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity
                Intent intent = new Intent(SplashActivity.this, ChooseRoleActivity.class);
                startActivity(intent);
                finish(); // Finish splash activity to prevent user from going back to it
            }
        }, SPLASH_DELAY);
    }
}
