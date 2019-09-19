package com.veve.flowreader.model.impl;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;

import java.util.List;

public class OpenCVPageLayoutParser implements PageLayoutParser {

    private static PageLayoutParser parser;

    @Override
    public List<PageGlyph> getGlyphs(BookRecord bookRecord, BookSource bookSource, int position) {
        List<PageGlyph> glyphs;

        //Bitmap b = Bitmap.createBitmap(bookSource.getPageBytes(position), g.x, g.y, g.bitmap.getWidth(), g.bitmap.getHeight());
        return null;
    }

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
