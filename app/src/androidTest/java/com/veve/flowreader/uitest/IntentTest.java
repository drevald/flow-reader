package com.veve.flowreader.uitest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import android.util.Log;

import com.veve.flowreader.R;
import com.veve.flowreader.Utils;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;

public class IntentTest {

    private File pdfBookFile;
    private File djvuBookFile;
    private long pdfBookFileId = -1L;
    private long djvuBookFileId = -1L;
    private BooksCollection booksCollection;
    private Context appContext;

    @Before
    public void preparePdfSample() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        pdfBookFile = new File(appContext.getExternalFilesDir(null), "pdf_sample.pdf");
        pdfBookFile.createNewFile();
        BookRecord oldBookRecord = booksCollection.getBook(pdfBookFile.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
            Log.v("BOOK", "Deleting " + oldBookRecord.getUrl() + " with id " + oldBookRecord.getId());
        } else {
            Log.v("BOOK", "Book with id " + pdfBookFile.getPath() + " is missing");
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(pdfBookFile);
        Utils.copy(is, os);
    }

    @Before
    public void prepareDjvuSample() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        djvuBookFile = new File(appContext.getExternalFilesDir(null), "djvu_sample.djvu");
        if (djvuBookFile.createNewFile());
        BookRecord oldBookRecord = booksCollection.getBook(djvuBookFile.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.djvu_sample);
        OutputStream os = new FileOutputStream(djvuBookFile);
        Utils.copy(is, os);
    }

    @Test
    public void testPdfFileOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.fromFile(pdfBookFile));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        Thread.sleep(5000);
        BookRecord bookRecord = booksCollection.getBook(pdfBookFile.getPath());
        pdfBookFileId = bookRecord.getId();
        assertNotNull(bookRecord);
    }

    @Test
    public void testDjvuFileOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.fromFile(djvuBookFile));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        Thread.sleep(5000);
        BookRecord bookRecord = booksCollection.getBook(djvuBookFile.getPath());
        djvuBookFileId = bookRecord.getId();
        assertNotNull(bookRecord);
    }

    @Test
    public void testDjvuContentOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                        "/Android/data/com.veve.flowreader/files/djvu_sample.djvu"));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        Thread.sleep(5000);
        BookRecord bookRecord = booksCollection.getBook(djvuBookFile.getPath());
        djvuBookFileId = bookRecord.getId();
        assertNotNull(bookRecord);
    }

    @Test
    public void testPdfContentOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                        "/Android/data/com.veve.flowreader/files/pdf_sample.pdf"));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        Thread.sleep(5000);
        BookRecord bookRecord = booksCollection.getBook(pdfBookFile.getPath());
        Log.v("BOOK", "Retrieved with URL " + pdfBookFile.getPath() + " as " + bookRecord==null?"null":bookRecord.toString());
        djvuBookFileId = bookRecord.getId();
        assertNotNull(bookRecord);
    }

    @After
    public void cleanup() {
        djvuBookFile.delete();
        pdfBookFile.delete();
        booksCollection.deleteBook(pdfBookFileId);
        booksCollection.deleteBook(djvuBookFileId);
    }

}
