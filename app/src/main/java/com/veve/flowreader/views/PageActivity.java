package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.impl.DevicePageContextImpl;

public class PageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton home = (FloatingActionButton) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.page);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i("Tag?" + Thread.currentThread(), "Surface created");
                Canvas canvas = holder.lockCanvas();
                DevicePageContext context = new DevicePageContextImpl();
                context.setCanvas(canvas);
                context.setStartPoint(new Point(0, 0));
                context.setZoom(1);
                if(canvas==null) {
                    Log.i("SurfaceHolder.Callback" + Thread.currentThread(), "canvas is null");
                } else {
                    Log.i("SurfaceHolder.Callback" + Thread.currentThread(), "canvas width is " + canvas.getWidth() + " canvas height is " + canvas.getHeight());

                    Book book = BooksCollection.getInstance().getBooks().get(0);
                    BookPage bookPage = book.getPage(book.getCurrentPageNumber());
                    PageGlyph pageGlyph;

                    while((pageGlyph = bookPage.getNextGlyph()) != null) {
                        pageGlyph.draw(context);
                    }

                    int counter = 0;
//                    while(counter++ < 10) {
//                        bookPage.getNextGlyph().draw(context);
//                    }


                    holder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i("SurfaceHolder.Callback" + Thread.currentThread(), "Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i("SurfaceHolder.Callback" + Thread.currentThread(), "Surface destroyed");
            }
        });

    }

}
