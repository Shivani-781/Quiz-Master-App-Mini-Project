// -- CATEGORY ADAPTER CLASS --

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
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
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

// Recycler View Adapter
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    // Declaring variables
    private List<CategoryModel> cat_list; // List of categories


    public CategoryAdapter(List<CategoryModel> cat_list) {
        this.cat_list = cat_list;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Returning category item layout view
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder viewHolder, int pos) {

        // Getting the name of category
        String title = cat_list.get(pos).getName();
        viewHolder.setData(title, pos, this);
    }

    @Override
    public int getItemCount() {
        return cat_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Declaring variables
        private TextView catName;
        private ImageView delCat;
        private Dialog loadingDialog;
        private Dialog editDialog;
        private EditText tv_editCatName;
        private Button updateCatB;

        public ViewHolder(@NonNull View itemView) {
            // Initializing variables
            super(itemView);
            catName = itemView.findViewById(R.id.catName);
            delCat = itemView.findViewById(R.id.catDelete);

            // Progress bar
            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progress_bar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_bg);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Dialog to update category name
            editDialog = new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category_layout);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Initializing variables
            tv_editCatName = editDialog.findViewById(R.id.ec_cat_name);
            updateCatB = editDialog.findViewById(R.id.ec_add_btn);

        }

        private void setData(String title, int pos, CategoryAdapter adapter) {
            // Setting category item name
            catName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CategoryActivity.selected_cat_index = pos;
                    Intent intent = new Intent(itemView.getContext(), SetsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    tv_editCatName.setText(cat_list.get(pos).getName());
                    editDialog.show();

                    return false;
                }
            });

            updateCatB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tv_editCatName.getText().toString().isEmpty()){
                        tv_editCatName.setError("Enter Category Name");
                        return;
                    }
                    updateCategory(tv_editCatName.getText().toString(), pos, itemView.getContext(), adapter);
                }
            });

            // Defining actions on delete button
            delCat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Display alert before deletion
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext(), R.style.CustomAlertDialog).setTitle("Delete Category").setMessage("Do you want to delete this category?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Deleting after confirm
                            deleteCategory(pos, itemView.getContext(), adapter);
                        }
                    }).setNegativeButton("Cancel", null).setIconAttribute(android.R.attr.alertDialogIcon).show();

                    // Setting color of buttons in delete dialog
                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
                    dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);

                    // Setting layout to negative button to separate delete and cancel buttons
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });
        }

        // Function to delete category and updating database
        private void deleteCategory(final int id, Context context, CategoryAdapter adapter) {
            // Display loading bar
            loadingDialog.show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            int index = 1;

            // Update document
            Map<String, Object> catDoc = new ArrayMap<>();

            // Iterating over each category and adding into document if it is not to be deleted
            for(int j=0; j<cat_list.size(); j++) {
                if(j !=id) {
                    catDoc.put("CAT" + index + "_ID", cat_list.get(j).getId());
                    catDoc.put("CAT" + index + "_NAME", cat_list.get(j).getName());
                    index++;
                }

            }

            // Updating count
            catDoc.put("COUNT", index - 1);

            // Writing new document into firestore
            firestore.collection("QUIZ").document("Categories").set(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Category Deleted Successfully", Toast.LENGTH_SHORT).show();

                    // Deleting from local list also
                    CategoryActivity.catList.remove(id);

                    // Notifying adapter about deletion
                    adapter.notifyDataSetChanged();

                    // Dismissing dialog
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

        // Updating category name i database
        private void updateCategory(String catNewName, int pos, Context context, CategoryAdapter adapter) {

            editDialog.dismiss();
            loadingDialog.show();

            Map<String, Object> catData = new ArrayMap<>();
            catData.put("NAME", catNewName);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(cat_list.get(pos).getId()).update(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    // Updating name in categories document
                    Map<String, Object> catDoc = new ArrayMap<>();
                    catDoc.put("CAT" + (pos + 1) + "_NAME", catNewName);

                    firestore.collection("QUIZ").document("Categories").update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Category Name Changed Successfully!", Toast.LENGTH_SHORT).show();

                            // Updating name in local list
                            CategoryActivity.catList.get(pos).setName(catNewName);
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

    }
}
