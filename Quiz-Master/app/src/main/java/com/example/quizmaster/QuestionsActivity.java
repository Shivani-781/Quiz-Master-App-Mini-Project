// -- QUESTIONS ACTIVITY --

package com.example.quizmaster;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.quizmaster.HomeActivity.catList;
import static com.example.quizmaster.HomeActivity.selected_cat_index;
import static com.example.quizmaster.SetsActivity.setsIDs;

public class QuestionsActivity extends AppCompatActivity implements View.OnClickListener {

    // Declaring variables
    private TextView question, qCount, timer;
    private Button option1, option2, option3, option4;
    private List<Question> questionList;
    private int quesNo;
    private CountDownTimer countDown;
    private int score;
    private FirebaseFirestore firestore;
    private int setNo;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // Initializing variables
        question= findViewById(R.id.question);
        qCount = findViewById(R.id.questionNo);
        timer = findViewById(R.id.countdown);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        // Setting action on click
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        // Loading bar
        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_bar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        // Initializing list
        questionList = new ArrayList<>();

        setNo = getIntent().getIntExtra("SetNo", 1);

        // Initializing firestore instance
        firestore = FirebaseFirestore.getInstance();

        // Get questions list
        getQuestionsList();

        // Initializing score
        score = 0;
    }

    private void getQuestionsList() {
        // Clearing list of previous questions
        questionList.clear();

        // Fetching questions from database
        firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(setNo)).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                    questionList.add(new Question(
                            quesDoc.getString("QUESTION"),
                            quesDoc.getString("A"),
                            quesDoc.getString("B"),
                            quesDoc.getString("C"),
                            quesDoc.getString("D"),
                            Integer.valueOf(quesDoc.getString("ANSWER"))

                    ));
                }

                // Setting questions
                setQuestion();

                // Dismissing progress bar after fetching
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

    public void setQuestion()  {
        // Display timer
        timer.setText(String.valueOf(30));
        // Set questions
        question.setText(questionList.get(0).getQuestion());
        option1.setText(questionList.get(0).getOptionA());
        option2.setText(questionList.get(0).getOptionB());
        option3.setText(questionList.get(0).getOptionC());
        option4.setText(questionList.get(0).getOptionD());

        // Question Count
        qCount.setText(1 + "/" + questionList.size());

        // Start timer
        startTimer();

        quesNo = 0;
    }

    private void startTimer()  {
        // Decrease time
        countDown = new CountDownTimer(32000, 1000) {
            @Override
            public void onTick(long l) {
                if (l < 30000) {
                    timer.setText(String.valueOf(l / 1000));
                }
                if (l < 30000) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            // Next Question
            @Override
            public void onFinish() {
                changeQuestion();
            }
        };

        // Restart timer
        countDown.start();

    }

    // Checking answer
    @Override
    public void onClick(View v) {

        int selectedOption = 0;

        switch (v.getId()) {
            case R.id.option1 :
                selectedOption = 1;
                break;
            case R.id.option2 :
                selectedOption = 2;
                break;
            case R.id.option3 :
                selectedOption = 3;
                break;
            case R.id.option4 :
                selectedOption = 4;
                break;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        countDown.cancel();
        checkAnswer(selectedOption, v);
    }

    private void checkAnswer(int selectedOption, View view)  {
        if(selectedOption == questionList.get(quesNo).getCorrectAns())  {
            ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            score++;
        }
        else {
            ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            switch(questionList.get(quesNo).getCorrectAns())
            {
                case 1:
                    option1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    option2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    option3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    option4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }
        }

        // Change question
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        }, 2000);


    }

    private void changeQuestion() {
        // If question available, diaplay question
        if(quesNo <questionList.size() - 1) {

            quesNo++;

            // Animating display of options
            playAnim(question, 0, 0);
            playAnim(option1, 0, 1);
            playAnim(option2, 0, 2);
            playAnim(option3, 0, 3);
            playAnim(option4, 0, 4);

            qCount.setText((quesNo + 1) + "/" + questionList.size());
            timer.setText(String.valueOf(30));
            startTimer();
        }
        // Else move to score activity
        else {
            Intent intent = new Intent(QuestionsActivity.this, ScoreActivity.class);
            intent.putExtra("Score", score + "/" + questionList.size());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            QuestionsActivity.this.finish();
        }

    }


    private void playAnim(final View view, final int value, final int viewNum) {
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(value == 0) {
                    switch (viewNum) {
                        case 0:
                            ((TextView) view).setText(questionList.get(quesNo).getQuestion());
                            break;
                        case 1:
                            ((Button) view).setText(questionList.get(quesNo).getOptionA());
                            break;
                        case 2:
                            ((Button) view).setText(questionList.get(quesNo).getOptionB());
                            break;
                        case 3:
                            ((Button) view).setText(questionList.get(quesNo).getOptionC());
                            break;
                        case 4:
                            ((Button) view).setText(questionList.get(quesNo).getOptionD());
                            break;
                    }

                    if(viewNum != 0) {
                        ((Button) view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                    playAnim(view, 1, viewNum);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        countDown.cancel();
    }

}