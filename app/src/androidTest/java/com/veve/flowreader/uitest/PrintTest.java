package com.veve.flowreader.uitest;

import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.widget.ImageButton;

import androidx.test.rule.ActivityTestRule;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.views.PrintActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;

public class PrintTest extends BookTest {

    PrintActivity printActivity;
    VectorDrawable iconDrawableToPhone;
    VectorDrawable iconDrawableToBook;

    @Rule
    public ActivityTestRule<PrintActivity> printActivityRule =
            new ActivityTestRule<>(
                    PrintActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent


    @Before
    public void getActivity() {
        bookRecord.setMode(Constants.VIEW_MODE_ORIGINAL);
        booksCollection.updateBook(bookRecord);
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, testBookId);
        intent.putExtra(Constants.POSITION, 1);
        printActivity = printActivityRule.launchActivity(intent);
    }

    @Test
    public void testShowButton() {
        ImageButton printButton = printActivity.findViewById(R.id.print);
        printButton.callOnClick();
    }

}
