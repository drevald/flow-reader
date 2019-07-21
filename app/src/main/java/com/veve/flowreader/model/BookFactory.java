package com.veve.flowreader.model;

import android.graphics.Bitmap;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.pdf.PdfBook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Designed as a factory of Book objects of all types
 * Created by ddreval on 4/3/2018.
 */

public class BookFactory {

    private static final BookFactory ourInstance = new BookFactory();

    public static BookFactory getInstance() {
        return ourInstance;
    }

    private BookFactory() {

    }

    public BookRecord createBook(File file) {
        BookRecord bookRecord = new BookRecord();
        Book book = null;
        if (file.getName().toLowerCase().endsWith("djvu")) {
            book = new DjvuBook(file.getPath());
        } else if (file.getName().toLowerCase().endsWith("pdf")) {
            book = new PdfBook(file.getPath());
        }

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
        }
        bookRecord.setPagesCount(book.getPagesCount());
        bookRecord.setName(book.getName());

        Bitmap bitmap = book.getPage(0).getAsBitmap(new DevicePageContext(100));
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 100, 150, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);
        bookRecord.setPreview(byteArrayOutputStream.toByteArray());

        return bookRecord;
    }

}
