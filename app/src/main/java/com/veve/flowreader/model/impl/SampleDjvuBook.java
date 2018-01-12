package com.veve.flowreader.model.impl;

import com.veve.flowreader.model.Book;

/**
 * Created by ddreval on 12.01.2018.
 */

public class SampleDjvuBook implements Book {

    @Override
    public String getName() {
        return "Sample DjVu Book";
    }

    @Override
    public long getId() {
        return 0;
    }
}
