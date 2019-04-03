package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.MockRenderer;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;


public class PageRendererFactory {

    public static PageRenderer getRenderer(BookRecord bookRecord) {
        BookSource bookSource = new DjvuBookSource(bookRecord.getUrl());
        return new PageRendererImpl(bookSource);
        //return new MockRenderer(bookRecord);
        //return null;
    }

}
