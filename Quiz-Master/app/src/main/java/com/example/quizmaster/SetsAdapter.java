// -- SETS ACTIVITY --

package com.example.quizmaster;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SetsAdapter extends BaseAdapter {

    // Declaring variable
    private int numOfSets;

    // Constructor
    public SetsAdapter(int numOfSets) {
        this.numOfSets = numOfSets;
    }


    @Override
    // Return count of sets
    public int getCount() {
        return numOfSets;
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout, parent, false);
        }
        else {
            view = convertView;
        }

        // Moving to questions activity
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(), QuestionsActivity.class);
                intent.putExtra("SetNo", position);
                parent.getContext().startActivity(intent);
            }
        });

        // Set text
        ((TextView) view.findViewById(R.id.setNo)).setText(String.valueOf(position+1));

        return view;
    }
}
