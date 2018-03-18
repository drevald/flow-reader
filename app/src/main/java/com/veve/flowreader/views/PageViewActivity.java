package com.veve.flowreader.views;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.djvu.DjvuDevicePageContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PageViewActivity extends AppCompatActivity {

    private static final String EXTRA_FILENAME_KEY = "filename";
    private static final int INDEX_INCREMENT = 1;
    private static final int INITIAL_PAGE_NUMBER = 1;
    private Book djvuBook;
    private DevicePageContext context;
    private SparseArray<Bitmap> cache;
    private FloatingActionButton fab;
    private ImageView iv;
    private int mPpageNo;
    private ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        cache = new SparseArray<Bitmap>();
        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        String newString = getStringExtra(savedInstanceState);

        final String fileName = newString;
        djvuBook = new DjvuBook(fileName);
        context = new DjvuDevicePageContext();
        iv = findViewById(R.id.page_image);
        iv.setVisibility(View.INVISIBLE);
        final AtomicInteger pageNumber = new AtomicInteger(INITIAL_PAGE_NUMBER);

        LoadPageTask loadPageTask = new LoadPageTask();
        fab.setVisibility(View.INVISIBLE);
        mPpageNo = pageNumber.get();
        loadPageTask.execute(pageNumber.getAndAdd(INDEX_INCREMENT));

        LoadPageTask loadPageTask1 = new LoadPageTask();
        loadPageTask1.execute(pageNumber.get());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.INVISIBLE);
                int pageNo = pageNumber.getAndAdd(INDEX_INCREMENT);
                LoadPageTask loadPageTask = new LoadPageTask();
                loadPageTask.execute(pageNo);
                LoadPageTask loadPageTask1 = new LoadPageTask();
                loadPageTask1.execute(pageNo+1);
                spinner.setVisibility(View.VISIBLE);
                iv.setVisibility(View.INVISIBLE);
                mPpageNo++;
            }
        });
    }

    @Nullable
    private String getStringExtra(Bundle savedInstanceState) {
        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString= extras.getString(EXTRA_FILENAME_KEY);
            }
        } else {
            newString= (String) savedInstanceState.getSerializable(EXTRA_FILENAME_KEY);
        }
        return newString;
    }

    class LoadPageTask extends AsyncTask<Integer,Void,Bitmap> {

        private int requestedPageNo;

        @Override
        protected Bitmap doInBackground(Integer... pageNumbers) {
            Bitmap bitmap = null;
            int pageNo = pageNumbers[0];
            requestedPageNo = pageNo;
            Log.d("ASYNC", "Executing async for " + pageNo);
            if (cache.get(pageNo) == null) {
                Log.d("ASYNC", "doesn't contain " + pageNo);
                BookPage page = djvuBook.getPage(pageNo);

                try {
                    bitmap = page.getAsBitmap(context);
                    cache.put(pageNo, bitmap);
                    cache.remove(pageNo-2);
                } catch (Error e) {
                    Log.d("ASYNC", "failed  to allocate memory for " + pageNo);
                }
            } else {
                Log.d("ASYNC", "contains " + pageNo);
                bitmap = cache.get(pageNo);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.d("ASYNC", "Execution complete");
            fab.setVisibility(View.VISIBLE);
            if (mPpageNo == requestedPageNo) {
                iv.setImageBitmap(bitmap);
                spinner.setVisibility(View.INVISIBLE);
                iv.setVisibility(View.VISIBLE);
            }
        }
    }

}
