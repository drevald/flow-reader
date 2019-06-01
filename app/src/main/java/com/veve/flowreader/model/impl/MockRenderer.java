package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.mockraster.MockRasterBook;

public class MockRenderer implements PageRenderer {

    private BookRecord bookRecord;
    private Book book;

    public MockRenderer(BookRecord bookRecord) {
        this.bookRecord = bookRecord;
        this.book = new MockRasterBook(bookRecord.getName());
    }

    @Override
    public Bitmap renderPage(DevicePageContext context, int position) {
        return book.getPage(position).getAsBitmap(context);
    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        return book.getPage(position).getAsBitmap(context);
    }

    @Override
    public void setPageLayoutParser(PageLayoutParser parser) {

    }

}