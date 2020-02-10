package com.veve.flowreader.uitest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import android.util.Log;

import com.veve.flowreader.MD5;
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

    String pdfFileName = "pdf_sample.pdf";
    String djvuFileName = "djvu_sample.djvu";
    String sampleDirectory = ".";
    int djvuFileRes = R.raw.djvu_sample;
    int pdfFileRes = R.raw.pdf_sample;

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
        File parentDirectory = new File(appContext.getExternalFilesDir(null), sampleDirectory);
        pdfBookFile = new File(parentDirectory, pdfFileName);
        pdfBookFile.createNewFile();
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(pdfBookFile);
        Utils.copy(is, os);
        BookRecord oldBookRecord = booksCollection.getBookByChecksum(MD5.fileToMD5(pdfBookFile.getPath()));
        if (oldBookRecord != null)
            booksCollection.deleteBook(oldBookRecord.getId());
    }

    @Before
    public void prepareDjvuSample() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        djvuBookFile = new File(appContext.getExternalFilesDir(null), djvuFileName);
        if (djvuBookFile.createNewFile());
        BookRecord oldBookRecord = booksCollection.getBook(djvuBookFile.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.djvu_sample);
        OutputStream os = new FileOutputStream(djvuBookFile);
        byte[] buffer = new byte[100];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
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
//        assertEquals(bookRecord.getTitle(), djvuFileName);
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
//        assertEquals(bookRecord.getTitle(), djvuFileName);
    }

    @Test
    public void testPdfContentOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                        pdfBookFile.getPath().substring("/storage/emulated/0".length())));
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
