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
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
//import java.util.function.BiConsumer;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

    File djvuFile;
    File pdfFile;
    BooksCollection booksCollection;
    Context appContext;
    long bookRecordId;

    private static void initTestIfNotTravis() {
        boolean cond = "true".equals(System.getenv("TRAVIS")) ;
        Assume.assumeFalse(cond);
    }

    @BeforeClass
    public static void before() {
        initTestIfNotTravis();
    }

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

    @Test()
    public void testDjvu() throws Exception {

        System.out.println("System.getProperty(\"TRAVIS\")=" + System.getProperty("TRAVIS"));
        System.out.println("System.getenv(\"TRAVIS\")=" + System.getenv("TRAVIS"));
        System.out.println("\"true\".equals(System.getProperty(\"TRAVIS\"))=" + ("true".equals(System.getProperty("TRAVIS"))));
        System.out.println("\"true\".equals(System.getenv(\"TRAVIS\"))=" + ("true".equals(System.getenv("TRAVIS"))));

        if("true".equals(System.getProperty("TRAVIS"))) {
            throw new Exception();
        }

        if("true".equals(System.getenv("TRAVIS"))) {
            throw new Exception();
        }

        long start, end;
        start = System.currentTimeMillis();

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
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "First time opening original page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("First time opening original page" + " took " + (end - start ) + " milliseconds which is more than " + 10000, end - start < 10000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderOriginalPage(context, 0);
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "Second time opening original page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("Second time opening original page" + " took " + (end - start ) + " milliseconds which is more than " + 2500, end - start < 2500);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "First time opening page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("First time opening page" + " took " + (end - start ) + " milliseconds which is more than " + 15000, end - start < 15000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "Second time opening page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("Second time opening page" + " took " + (end - start ) + " milliseconds which is more than " + 5000, end - start < 5000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 1);
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "First time opening next page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("First time opening next page" + " took " + (end - start ) + " milliseconds which is more than " + 15000, end - start < 15000);

        start = System.currentTimeMillis();
        cachedPageRenderer.renderPage(context, 0);
        end = System.currentTimeMillis();
        Log.d(getClass().getName(), "Third time opening page" + " took " + (System.currentTimeMillis() - start) + " millisecods");
        assertTrue("Third time opening page" + " took " + (end - start ) + " milliseconds which is more than " + 5000, end - start < 5000);

    }

    @After
    public void cleanup() {

        booksCollection.deleteBook(bookRecordId);

    }

//    private void checkTiming(BiConsumer<DevicePageContext, Integer> f, DevicePageContext context, int position, int limit, String message) {
//        long start = System.currentTimeMillis();
//        f.accept(context, position);
//        long end = System.currentTimeMillis();
//        Log.d(getClass().getName(), message + " took " + (System.currentTimeMillis() - start) + " millisecods");
//        assertTrue(message + " took " + (end - start ) + " milliseconds which is more than " + limit, end - start < limit);
//    }

}
