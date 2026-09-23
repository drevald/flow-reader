package com.veve.flowreader.uitest;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.Utils;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.views.PageActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.VISIBLE;

public class FlippingTest {

    Context appContext;
    BooksCollection booksCollection;
    BookRecord bookRecord;
    Long bookRecordId;
    File bookFile;
    ActivityScenario<PageActivity> scenario;

    @Before
    public void setup() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Delete any stale database from previous runs so Room creates a fresh schema.
        appContext.deleteDatabase("db");
        // Create the book record BEFORE initialising BooksCollection so that our pdfium
        // call does not race with the InitDatabaseTask that starts in the background
        // as soon as BooksCollection.getInstance() opens the database.
        bookFile = new File(appContext.getExternalFilesDir(null), "cyberiada.pdf");
        bookFile.createNewFile();
        InputStream is = appContext.getResources().openRawResource(R.raw.sample);
        OutputStream os = new FileOutputStream(bookFile);
        Utils.copy(is, os);
        bookRecord = BookFactory.getInstance().createBook(bookFile);
        booksCollection = BooksCollection.getInstance(appContext);
        bookRecordId = booksCollection.addBook(bookRecord);
        Intent intent = new Intent(appContext, PageActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, bookRecordId);
        intent.putExtra(Constants.POSITION, 1);
        scenario = ActivityScenario.launch(intent);
    }

    @Test
    public void testFlipping() throws Exception {
        waitForPageLoad();
        for (int i = 1; i < bookRecord.getPagesCount(); i++) {
            final int page = i;
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    scenario.onActivity(a -> a.setPageNumber(page)));
            waitForPageLoad();
        }
    }

    /** Polls from the test thread (never blocks the main thread). */
    private void waitForPageLoad() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            final boolean[] loading = {false};
            scenario.onActivity(a ->
                    loading[0] = a.findViewById(R.id.progress).getVisibility() == VISIBLE);
            if (!loading[0]) return;
            Thread.sleep(50);
        }
    }

    @After
    public void clearUp() {
        if (scenario != null) scenario.close();
        booksCollection.deleteBook(bookRecordId);
        bookFile.delete();
    }

}
