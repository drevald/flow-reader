package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.util.Log;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.impl.mockraster.MockRasterBook;

import java.util.Arrays;
import java.util.List;

public class MockRenderer implements PageRenderer {

    private Book book;

    public MockRenderer(BookRecord bookRecord) {
        this.book = new MockRasterBook(bookRecord.getTitle());
    }


    @Override
    public List<Bitmap> renderPage(DevicePageContext context, int position) {
        return Arrays.asList(book.getPage(position).getAsBitmap(context));
    }


    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        return book.getPage(position).getAsBitmap(context);
    }

    @Override
    public Bitmap renderOriginalPage(int position) {
        Log.v("NULLBOOK", "Getting MockRenderer the original page " + position);
        return null;
    }

    @Override
    public void setPageLayoutParser(PageLayoutParser parser) {

    }



}
