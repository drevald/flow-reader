package com.veve.flowreader.uitest;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.views.PageActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;

public class FlippingTest {

    PageActivity pageActivity;
    VectorDrawable iconDrawableToPhone;
    VectorDrawable iconDrawableToBook;
    Context appContext;
    BooksCollection booksCollection;
    BookRecord bookRecord;
    Long bookRecordId;
    File bookFile;

    @Rule
    public ActivityTestRule<PageActivity> pageActivityRule =
            new ActivityTestRule<>(
                    PageActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Before
    public void createBook() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        bookFile = new File(appContext.getExternalFilesDir(null), "cyberiada.pdf");
        bookFile.createNewFile();
        InputStream is = appContext.getResources().openRawResource(R.raw.sample);
        OutputStream os = new FileOutputStream(bookFile);
        byte[] buffer = new byte[100];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
        bookRecord = BookFactory.getInstance().createBook(bookFile);
        bookRecordId = booksCollection.addBook(bookRecord);
    }

    @Before
    public void getActivity() {
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, bookRecordId);
        intent.putExtra(Constants.POSITION, 1);
        pageActivity = pageActivityRule.launchActivity(intent);
    }

    @Test
    public void testFlipping() throws Exception {
        for (int i = 1; i < bookRecord.getPagesCount(); i++) {
            pageActivity.setPageNumber(i);
            while (pageActivity.findViewById(R.id.progress).getVisibility() == VISIBLE) {
                Thread.currentThread().sleep(10);
            }
        }
    }

    @After
    public void clearUp() {
        booksCollection.deleteBook(bookRecordId);
        bookFile.delete();
    }

}
