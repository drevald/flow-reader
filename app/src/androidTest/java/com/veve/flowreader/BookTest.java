package com.veve.flowreader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;


import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BookTest {

    long testBookId;

    @Before
    public void prepareCollection() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        BooksCollection booksCollection = BooksCollection.getInstance(appContext);
        File file = new File(appContext.getFilesDir(), "pdf_sample.pdf");
        file.createNewFile();
        BookRecord oldBookRecord = booksCollection.getBook(file.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[100];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
        BookRecord bookRecord = BookFactory.getInstance().createBook(file);
        testBookId = booksCollection.addBook(bookRecord);
    }

}
