package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

import android.view.ViewTreeObserver.*;

public class PageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        final GridView gridView = findViewById(R.id.grid);
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i("tag", ""+gridView.getWidth());
                if (gridView.getAdapter() == null)
                    gridView.setAdapter(new BookGridAdapter(gridView.getWidth()));
            }
        });
    }

    class BookGridAdapter extends BaseAdapter {

        Bitmap[] bitmaps;

        public BookGridAdapter(int width) {

            DevicePageContext pageContext = new DevicePageContextImpl(width);
            Book book = BooksCollection.getInstance().getBooks().get(0);
            bitmaps = new Bitmap[book.getPagesCount()];
            for (int i=0; i<book.getPagesCount(); i++) {
                BookPage bookPage = book.getPage(i);
                bitmaps[i] = bookPage.getAsBitmap(pageContext);
            }
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return bitmaps.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view = new ImageView(PageActivity.this.getApplicationContext());
            view.setImageBitmap(bitmaps[position]);
            return view;
        }
    }

}
