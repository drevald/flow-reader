package com.veve.flowreader.views;

import android.app.UiModeManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.veve.flowreader.R;

public class BaseActivity  extends AppCompatActivity {

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

}
