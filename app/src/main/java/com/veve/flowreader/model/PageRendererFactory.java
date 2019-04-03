package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.MockRenderer;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;


public class PageRendererFactory {

    public static PageRenderer getRenderer(BookRecord bookRecord) {
        if (bookRecord.getUrl().toLowerCase().endsWith("djvu")) {
            BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
            return new PageRendererImpl(bookSource);
        } else  if (bookRecord.getUrl().toLowerCase().endsWith("pdf")) {
            BookSource bookSource = new PdfBookSource(bookRecord.getUrl());
            return new PageRendererImpl(bookSource);
        } else {
            return new MockRenderer(bookRecord);
        }
    }

}
