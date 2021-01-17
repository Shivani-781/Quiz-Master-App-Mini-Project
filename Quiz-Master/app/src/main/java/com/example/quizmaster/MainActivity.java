// -- MAIN ACTIVITY --

package com.example.quizmaster;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class MainActivity extends AppCompatActivity {

    // Declaring variables
    private TextView title;
    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing variables
        title = findViewById(R.id.mainTitle);
        start = findViewById(R.id.mainButton);

        // Setting font
        Typeface typeface = ResourcesCompat.getFont(this, R.font.arialbold);
        title.setTypeface(typeface);

        // Adding action to button
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Moving to category activity
                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                startActivity(intent);
            }
        });
    }
}