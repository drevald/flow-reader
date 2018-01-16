package com.veve.flowreader.views;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
                Log.i("Tag?", "Surface created");
                Canvas canvas = holder.lockCanvas();
                if(canvas==null)
                    Log.i("SurfaceHolder.Callback","canvas is null");
                Log.i("SurfaceHolder.Callback","canvas width is " + canvas.getWidth()+ " canvas height is " + canvas.getHeight());
                Paint paint = new Paint();
                paint.setColor(Constants.GREEN);
                canvas.drawColor(Color.CYAN);
                canvas.drawRect(300, 200, 200, 400, paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i("Tag?", "Surface changed");
                Canvas canvas = holder.lockCanvas();
                if(canvas==null) {
                    Log.i("SurfaceHolder.Callback", "canvas is null");
                } else {
                    Log.i("SurfaceHolder.Callback", "canvas width is " + canvas.getWidth() + " canvas height is " + canvas.getHeight());
//                    Paint paint = new Paint();
//                    paint.setColor(Color.WHITE);
//                    canvas.drawColor(Color.RED);
//                    canvas.drawRect(300, 200, 200, 400, paint);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i("Tag?", "Surface destroyed");
            }
        });

    }

}
