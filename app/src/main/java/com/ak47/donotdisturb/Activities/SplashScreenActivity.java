package com.ak47.donotdisturb.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ak47.donotdisturb.R;

public class SplashScreenActivity extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity with No Layout
        SharedPreferences sharedPreferences = getSharedPreferences("initial_setup", MODE_PRIVATE);
        boolean initialSetupBoolean = sharedPreferences.getBoolean("initial_setup", false);

        if (initialSetupBoolean) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, InitialSetupActivity.class);
        }
        startActivity(intent);
        finish();

    }
}
