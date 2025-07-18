package com.veve.flowreader;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreateBookTest {

    private File pdfBookFile;
    private long pdfBookFileId = -1L;
    private BooksCollection booksCollection;
    private Context appContext;

    private static final String BOOK_MD5 = "B5407E667B7BAA38FF6F10C3A8E1B8AD";
    private static final String BOOK_URI = "/storage/emulated/0/Android/data/com.veve.flowreader/files/pdf_sample.pdf";
    private static final long BOOK_SIZE = 30921L;

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

    @Test
    public void createPdfBook() {
        BookRecord bookRecord = BookFactory.getInstance().createBook(pdfBookFile);
        pdfBookFileId = booksCollection.addBook(bookRecord);
        assertEquals(BOOK_SIZE, bookRecord.getSize());
        assertEquals(BOOK_MD5, bookRecord.getMd5());
        assertEquals(BOOK_URI, bookRecord.getUrl());
    }

    @After
    public void cleanup() {
        pdfBookFile.delete();
        booksCollection.deleteBook(pdfBookFileId);
    }


}

//      pdf_sample.pdf

