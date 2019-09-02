package com.veve.flowreader.views;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;

public class HelpActivity extends AppCompatActivity {

    long bookId;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bookId = intent.getIntExtra(Constants.BOOK_ID, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_help_landscape);
        } else {
            setContentView(R.layout.activity_help_portrait);
        }

        bookId = getIntent().getLongExtra(Constants.BOOK_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(HelpActivity.this, PageActivity.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
               intent.putExtra(Constants.BOOK_ID, getIntent().getLongExtra(Constants.BOOK_ID, -1));
               HelpActivity.this.startActivity(intent);
            }
        });
    }

}
