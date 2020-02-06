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
            click(R.id.set_column_width);
            setText(R.id.column_width, "100");
            click(R.id.set_columns_number);
        });
        currentThread().sleep(100);
        printActivity.runOnUiThread(() -> {
            click(R.id.set_columns_number);
        });
        int colsNum = getInt(R.id.columns_number);
        assertEquals(String.format("Expected %d result %d", 2, colsNum), 2, colsNum);
    }

    @Test
    public void testSetColumnsNumber() throws Exception {
        printActivity.runOnUiThread(() -> {
            click(R.id.set_columns_number);
            setText(R.id.columns_number, "2");
            setText(R.id.gap, "0");
            click(R.id.set_column_width);
        });
        currentThread().sleep(100);
        int colWidth = getInt(R.id.column_width);
        assertEquals(String.format("Expected %d result %d", 105, colWidth), 105, colWidth);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int getInt(int id) {
        int result;
        EditText v = (EditText)printActivity.findViewById(id);
        try {
            result = Integer.parseInt(v.getText().toString());
            v.setBackgroundColor(printActivity.getResources().getColor(R.color.colorPrimary));
        } catch (Exception e) {
            v.setBackgroundColor(printActivity.getResources().getColor(R.color.colorAccent));
            result = -1;
        }
        return result;
    }

    private void click(int id) {
        printActivity.findViewById(id).callOnClick();
    }

    private void setText(int id, String s) {
        ((EditText)printActivity.findViewById(id)).setText(s);
    }

    private void focus(int id) {
        printActivity.findViewById(id).performAccessibilityAction(AccessibilityNodeInfo.ACTION_FOCUS, null);
    }


}
