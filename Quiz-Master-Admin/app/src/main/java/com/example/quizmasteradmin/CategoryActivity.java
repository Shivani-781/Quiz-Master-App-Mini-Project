// -- CATEGORY ACTIVITY --

package com.example.quizmasteradmin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Category Activity
public class CategoryActivity extends AppCompatActivity {

    // Declaring variables
    private RecyclerView cat_recycler_view;
    private Button addCat;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index = 0;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addCatDialog;
    private EditText dialogCatName;
    private Button dialogAddB;
    private CategoryAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Set title of activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        // Initializing variables
        cat_recycler_view = findViewById(R.id.cat_recycler);
        addCat = findViewById(R.id.addCat);

        // Initializing Progress bar
        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initializing Dialog for adding new category
        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_category_dialog);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initializing variables
        dialogCatName = addCatDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addCatDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        // Adding action to add category button
        addCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear the name and Display dialog
                dialogCatName.getText().clear();
                addCatDialog.show();
            }
        });

        // Dialog for adding new category
        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Error if empty
                if(dialogCatName.getText().toString().isEmpty()){
                    dialogCatName.setError("Enter Category Name");
                    return;
                }
                else {
                    // Calling function to add category to database
                    addNewCategory(dialogCatName.getText().toString());
                }
            }
        });

        // Creating a linear layout of category lists
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycler_view.setLayoutManager(layoutManager);

        // Loading data
        loadData();

    }

    // Fetching the categories from the database
    private void loadData() {

        // Displaying progress bar
        loadingDialog.show();

        // Clear list
        catList.clear();

        // Fetching from Firestore database
        firestore.collection("QUIZ").document("Categories").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // Checking if categories exist or not and fetching them using count
                    if(doc.exists()) {
                        long count = (long) doc.get("COUNT");
                        for (int i=1; i<=count; i++) {
                            String catName = doc.getString("CAT" + i + "_NAME");
                            String catId = doc.getString("CAT" + i + "_ID");
                            catList.add(new CategoryModel(catId, catName, "0", "1"));
                        }
                        // After fetching, set adapter
                        adapter = new CategoryAdapter(catList);
                        cat_recycler_view.setAdapter(adapter);
                    }
                    else {
                        Toast.makeText(CategoryActivity.this, "No Category Document Exists!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Dismissing dialog after fetching
                loadingDialog.dismiss();
            }
        });
    }

    private void addNewCategory(String title) {
        // Dismiss adding category dialog and display loading dialog while fetching new set of categories
        addCatDialog.dismiss();
        loadingDialog.show();

        // Creating new document in database
        Map<String, Object> catData = new ArrayMap<>();
        catData.put("NAME", title);
        catData.put("SETS", 0);
        catData.put("COUNTER", "1");

        String doc_id = firestore.collection("QUIZ").document().getId();
        Task<Void> quiz = firestore.collection("QUIZ").document(doc_id).set(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String, Object> catDoc = new ArrayMap<>();
                catDoc.put("CAT" + (catList.size() + 1) + "_NAME", title);
                catDoc.put("CAT" + (catList.size() + 1) + "_ID", doc_id);
                catDoc.put("COUNT", catList.size() + 1);

                // Updating categories in firestore
                firestore.collection("QUIZ").document("Categories").update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    Toast.makeText(CategoryActivity.this, "Category Added Successfully", Toast.LENGTH_SHORT).show();

                    // Adding to the local list
                    catList.add(new CategoryModel(doc_id, title, "0", "1"));

                    // Updating the category listing - Adapter, it displays the added category at the end of the list
                    adapter.notifyItemInserted(catList.size());

                    // Dismiss progress bar
                    loadingDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });


    }

}