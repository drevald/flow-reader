package com.veve.flowreader.uitest;

import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
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
import static java.lang.Thread.*;
import static java.lang.Thread.currentThread;
import static junit.framework.TestCase.assertEquals;
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
    public void testPrintButton() {
        ImageButton printButton = printActivity.findViewById(R.id.do_print);
        printButton.callOnClick();
   }

    @Test
    public void testSetColumnWidth() throws Exception {
        printActivity.runOnUiThread(() -> {
            printActivity.findViewById(R.id.column_width).performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
            ((EditText)printActivity.findViewById(R.id.column_width)).setText("100");
            printActivity.findViewById(R.id.columns_number).performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
        });

        currentThread().sleep(100);
        printActivity.runOnUiThread(() -> {
            printActivity.findViewById(R.id.columns_number).callOnClick();
        });
        int colsNum = Integer.parseInt(((EditText)printActivity.findViewById(R.id.columns_number)).getText().toString());
        assertEquals(2, colsNum);
    }

    @Test
    public void testSetColumnsNumber() throws Exception {
        printActivity.runOnUiThread(() -> {
            printActivity.findViewById(R.id.columns_number).performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
            ((EditText)printActivity.findViewById(R.id.columns_number)).setText("2");
            printActivity.findViewById(R.id.column_width).performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
        });
        currentThread().sleep(100);
        int colWidth = Integer.parseInt(((EditText)printActivity.findViewById(R.id.column_width)).getText().toString());
        assertEquals(105, colWidth);
    }

}
