package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.io.File;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.djvu.DjvuDevicePageContext;

import org.opencv.android.OpenCVLoader;

import java.util.concurrent.atomic.AtomicInteger;

public class PageViewActivity extends AppCompatActivity {

    private static final String EXTRA_FILENAME_KEY = "filename";
    private static final int INDEX_INCREMENT = 1;
    private static final int INITIAL_PAGE_NUMBER = 1;
    public static final String PAGENO_EXTRA = "pageno";
    public static final String BITMAP_KEY = "bitmap";
    public static final String BITMAP_EXTRA = BITMAP_KEY;
    private Book djvuBook;
    private DevicePageContext context;
    private SparseArray<Bitmap> cache;
    private ImageView iv;
    private AtomicInteger mPageNo;
    private ProgressBar spinner;
    private String filename;
    private Bitmap bmp;
    private double actionDownBegin = 0;

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(PAGENO_EXTRA, mPageNo.intValue());
        outState.putString(EXTRA_FILENAME_KEY, filename);
        outState.putParcelable(BITMAP_KEY, bmp);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OpenCVLoader.initDebug();
        cache = new SparseArray<>();
        setContentView(R.layout.activity_page_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPageNo = new AtomicInteger(INITIAL_PAGE_NUMBER);
        spinner = findViewById(R.id.progressBar1);
        iv = findViewById(R.id.page_image);

        if (savedInstanceState != null) {
            mPageNo = new AtomicInteger(savedInstanceState.getInt(PAGENO_EXTRA));
            bmp = savedInstanceState.getParcelable(BITMAP_EXTRA);
            iv.setImageBitmap(bmp);
            cache.put(mPageNo.intValue(), bmp);
            spinner.setVisibility(View.INVISIBLE);
            iv.setVisibility(View.VISIBLE);
        }


        spinner.setVisibility(View.VISIBLE);

        String newString = getStringExtra(savedInstanceState);
        filename = newString;

        final String fileName = newString;
        setTitle(new File(fileName).getName());
        djvuBook = new DjvuBook(fileName);
        context = new DjvuDevicePageContext();

        iv.setVisibility(View.INVISIBLE);

        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int width = view.getWidth();

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                    actionDownBegin = motionEvent.getX();
                } else if ( motionEvent.getAction() == MotionEvent.ACTION_UP ) {
                    if ( motionEvent.getX() > actionDownBegin + width / 3  ) {
                        nextPage();
                    } else if (motionEvent.getX() < actionDownBegin - width / 3 ) {
                        previousPage();
                    }
                }

                return false;
            }
        });

        LoadPageTask loadPageTask1 = new LoadPageTask();
        loadPageTask1.execute(mPageNo.get());

        LoadPageTask loadPageTask = new LoadPageTask();
        loadPageTask.execute(mPageNo.get() + 1);
        
    }

    private void previousPage() {
        int oldValue = mPageNo.intValue();
        if (oldValue > 1){

            LoadPageTask loadPageTask = new LoadPageTask();
            loadPageTask.execute(oldValue);
            if (oldValue>1) {
                     LoadPageTask loadPageTask1 = new LoadPageTask();
                     loadPageTask1.execute(oldValue-1);
                     spinner.setVisibility(View.VISIBLE);
                     iv.setVisibility(View.INVISIBLE);
              }
              mPageNo.decrementAndGet();
        }
    }

    private void nextPage() {
        mPageNo.incrementAndGet();
        LoadPageTask loadPageTask = new LoadPageTask();
        loadPageTask.execute(mPageNo.get());
        LoadPageTask loadPageTask1 = new LoadPageTask();
        loadPageTask1.execute(mPageNo.get()+1);
        spinner.setVisibility(View.VISIBLE);
        iv.setVisibility(View.INVISIBLE);

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
            newString = getIntentData(newString);
        } else {
            newString= (String) savedInstanceState.getSerializable(EXTRA_FILENAME_KEY);
            newString = getIntentData(newString);
        }
        return newString;
    }

    private String getIntentData(String newString) {
        if (newString == null) {
            Intent intent = getIntent();
            Uri data = intent.getData();
            newString = data.getPath();
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

            if (cache.get(pageNo) == null) {

                BookPage page = djvuBook.getPage(pageNo);

                try {
                    bitmap = page.getAsBitmap(context);
                    cache.put(pageNo, bitmap);
                    cache.remove(pageNo-2);
                } catch (Error e) {
                    Log.d("ASYNC", "failed  to allocate memory for " + pageNo);
                }
            } else {

                bitmap = cache.get(pageNo);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (mPageNo.intValue() == requestedPageNo) {
                bmp = bitmap;
                iv.setImageBitmap(bitmap);
                spinner.setVisibility(View.INVISIBLE);
                iv.setVisibility(View.VISIBLE);
            }
        }
    }

}

