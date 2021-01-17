package com.example.quizmaster;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.example.quizmaster.HomeActivity.catList;
import static com.example.quizmaster.HomeActivity.selected_cat_index;

public class SetsActivity extends AppCompatActivity {

    // Declaring variables
    private GridView sets_grid;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    public static List<String> setsIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        // Setting title
        Toolbar toolbar = findViewById(R.id.settoolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(catList.get(selected_cat_index).getName());

        // Displaying back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing grid
        sets_grid = findViewById(R.id.sets_gridView);

        // Progress bar
        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        // Loading data
        firestore = FirebaseFirestore.getInstance();
        loadSets();

    }

    public void loadSets() {

        // Clearing sets list
        setsIDs.clear();

        // Fetching set details
        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long noOfSets = (long) documentSnapshot.get("SETS");
                for(int i=1; i <= noOfSets; i++) {
                    setsIDs.add(documentSnapshot.getString("SET" + i + "_ID"));
                }

                // Creating adapter to fetch each set item
                SetsAdapter adapter = new SetsAdapter(setsIDs.size());
                sets_grid.setAdapter(adapter);

                // Dismissing progress bar after loading
                loadingDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // if back button is clicked
        if(item.getItemId() == android.R.id.home)
        {
            SetsActivity.this.finish();
        }
        // else return set item seleted
        return super.onOptionsItemSelected(item);
    }
}