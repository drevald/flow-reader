package com.veve.flowreader.uitest;

import android.content.Intent;
import android.graphics.drawable.VectorDrawable;

import androidx.test.rule.ActivityTestRule;

import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.views.PageActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class ModeBookTest extends BookTest {

    PageActivity pageActivity;
    VectorDrawable iconDrawableToPhone;
    VectorDrawable iconDrawableToBook;

    @Rule
    public ActivityTestRule<PageActivity> pageActivityRule =
            new ActivityTestRule<>(
                    PageActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Before
    public void getIcons() {
        iconDrawableToPhone = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_baseline_smartphone_24);
        iconDrawableToBook = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_baseline_menu_book_24);
    }

    @Before
    public void getActivity() {
        bookRecord.setMode(Constants.VIEW_MODE_ORIGINAL);
        booksCollection.updateBook(bookRecord);
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, testBookId);
        intent.putExtra(Constants.POSITION, 1);
        pageActivity = pageActivityRule.launchActivity(intent);
    }

    @Test
    public void testShowButton() {
        ImageButton showButton = pageActivity.findViewById(R.id.show);
        assertTrue(showButton.getVisibility() == VISIBLE);
        VectorDrawable iconDrawable;
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(pageActivity.findViewById(R.id.bottomBar).getVisibility() == INVISIBLE);
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));
        showButton.callOnClick();
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook));
    }

    @Test
    public void testSwitchShowButton() throws Exception {

        ImageButton showButton = pageActivity.findViewById(R.id.show);
        VectorDrawable iconDrawable;
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));

        //make panel visible
        LinearLayout page = pageActivity.findViewById(R.id.page);
        page.callOnClick();

        //changing mode to "reflowed page"
        showButton.callOnClick();
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook));
        assertTrue(pageActivity.getBook().getMode()==Constants.VIEW_MODE_PHONE);

    }


    @Test
    public void testZoom() throws InterruptedException {
        assertEquals(pageActivity.getBook().getZoomOriginal(), 1.0F);
        ImageButton zoomInButton = pageActivity.findViewById(R.id.larger_text);
        zoomInButton.callOnClick();
        assertEquals(pageActivity.getBook().getZoomOriginal(), 1.25F);
        ImageButton zoomOutButton = pageActivity.findViewById(R.id.smaller_text);
        zoomOutButton.callOnClick();
        assertEquals(pageActivity.getBook().getZoomOriginal(), 1.0F);

        for (int i = 0; i < 16; i++) {
            zoomOutButton.callOnClick();
            Thread.currentThread().sleep(1000);
            Log.v(getClass().getName(),
                    "Zooming out with ratio " + pageActivity.getBook().getZoomOriginal());
        }

        for (int i = 0; i < 16; i++) {
            zoomInButton.callOnClick();
            Thread.currentThread().sleep(1000);
            Log.v(getClass().getName(),
                    "Zooming in with ratio " + pageActivity.getBook().getZoomOriginal());
        }


    }

//    @Test
//    public void testPinch() {
//        assertEquals(pageActivity.getBook().getZoom(), 1.0F);
//        LinearLayout page = pageActivity.findViewById(R.id.page);
////        MotionEvent pinchEvent = MotionEvent.obtain(long downTime, long eventTime, int action,
////        float x, float y, int metaState);
//        MotionEvent pinchEvent = MotionEvent.obtain(10 , 10, MotionEvent.ACTION_DOWN,
//        0, 0, );
//        page.onTouchEvent(pinchEvent);
//    }



}
