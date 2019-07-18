package com.veve.flowreader.model.impl;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;

import java.util.List;

public class OpenCVPageLayoutParser implements PageLayoutParser {

    private static PageLayoutParser parser;

    @Override
    public List<PageGlyph> getGlyphs(BookSource bookSource, int position) {
        return bookSource.getPageGlyphs(position);
    }

    public static PageLayoutParser getInstance() {
        if (parser == null) {
            parser = new OpenCVPageLayoutParser();
        }
        return parser;
    }
}
