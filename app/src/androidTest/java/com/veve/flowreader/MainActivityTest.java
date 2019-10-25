package com.veve.flowreader;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.veve.flowreader.views.MainActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class MainActivityTest extends BookTest {

    MainActivity mainActivity;

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(
                    MainActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Test
    public void testActivity() {
        Intent intent = new Intent("com.veve.flowreader.views.MainActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        mainActivity = activityRule.launchActivity(intent);
        ImageButton listButton = mainActivity.findViewById(R.id.books_list);
        ImageButton gridButton = mainActivity.findViewById(R.id.books_grid);
//        gridButton.callOnClick();
//        listButton.callOnClick();
        GridView grid = mainActivity.findViewById(R.id.grid);
        assertNotNull(grid);
        assertTrue(grid.getVisibility() == VISIBLE);
        View bookView = grid.getChildAt(0);
        bookView.callOnClick();
    }

}
