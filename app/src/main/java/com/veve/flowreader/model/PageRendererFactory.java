package com.veve.flowreader.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.MockRenderer;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class PageRendererFactory {

    public static PageRenderer getRenderer(BooksCollection bookCollection, BookRecord bookRecord, Context context) throws Exception  {
        if (bookRecord.getUrl().toLowerCase().endsWith("djvu")) {
            BookSource bookSource = new DjvuBookSource(getTempPath(bookRecord.getUrl(), context));
            return new CachedPageRendererImpl(bookCollection, bookRecord, bookSource);
        } else  if (bookRecord.getUrl().toLowerCase().endsWith("pdf")) {
            BookSource bookSource = new PdfBookSource(getTempPath(bookRecord.getUrl(), context));
            return new CachedPageRendererImpl(bookCollection, bookRecord, bookSource);
        } else {
            return new MockRenderer(bookRecord);
        }
    }

    private static String getTempPath(String url, Context context) throws Exception {
        if (url.startsWith("file")) {
            return url;
        } else {
            ContentResolver resolver = context.getContentResolver();
            InputStream fis = resolver.openInputStream(Uri.parse(url));
            String extension = url.substring(url.lastIndexOf("."));
            File bookFile = File.createTempFile("book", extension);
            bookFile.deleteOnExit();
            FileOutputStream fileOutputStream = new FileOutputStream(bookFile);
            byte[] buffer = new byte[100];
            while(fis.read(buffer)!=-1) {
                fileOutputStream.write(buffer);
                fileOutputStream.flush();
            }
            fileOutputStream.close();
            fis.close();
            return bookFile.getPath();
        }
    }


}
