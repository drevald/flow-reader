package com.veve.flowreader.views;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;

public class HelpActivity extends BaseActivity {

    long bookId;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(getClass().getName(), getClass().getName() + "onNewIntent# " + this.hashCode());
        bookId = intent.getIntExtra(Constants.BOOK_ID, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(getClass().getName(), getClass().getName() + "onCreate# " + this.hashCode());

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
               intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
               intent.putExtra(Constants.BOOK_ID, getIntent().getLongExtra(Constants.BOOK_ID, -1));
               HelpActivity.this.startActivity(intent);
            }
        });
    }

}
