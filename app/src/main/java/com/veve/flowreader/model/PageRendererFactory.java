package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.MockPageRendererImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;
import com.veve.flowreader.model.impl.pdf.PdfBookSource;

public class PageRendererFactory {

    public static PageRenderer getRenderer(BookRecord bookRecord) {
        BookSource bookSource = null;
        if(bookRecord.getUrl().endsWith("djvu")) {
            bookSource = new DjvuBookSource(bookRecord.getUrl());
        } else if (bookRecord.getUrl().endsWith("pdf")) {
            bookSource = new PdfBookSource(bookRecord.getUrl());
        }
        return new PageRendererImpl(bookSource);
    }

    public static PageRenderer getMockRenderer() {
        return new MockPageRendererImpl();
    }


}
