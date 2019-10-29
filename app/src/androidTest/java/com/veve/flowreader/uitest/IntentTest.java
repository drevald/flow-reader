package com.veve.flowreader.uitest;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class IntentTest {

    File bookFile;
    BooksCollection booksCollection;
    Context appContext;

    @Before
    public void prepareCollection() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        bookFile = new File(appContext.getFilesDir(), "pdf_sample.pdf");
        bookFile.createNewFile();
        BookRecord oldBookRecord = booksCollection.getBook(bookFile.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(bookFile);
        byte[] buffer = new byte[100];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
    }

    @Test
    public void testIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.fromFile(bookFile));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        BookRecord bookRecord = booksCollection.getBook(bookFile.getPath());
        assertNotNull(bookRecord);
    }

}
