// -- HOME ACTIVITY --

package com.example.quizmasteradmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

// Launcher Activity
public class HomeActivity extends AppCompatActivity {

    // Declaring activity
    private TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initializing variable
        appName = findViewById(R.id.appName);

        // Adding animation
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.myanim);
        appName.setAnimation(anim);

        // Moving to Main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            HomeActivity.this.finish();
            }
        }, 3000);

    }
}