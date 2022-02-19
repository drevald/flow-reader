package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.NativePageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

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
