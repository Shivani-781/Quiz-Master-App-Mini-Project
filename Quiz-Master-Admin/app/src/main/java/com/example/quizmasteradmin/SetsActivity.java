// -- SETS ACTIVITY --

package com.example.quizmasteradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.quizmasteradmin.CategoryActivity.catList;
import static com.example.quizmasteradmin.CategoryActivity.selected_cat_index;

public class SetsActivity extends AppCompatActivity {

    // Declaring variable
    private RecyclerView setsView;
    private Button addSetB;
    private SetAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    public static List<String> setsIDs = new ArrayList<>();
    public static int selected_set_index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        // Setting title bar
        Toolbar toolbar = findViewById(R.id.sa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");

        // Displaying back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing variable
        setsView = findViewById(R.id.sets_recycler);
        addSetB = findViewById(R.id.addSet);

        // Progress bar
        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addSetB.setText("Add New Set");

        addSetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSet();
            }
        });

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();

    }

    private void loadSets() {
        // clear list
        setsIDs.clear();

        // display loading bar
        loadingDialog.show();

        // Adding sets in database
        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long noOfSets = (long) documentSnapshot.get("SETS");
                for(int i=1; i <= noOfSets; i++) {
                    setsIDs.add(documentSnapshot.getString("SET" + i + "_ID"));
                }

                catList.get(selected_cat_index).setSetCounter(documentSnapshot.getString("COUNTER"));
                catList.get(selected_cat_index).setNoOfSets(String.valueOf(noOfSets));

                adapter = new SetAdapter(setsIDs);
                setsView.setAdapter(adapter);

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
    private void addNewSet() {
        loadingDialog.show();
        String curr_cat_id = catList.get(selected_cat_index).getId();
        String curr_counter = catList.get(selected_cat_index).getSetCounter();
        Map<String, Object> qData = new ArrayMap<>();
        qData.put("COUNT", "0");

        firestore.collection("QUIZ").document(curr_cat_id).collection(curr_counter).document("QUESTIONS_LIST").set(qData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> catDoc = new ArrayMap<>();
                catDoc.put("COUNTER", String.valueOf(Integer.valueOf(curr_counter) + 1));
                catDoc.put("SET" + (setsIDs.size() + 1) + "_ID", curr_counter);
                catDoc.put("SETS", setsIDs.size()+1);

                firestore.collection("QUIZ").document(curr_cat_id).update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetsActivity.this, "Set Added Successfully!", Toast.LENGTH_SHORT).show();
                        setsIDs.add(curr_counter);
                        catList.get(selected_cat_index).setNoOfSets(String.valueOf(setsIDs.size()));
                        catList.get(selected_cat_index).setSetCounter(String.valueOf(Integer.valueOf(curr_counter) + 1));

                        adapter.notifyItemInserted(setsIDs.size());
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
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        // else return item
        return super.onOptionsItemSelected(item);
    }
}
