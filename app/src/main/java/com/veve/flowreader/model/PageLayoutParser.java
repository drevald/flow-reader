package com.veve.flowreader.model;

import com.veve.flowreader.dao.BookRecord;

import java.util.List;

/**
 * Component designed to recognize page layout (columns, lines, images) and parse it into glyphs
 * of proper type
 */
public interface PageLayoutParser {

    List<PageGlyph> getGlyphs(BookSource bookSource, int position);

    List<PageGlyph> getGlyphs(BookRecord bookRecord, BookSource bookSource, int position);

}
