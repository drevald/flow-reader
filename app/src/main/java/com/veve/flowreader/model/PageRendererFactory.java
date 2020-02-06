package com.veve.flowreader.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.CachedPageRendererImpl;
import com.veve.flowreader.model.impl.MockRenderer;
import com.veve.flowreader.model.impl.NativePageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class PageRendererFactory {

    public static PageRenderer getRenderer(BooksCollection bookCollection, BookRecord bookRecord) throws Exception  {
        if (bookRecord.getFormat()==BookRecord.DJVU_FORMAT) {
            BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
            return new NativePageRendererImpl(bookCollection, bookRecord, bookSource);
        } else  if (bookRecord.getFormat()==BookRecord.PDF_FORMAT) {
            BookSource bookSource = new PdfBookSource(bookRecord.getUrl());
            return new NativePageRendererImpl(bookCollection, bookRecord, bookSource);
        } else {
            throw new Exception("Format Undefined");
        }
    }

}
