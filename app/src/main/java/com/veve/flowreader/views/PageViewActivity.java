package com.veve.flowreader.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.djvu.DjvuDevicePageContext;

import java.util.concurrent.atomic.AtomicInteger;

public class PageViewActivity extends AppCompatActivity {

    private Book djvuBook;
    private DevicePageContext context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString= extras.getString("filename");
            }
        } else {
            newString= (String) savedInstanceState.getSerializable("filename");
        }

        final String fileName = newString;
        djvuBook = new DjvuBook(fileName);
        final ImageView iv = findViewById(R.id.page_image);
        final AtomicInteger pageNumber = new AtomicInteger(0);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int pageNo = pageNumber.addAndGet(1);
                BookPage page = djvuBook.getPage(pageNo);
                context = new DjvuDevicePageContext();
                Bitmap bitmap = page.getAsBitmap(context);
                iv.setImageBitmap(bitmap);

            }
        });
    }

}
