// -- CATEGORY ADAPTER CLASS --

package com.example.quizmaster;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CatGridAdapter extends BaseAdapter {

    // Declaring list
    private List<CategoryModel> catList;

    // Constructor
    public CatGridAdapter(List<CategoryModel> catList) {
        this.catList = catList;
    }

    @Override
    // Return count of categories
    public int getCount() {
        return catList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view;
        // Get view
        if(convertView == null)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout, parent, false);
        }
        else {
            view = convertView;
        }

        // Moving to the category clicked - sets activity
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.selected_cat_index = position;
                Intent intent = new Intent(parent.getContext(), SetsActivity.class);
                parent.getContext().startActivity(intent);
            }
        });

        // Setting text
        ((TextView) view.findViewById(R.id.catName)).setText(catList.get(position).getName());

        // Setting background color
        view.setBackgroundResource(R.drawable.border);

        return view;
    }
}
