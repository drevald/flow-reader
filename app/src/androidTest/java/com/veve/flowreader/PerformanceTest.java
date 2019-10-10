package com.veve.flowreader;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

    File djvuFile;
    File pdfFile;
    BooksCollection booksCollection;
    Context appContext;
    long bookRecordId;

    @Before
    public void getBookFile() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        InputStream is = appContext.getResources().openRawResource(R.raw.djvu_sample);
        djvuFile = File.createTempFile("djvu", null);
        djvuFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(djvuFile);
        byte[] buffer = new byte[100];
        while (is.read(buffer) != -1) {
            fos.write(buffer);
            fos.flush();
        }
        fos.close();
    }

    @Test
    public void testDjvu() {
        long start = System.currentTimeMillis();
        DjvuBook djvuBook = new DjvuBook(djvuFile.getPath());
        Log.d(getClass().getName(), "Book creation took " + (System.currentTimeMillis() - start) + " millisecods");
        djvuBook.getId();
        DjvuBookSource djvuBookSource = new DjvuBookSource(djvuFile.getPath());
        BookRecord bookRecord = new BookRecord(djvuBook.getId(), 0, djvuBook.getPagesCount(), djvuBook.getName(), djvuBook.getPath());

        bookRecordId = booksCollection.addBook(bookRecord);

        CachedPageRendererImpl cachedPageRenderer = new CachedPageRendererImpl(booksCollection, bookRecord, djvuBookSource);
        DevicePageContext context = new DevicePageContextImpl(800);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderOriginalPage(context, 0);
        Log.d(getClass().getName(), "First opening original took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 1000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderOriginalPage(context, 0);
        Log.d(getClass().getName(), "Second opening original took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 500);


        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        Log.d(getClass().getName(), "First opening page took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 3000);


        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        Log.d(getClass().getName(), "Second opening page took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 1000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 1);
        Log.d(getClass().getName(), "First opening next page took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 3000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        Log.d(getClass().getName(), "Third opening page took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue(System.currentTimeMillis() - start < 1000);


    }

    @After
    public void cleanup() {
       booksCollection.deleteBook(bookRecordId);
    }



}
