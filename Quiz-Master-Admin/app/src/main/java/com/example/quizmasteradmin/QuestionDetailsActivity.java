// -- QUESTION DETAILS ACTIVITY --

package com.example.quizmasteradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import static com.example.quizmasteradmin.CategoryActivity.catList;
import static com.example.quizmasteradmin.CategoryActivity.selected_cat_index;
import static com.example.quizmasteradmin.QuestionsActivity.quesList;
import static com.example.quizmasteradmin.SetsActivity.selected_set_index;
import static com.example.quizmasteradmin.SetsActivity.setsIDs;

public class QuestionDetailsActivity extends AppCompatActivity {

    // Declaring activity
    private EditText ques, optionA, optionB, optionC, optionD, answer;
    private Button addQB;
    private String qStr, aStr, bStr, cStr, dStr, ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore firestore;
    private String action;
    private int qID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        // Setting title bar
        Toolbar toolbar = findViewById(R.id.qdetails_toolbar);
        setSupportActionBar(toolbar);

        // Displaying back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializing variables
        ques = findViewById(R.id.question);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        answer = findViewById(R.id.answer);
        addQB = findViewById(R.id.addQB);

        // Progress bar
        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");

        // Updating questions
        if(action.compareTo("EDIT") == 0) {
            qID = getIntent().getIntExtra("Q_ID", 0);
            loadData(qID);
            getSupportActionBar().setTitle("Question " + (qID + 1));
            addQB.setText("UPDATE");
        }
        // Adding questions
        else {
            getSupportActionBar().setTitle("Question " + (quesList.size() + 1));
            addQB.setText("ADD");
        }

        // Adding questions on click
        addQB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qStr = ques.getText().toString();
                aStr = optionA.getText().toString();
                bStr = optionB.getText().toString();
                cStr = optionC.getText().toString();
                dStr = optionD.getText().toString();
                ansStr = answer.getText().toString();

                if(qStr.isEmpty()) {
                    ques.setError("Enter Question");
                    return;
                }
                if(aStr.isEmpty()) {
                    optionA.setError("Enter Option A");
                    return;
                }
                if(bStr.isEmpty()) {
                    optionB.setError("Enter Option B");
                    return;
                }
                if(cStr.isEmpty()) {
                    optionC.setError("Enter Option C");
                    return;
                }
                if(dStr.isEmpty()) {
                    optionD.setError("Enter Option D");
                    return;
                }
                if(ansStr.isEmpty()) {
                    answer.setError("Enter Correct Answer");
                    return;
                }

                if(action.compareTo("EDIT") == 0) {
                    editQuestion();
                }
                else {
                    addNewQuestion();
                }
            }
        });

    }
    private void addNewQuestion() {

        // Progress bar
        loadingDialog.show();

        Map<String, Object> quesData = new ArrayMap<>();
        quesData.put("QUESTION", qStr);
        quesData.put("A", aStr);
        quesData.put("B", bStr);
        quesData.put("C", cStr);
        quesData.put("D", dStr);
        quesData.put("ANSWER", ansStr);

        // Accessing questions document
        String doc_id = firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document().getId();

        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document(doc_id).set(quesData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> quesDoc = new ArrayMap<>();
                quesDoc.put("Q" + (quesList.size() + 1) + "_ID", doc_id);
                quesDoc.put("COUNT", String.valueOf(quesList.size() + 1));

                firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST").update(quesDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(QuestionDetailsActivity.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();
                        quesList.add(new QuestionModel(
                                doc_id,
                                qStr, aStr, bStr, cStr, dStr, Integer.valueOf(ansStr)
                        ));

                        // Dismissing progress bar
                        loadingDialog.dismiss();

                        QuestionDetailsActivity.this.finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }
    // Loading question list
    private void loadData(int id) {
        ques.setText(quesList.get(id).getQuestion());
        optionA.setText(quesList.get(id).getOptionA());
        optionB.setText(quesList.get(id).getOptionB());
        optionC.setText(quesList.get(id).getOptionC());
        optionD.setText(quesList.get(id).getOptionD());
        answer.setText(String.valueOf(quesList.get(id).getAnswer()));
    }

    // Editing question
    private void editQuestion() {
        loadingDialog.show();
        Map<String, Object> quesData = new ArrayMap<>();
        quesData.put("QUESTION", qStr);
        quesData.put("A", aStr);
        quesData.put("B", bStr);
        quesData.put("C", cStr);
        quesData.put("D", dStr);
        quesData.put("ANSWER", ansStr);

        // Updating in database
        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document(quesList.get(qID).getQuesID()).set(quesData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(QuestionDetailsActivity.this, "Question Updated Successfully!", Toast.LENGTH_SHORT).show();

                quesList.get(qID).setQuestion(qStr);
                quesList.get(qID).setOptionA(aStr);
                quesList.get(qID).setOptionB(bStr);
                quesList.get(qID).setOptionC(cStr);
                quesList.get(qID).setOptionD(dStr);
                quesList.get(qID).setAnswer(Integer.valueOf(ansStr));

                loadingDialog.dismiss();
                QuestionDetailsActivity.this.finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // If back button is clicked
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        // else return item
        return super.onOptionsItemSelected(item);
    }
}