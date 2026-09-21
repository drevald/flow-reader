package com.veve.flowreader.views;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.veve.flowreader.R;

public class BaseActivity  extends AppCompatActivity {

    private static final ColorMatrix COLOR_MATRIX_INVERTED =
            new ColorMatrix(new float[] {
                -0.4f, 0,  0,  0,  255,
                0,  -0.4f, 0,  0,  255,
                0,  0,  -0.4f, 0,  255,
                0,  0,  0,  1,  0});

    private static final ColorFilter COLOR_FILTER_SEPIA = new ColorMatrixColorFilter(
            COLOR_MATRIX_INVERTED);

    static boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(darkTheme
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    public void changeTheme(View v) {
        changeTheme();
    }

    protected void changeTheme() {
        darkTheme = !darkTheme;
        getDelegate().setLocalNightMode(darkTheme
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }

    protected boolean isNightMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected Bitmap createInvertedBitmap(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(COLOR_FILTER_SEPIA);
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }


}
