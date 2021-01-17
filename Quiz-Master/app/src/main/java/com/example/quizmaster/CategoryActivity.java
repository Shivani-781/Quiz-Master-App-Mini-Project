// -- CATEGORY ACTIVITY --

package com.example.quizmaster;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import static com.example.quizmaster.HomeActivity.catList;

public class CategoryActivity extends AppCompatActivity {

    // Initializing variable
    private GridView catGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Setting title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        // Enabling back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GridView catGrid = findViewById(R.id.catGridView);

        // Creating adapter to fill data in the grid view
        // create category item layout first to use adapter
        CatGridAdapter adapter = new CatGridAdapter(catList);
        catGrid.setAdapter(adapter);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If back button is clicked
        if (item.getItemId()==android.R.id.home)
        {
            CategoryActivity.this.finish();
        }
        // else return selected item
        return super.onOptionsItemSelected(item);
    }
}