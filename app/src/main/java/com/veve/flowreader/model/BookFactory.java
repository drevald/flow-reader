package com.veve.flowreader.model;

import android.graphics.Bitmap;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.djvu.DjvuBook;
import com.veve.flowreader.model.impl.pdf.PdfBook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import static com.veve.flowreader.MD5.fileToMD5;

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
            bookRecord.setFormat(BookRecord.DJVU_FORMAT);
        } else if (file.getName().toLowerCase().endsWith("pdf")) {
            book = new PdfBook(file.getPath());
            bookRecord.setFormat(BookRecord.PDF_FORMAT);
        } else {
            String header = getHeader(file);
            if (header.startsWith("%PDF")) {
                book = new PdfBook(file.getPath());
                bookRecord.setFormat(BookRecord.PDF_FORMAT);
            } else if (header.startsWith("AT&T")) {
                book = new DjvuBook(file.getPath());
                bookRecord.setFormat(BookRecord.DJVU_FORMAT);
            }
        }

        //Filling native book data
        bookRecord.setPagesCount(book.getPagesCount());
        bookRecord.setTitle(book.getName());

        //Generating and setting preview
        Bitmap bitmap = book.getPage(0).getAsBitmap(new DevicePageContext(100));
        Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 100, 150, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);
        bookRecord.setPreview(byteArrayOutputStream.toByteArray());

        String bookName = "";
        if (book.getTitle() != null) {
            bookName = book.getTitle();
            bookRecord.setOriginalTitle(book.getTitle());
            if (book.getAuthor() != null) {
                bookName += " ";
                bookName += book.getAuthor();
                bookRecord.setAuthor(book.getAuthor());
            }
        } else {
            bookName = file.getName().replaceAll("_", " ");
            bookName = bookName.substring(0, bookName.lastIndexOf("."));
        }

        bookRecord.setMd5(fileToMD5(file.getPath()));
        bookRecord.setSize(file.length());
        bookRecord.setTitle(bookName);
        bookRecord.setUrl(file.getPath());
        return bookRecord;

    }

    private String getHeader(File file) {
        FileInputStream fis = null;
        String result = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[16];
            fis.read(buffer);
            result = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }


}
