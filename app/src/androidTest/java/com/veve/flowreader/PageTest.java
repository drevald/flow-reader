package com.veve.flowreader;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;

import com.veve.flowreader.views.MainActivity;
import com.veve.flowreader.views.PageActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class PageTest extends BookTest {

    @Rule
    public ActivityTestRule<PageActivity> activityRule =
            new ActivityTestRule<>(
                    PageActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Test
    public void testActivity() {
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, testBookId);
        intent.putExtra(Constants.POSITION, 1);
        PageActivity pageActivity = activityRule.launchActivity(intent);
        ImageButton showButton = pageActivity.findViewById(R.id.show);
        assertTrue(showButton.getVisibility() == VISIBLE);
        assertTrue(pageActivity.findViewById(R.id.bottomBar).getVisibility() == INVISIBLE);
    }


}
