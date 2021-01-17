// -- QUESTION ADAPTER CLASS --

package com.example.quizmasteradmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import static com.example.quizmasteradmin.CategoryActivity.catList;
import static com.example.quizmasteradmin.CategoryActivity.selected_cat_index;
import static com.example.quizmasteradmin.QuestionsActivity.quesList;
import static com.example.quizmasteradmin.SetsActivity.selected_set_index;
import static com.example.quizmasteradmin.SetsActivity.setsIDs;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    // Initializing Variables
    private List<QuestionModel> ques_list;

    public QuestionAdapter(List<QuestionModel> ques_list) {
        this.ques_list = ques_list;
    }

    @NonNull
    @Override
    // Get Questions
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    // Set questions
    public void onBindViewHolder(@NonNull QuestionAdapter.ViewHolder holder, int pos) {
        holder.setData(pos, this);
    }

    @Override
    public int getItemCount() {
        return ques_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Declaring variables
        private TextView title;
        private ImageView deleteB;
        private Dialog loadingDialog;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initializing variables
            title = itemView.findViewById(R.id.catName);
            deleteB = itemView.findViewById(R.id.catDelete);

            // Progress bar
            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progress_bar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        private void setData(int pos, QuestionAdapter adapter) {

            // Setting title
            title.setText("QUESTION " + (pos + 1));

            // Moving to Question Details upon click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(), QuestionDetailsActivity.class);
                    intent.putExtra("ACTION", "EDIT");
                    intent.putExtra("Q_ID", pos);
                    itemView.getContext().startActivity(intent);
                }
            });

            // Deleting questions
            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext(), R.style.CustomAlertDialog).setTitle("Delete Question").setMessage("Do you want to delete this question?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteQuestion(pos, itemView.getContext(), adapter);
                        }
                    }).setNegativeButton("Cancel", null).setIconAttribute(android.R.attr.alertDialogIcon).show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
                    dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });
        }

        private void deleteQuestion(int pos, Context context, QuestionAdapter adapter) {

            // Progress bar
            loadingDialog.show();

            // Accessing database and deleting questions
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document(quesList.get(pos).getQuesID()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Map<String, Object> quesDoc = new ArrayMap<>();
                    int index = 1;
                    for(int i=0; i<quesList.size(); i++) {
                        if(i != pos) {
                            quesDoc.put("Q" + index + "_ID", quesList.get(i).getQuesID());
                            index++;
                        }
                    }
                    quesDoc.put("COUNT", String.valueOf(index-1));

                    firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setsIDs.get(selected_set_index)).document("QUESTIONS_LIST").set(quesDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Question Deleted Successfully!", Toast.LENGTH_SHORT).show();
                            quesList.remove(pos);
                            adapter.notifyDataSetChanged();

                            // Dismissing dialog
                            loadingDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                            // Dismissing dialog
                            loadingDialog.dismiss();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Dismissing dialog
                    loadingDialog.dismiss();
                }
            });
        }
    }
}
