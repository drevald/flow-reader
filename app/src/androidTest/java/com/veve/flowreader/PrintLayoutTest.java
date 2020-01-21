package com.veve.flowreader;

import android.app.admin.DeviceAdminInfo;
import android.content.Intent;
import android.print.PrintAttributes;

import androidx.test.rule.ActivityTestRule;

import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.uitest.BookTest;
import com.veve.flowreader.views.MainActivity;
import com.veve.flowreader.views.PrintActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.veve.flowreader.Constants.BOOK_CONTEXT;
import static com.veve.flowreader.Constants.BOOK_ID;
import static com.veve.flowreader.Constants.MM_IN_INCH;
import static junit.framework.Assert.assertEquals;

public class PrintLayoutTest extends BookTest {

    private PrintAttributes printAttributes;
    private PrintActivity printActivity;

    @Rule
    public ActivityTestRule<PrintActivity> activityRule =
            new ActivityTestRule<>(
                    PrintActivity.class,
                    true,     // initialTouchMode
                    false);     // launchActivity. False to customize the intent

    @Before
    public void setPrintAttributes() {
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        builder.setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0));
        builder.setResolution(new PrintAttributes.Resolution("300dpi", "300dpi", 300, 300));
        printAttributes = builder.build();
    }

    @Test
    public void testColumnWidth() {
        Intent printIntent = new Intent("com.veve.flowreader.views.PrintActivity");
        printIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        printIntent.putExtra(BOOK_ID, testBookId);
        printIntent.putExtra(BOOK_CONTEXT, new DevicePageContext());
        printActivity = activityRule.launchActivity(printIntent);
        int columnWidth = 66;
        assertEquals(3, printActivity.calculateColsNum(columnWidth, printAttributes));
        assertEquals(70,  printActivity.calculateGapPix(columnWidth, printAttributes));
    }

}
