package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.impl.MockRenderer;


public class PageRendererFactory {

    public static PageRenderer getRenderer(BookRecord bookRecord) {
        return new MockRenderer(bookRecord);
        //return null;
    }

}
