// -- SETS ADAPTER CLASS --

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

import static com.example.quizmasteradmin.CategoryActivity.catList;
import static com.example.quizmasteradmin.CategoryActivity.selected_cat_index;
import static com.example.quizmasteradmin.SetsActivity.selected_set_index;

// Stores list of sets

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {

    private List<String> setIDs;

    public SetAdapter(List<String> setIDs) {
        this.setIDs = setIDs;
    }

    @NonNull
    @Override
    public SetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetAdapter.ViewHolder holder, int position) {
        String setID = setIDs.get(position);
        holder.setData(position, setID, this);
    }

    @Override
    public int getItemCount() {
        return setIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView setName;
        private ImageView deleteSetB;
        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setName = itemView.findViewById(R.id.catName);
            deleteSetB = itemView.findViewById(R.id.catDelete);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progress_bar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        private void setData(int pos, final String setID, SetAdapter adapter) {

            setName.setText("SET " + (pos + 1));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_set_index = pos;
                    Intent intent = new Intent(itemView.getContext(), QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            deleteSetB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext(), R.style.CustomAlertDialog).setTitle("Delete Set").setMessage("Do you want to delete this set?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteSet(pos, setID, itemView.getContext(), adapter);
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

        private void deleteSet(int pos, String setID, Context context, SetAdapter adapter) {

            loadingDialog.show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).collection(setID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    WriteBatch batch = firestore.batch();
                    for(QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String, Object> catDoc = new ArrayMap<>();
                            int index = 1;
                            for(int i=0; i<setIDs.size(); i++) {
                                if(i != pos) {
                                    catDoc.put("SET" + index + "_ID", setIDs.get(i));
                                    index++;
                                }
                            }
                            catDoc.put("SETS", index-1);
                            firestore.collection("QUIZ").document(catList.get(selected_cat_index).getId()).update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Set Deleted Successfully", Toast.LENGTH_SHORT).show();

                                    SetsActivity.setsIDs.remove(pos);
                                    catList.get(selected_cat_index).setNoOfSets(String.valueOf(SetsActivity.setsIDs.size()));
                                    adapter.notifyDataSetChanged();
                                    loadingDialog.dismiss();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
    }
}
