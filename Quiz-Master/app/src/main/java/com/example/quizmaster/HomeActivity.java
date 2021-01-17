// -- HOME ACTIVITY --

package com.example.quizmaster;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // Declaring variables
    private TextView appName;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index = 0;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initializing variable
        appName = findViewById(R.id.appName);

        // Setting font
        Typeface typeface = ResourcesCompat.getFont(this, R.font.arialbold);
        appName.setTypeface(typeface);

        // Adding Animation
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.myanim);
        appName.setAnimation(anim);

        // Initializing firestore
        firestore = FirebaseFirestore.getInstance();

        // Start loading data from firestore
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }).start();
    }

    // Fetch data
    private void loadData() {

        // Clear category list
        catList.clear();

        firestore.collection("QUIZ").document("Categories").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists()) {

                        // Fetching categories
                        long count = (long) doc.get("COUNT");
                        for (int i=1; i<=count; i++) {
                            String catName = doc.getString("CAT" + i + "_NAME");
                            String catID = doc.getString("CAT" + i + "_ID");
                            catList.add(new CategoryModel(catID, catName));
                        }

                        // Moving to main activity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                startActivity(intent);
                                HomeActivity.this.finish();
                            }
                        }, 1000);

                    }
                    else {
                        Toast.makeText(HomeActivity.this, "No Category Document Exists!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(HomeActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}