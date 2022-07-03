package com.veve.flowreader.views;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

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
        super.onCreate(savedInstanceState);
//        setTheme(darkTheme ? R.style.AppThemeNight : R.style.AppTheme);
    }

    protected void changeTheme() {
//        darkTheme = !darkTheme;
//        recreate();

        UiModeManager uiManager = (UiModeManager) getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);

        if (!darkTheme) {
            //uiManager.enableCarMode(0);
//            uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkTheme = true;
        } else {
            // uiManager.disableCarMode(0);
            //uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkTheme = false;
        }
        recreate();
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
