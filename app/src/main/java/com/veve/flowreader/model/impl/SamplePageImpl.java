package com.veve.flowreader.model.impl;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.PageGlyph;

/**
 * Created by ddreval on 15.01.2018.
 */

public class SamplePageImpl implements BookPage {

    private int counter;
    private static final int MAX_GLYPHS = 1000;

    public SamplePageImpl() {
        counter = 0;
    }

    @Override
    public PageGlyph getNextGlyph() {
        if (counter ++ < MAX_GLYPHS)
            return new FakePageGlyphImpl();
        return null;
    }

}
