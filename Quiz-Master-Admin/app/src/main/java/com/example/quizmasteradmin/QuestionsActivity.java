// -- QUESTIONS ACTIVITY --

package com.example.quizmasteradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.quizmasteradmin.CategoryActivity.catList;
import static com.example.quizmasteradmin.CategoryActivity.selected_cat_index;
import static com.example.quizmasteradmin.SetsActivity.selected_set_index;
import static com.example.quizmasteradmin.SetsActivity.setsIDs;

public class QuestionsActivity extends AppCompatActivity {

    // Declaring variables
    private RecyclerView quesView;
    private Button addQues;
    public static List<QuestionModel> quesList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // Setting title bar
        Toolbar toolbar = findViewById(R.id.q_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        // Display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing variable
        quesView = findViewById(R.id.quest_recycler);
        addQues = findViewById(R.id.addQues);

        // Progress bar
        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Adding question
        addQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionsActivity.this, QuestionDetailsActivity.class);
                intent.putExtra("ACTION", "ADD");
                startActivity(intent);

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        quesView.setLayoutManager(layoutManager);

        firestore = FirebaseFirestore.getInstance();

        // Loading questions
        loadQuestions();

    }


    private void loadQuestions() {
        // Clearing list
        quesList.clear();
        // Display loading bar
        loadingDialog.show();

        // Adding question in database
        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                    docList.put(doc.getId(), doc);
                }
                QueryDocumentSnapshot quesListDoc = docList.get("QUESTIONS_LIST");

                String count = quesListDoc.getString("COUNT");

                for(int i=0; i<Integer.valueOf(count); i++) {
                    String quesID = quesListDoc.getString("Q" + (i + 1) + "_ID");
                    QueryDocumentSnapshot quesDoc = docList.get(quesID);
                    quesList.add(new QuestionModel(
                            quesID,
                            quesDoc.getString("QUESTION"),
                            quesDoc.getString("A"),
                            quesDoc.getString("B"),
                            quesDoc.getString("C"),
                            quesDoc.getString("D"),
                            Integer.valueOf(quesDoc.getString("ANSWER"))

                    ));
                }
                adapter = new QuestionAdapter(quesList);
                quesView.setAdapter(adapter);
                loadingDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(adapter != null) {
            // Notifying adapter
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}