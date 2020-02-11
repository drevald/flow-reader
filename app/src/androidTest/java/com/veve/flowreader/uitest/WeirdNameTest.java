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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeirdNameTest {

    String djvuFileName = "S P\tACE(1).djvu";
    String sampleDirName = "/1/2/3/4/5/6/7/8/9/10/";
    int djvuFileRes = R.raw.noname;

    private File djvuBookFile;
    private long djvuBookFileId = -1L;
    private BooksCollection booksCollection;
    private Context appContext;

    @Before
    public void prepareDjvuSample() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        File parentDirectory = new File(appContext.getExternalFilesDir(null), sampleDirName);
        parentDirectory.mkdirs();
        djvuBookFile = new File(parentDirectory, djvuFileName);
        if (djvuBookFile.createNewFile());
        InputStream is = appContext.getResources().openRawResource(djvuFileRes);
        OutputStream os = new FileOutputStream(djvuBookFile);
        Utils.copy(is, os);
        BookRecord oldBookRecord = booksCollection
                .getBookByChecksum(MD5.fileToMD5(djvuBookFile.getPath()));
        if (oldBookRecord != null)
            booksCollection.deleteBook(oldBookRecord.getId());
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
        assertEquals(bookRecord.getTitle(), djvuFileName.substring(0, djvuFileName.lastIndexOf(".")));
//        assertEquals(bookRecord.getTitle(), djvuFileName);
    }

    @Test
    public void testDjvuContentOpen() throws Exception {
        Intent intent = new Intent("android.intent.action.VIEW",
                Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files" +
                        djvuBookFile.getPath().substring("/storage/emulated/0".length())));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
        appContext.startActivity(intent);
        Thread.sleep(5000);
        BookRecord bookRecord = booksCollection.getBook(djvuBookFile.getPath());
        djvuBookFileId = bookRecord.getId();
        assertNotNull(bookRecord);
        assertEquals(bookRecord.getTitle(), djvuFileName.substring(0, djvuFileName.indexOf(".")));
//        assertEquals(bookRecord.getTitle(), djvuFileName);
    }

    @After
    public void cleanup() {
        djvuBookFile.delete();
        booksCollection.deleteBook(djvuBookFileId);
    }

}

