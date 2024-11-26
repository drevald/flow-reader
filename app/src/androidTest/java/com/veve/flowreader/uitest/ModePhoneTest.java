package com.veve.flowreader.uitest;

import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import androidx.test.rule.ActivityTestRule;
import android.widget.ImageButton;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.views.PageActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static junit.framework.TestCase.assertTrue;

public class ModePhoneTest extends BookTest {

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
