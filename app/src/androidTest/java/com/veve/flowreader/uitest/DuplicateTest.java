package com.veve.flowreader.uitest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.veve.flowreader.MD5;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.DaoAccess;
import com.veve.flowreader.model.BooksCollection;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

public class DuplicateTest {

    private File firstBookFile;
    private File secondBookFile;
    private BookRecord firstBookRecord;
    private BookRecord secondBookRecord;
    private BooksCollection booksCollection;
    private Context appContext;

    @Before
    public void prepareSamples() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        AppDatabase appDatabase = AppDatabase.getInstance(appContext);
        DaoAccess daoAccess = appDatabase.daoAccess();
        firstBookFile = makeFile("first_sample.pdf");
        secondBookFile = makeFile("second_sample.pdf");
        String checksum = MD5.fileToMD5(secondBookFile.getPath());
        daoAccess.deleteByChecksum(checksum);
    }

    private File makeFile(String fileName) throws IOException {
        File file = new File(appContext.getExternalFilesDir(null), fileName);
        if (!file.exists()) assertTrue(file.createNewFile());
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[1];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
        return file;
    }

    @Test
    public void testDuplicate() throws Exception {
        Intent firstIntent = new Intent("android.intent.action.VIEW", Uri.fromFile(firstBookFile));
        firstIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        firstIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        firstIntent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(firstIntent);
        Thread.sleep(5000);
        Intent secondIntent = new Intent("android.intent.action.VIEW", Uri.fromFile(secondBookFile));
        secondIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        secondIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        secondIntent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(secondIntent);
        Thread.sleep(5000);

        firstBookRecord = booksCollection.getBook(firstBookFile.getPath());
        secondBookRecord = booksCollection.getBook(secondBookFile.getPath());

        assertTrue((firstBookRecord==null&&secondBookRecord!=null)
                ||(secondBookRecord==null&&firstBookRecord!=null));

    }

    @After
    public void cleanup() {
        if(firstBookFile!=null) assertTrue(firstBookFile.delete());
        if(secondBookFile!=null) assertTrue(secondBookFile.delete());
        if (firstBookRecord!=null) booksCollection.deleteBook(firstBookRecord.getId());
        if (secondBookRecord!=null) booksCollection.deleteBook(secondBookRecord.getId());
    }

}
