package com.veve.flowreader.uitest;

import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.support.test.rule.ActivityTestRule;
import android.widget.ImageButton;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.views.PageActivity;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;

public class ModePhoneTest extends BookTest {

    PageActivity pageActivity;
    VectorDrawable iconDrawableToPhone;
    VectorDrawable iconDrawableToBook;

    private static void initTestIfNotTravis() {
        boolean cond = "true".equals(System.getenv("TRAVIS")) ;
        Assume.assumeFalse(cond);
    }

    @BeforeClass
    public static void before() {
        initTestIfNotTravis();
    }

    @Rule
    public ActivityTestRule<PageActivity> pageActivityRule =
            new ActivityTestRule<>(
                    PageActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Before
    public void getIcons() {
        iconDrawableToPhone = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_to_phone);
        iconDrawableToBook = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_to_book);
    }

    @Before
    public void getActivity() {
        bookRecord.setMode(Constants.VIEW_MODE_PHONE);
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
        VectorDrawable iconDrawable;
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook));
        showButton.callOnClick();
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));
    }

}
