package com.veve.flowreader.dao;

import java.util.List;

/**
 * Designed to implement persistance
 * Created by ddreval on 4/3/2018.
 */

public interface BookStorage {

    public List<BookRecord> getBooksList();

    public long addBook(BookRecord bookRecord);

    public void deleteBook(long bookId);

    public void updateBook(BookRecord bookRecord);
}
