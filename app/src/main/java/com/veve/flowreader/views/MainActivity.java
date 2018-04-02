package com.veve.flowreader.views;

import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.impl.mocksimple.MockBook;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BookListAdapter bookListAdapter = new BookListAdapter();

    BookGridAdapter bookGridAdapter = new BookGridAdapter();

    int columnsNumber;

    SharedPreferences preferences;

    private static final String VIEW_TYPE = "VIEW_TYPE";

    private static final int LIST_VIEW = 0;

    private static final int GRID_VIEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        preferences = getPreferences(MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i("MainActivity", "Is toolbar visible" + getSupportActionBar().isShowing());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentOne = new Intent(MainActivity.this, BrowseFilesActivity.class);
                intentOne.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentOne);
            }
        });

        final GridView gridView = findViewById(R.id.grid);

        if (!preferences.contains(VIEW_TYPE)) {
            Log.d(getClass().getName(), "View type preference missing. Setting list as default");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(VIEW_TYPE, GRID_VIEW);
            editor.apply();
        }

        if (preferences.getInt(VIEW_TYPE, LIST_VIEW) == LIST_VIEW) {
            Log.d(getClass().getName(), "Preference view is list");
            gridView.setNumColumns(1);
            gridView.setAdapter(bookListAdapter);
        } else {
            gridView.setAdapter(bookGridAdapter);
            Log.d(getClass().getName(),
                    String.format("Preference view is grid. Adapter class is %S",
                            gridView.getAdapter()));
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(getClass().getName(), String.format("Clicked view id %d position %d id %d",
                        view.getId(), position, id));
                Intent intentTwo = new Intent(MainActivity.this, PageActivity.class);
                intentTwo.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentTwo);
            }
        });

        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                columnsNumber = (int) (gridView.getWidth()/
                        (Constants.BOOK_THUMB_WIDTH + 2 * Constants.BOOK_THUMB_HOR_PADDING));
                if(preferences.getInt(VIEW_TYPE, LIST_VIEW) == LIST_VIEW){
                    gridView.setNumColumns(1);
                } else {
                    gridView.setNumColumns(columnsNumber);
                }
            }
        });

        ImageButton listBooksButton = findViewById(R.id.books_list);
        listBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "List View");
                gridView.setNumColumns(1);
                gridView.setAdapter(bookListAdapter);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(VIEW_TYPE, LIST_VIEW);
                editor.apply();
            }
        });

        ImageButton gridBooksButton = findViewById(R.id.books_grid);
        gridBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "Grid View");
                gridView.setNumColumns(columnsNumber);
                gridView.setAdapter(bookGridAdapter);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(VIEW_TYPE, GRID_VIEW);
                editor.apply();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class BookListAdapter extends BaseAdapter {

        private BookListAdapter instance;
        private List<Book> booksList;

        private BookListAdapter() {
            Log.i(this.getClass().getName(), "Constructing BookListAdapter");
            booksList = BooksCollection.getInstance().getBooks();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return booksList.size();
        }

        @Override
        public Object getItem(int position) {
            return booksList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return booksList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.items_list, container, false);
            }
            TextView textView = (TextView)((ConstraintLayout)convertView).getChildAt(0);
                textView.setText(booksList.get(position).getName());
            return convertView;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object object) {
            return true;
        }

    }

    public class BookGridAdapter extends BaseAdapter {

        private BookListAdapter instance;
        private List<Book> booksList;

        private BookGridAdapter() {
            Log.i(this.getClass().getName(), "Constructing BookListAdapter");
            booksList = BooksCollection.getInstance().getBooks();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return booksList.size();
        }

        @Override
        public Object getItem(int position) {
            return booksList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return booksList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.items_list, container, false);
            }
            convertView = getLayoutInflater().inflate(R.layout.book_preview, container, false);
            TextView textView = convertView.findViewById(R.id.book);
            textView.setText(booksList.get(position).getName());
            textView.setTextSize(12);
            textView.setTextColor(Color.WHITE);

            return convertView;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object object) {
            return true;
        }

    }

}
