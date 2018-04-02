package com.veve.flowreader.model;

import com.veve.flowreader.model.impl.mockraster.MockRasterBook;
import com.veve.flowreader.model.impl.mocksimple.MockBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 12.01.2018.
 */

public class BooksCollection {

    private List<Book> booksList;

    private static BooksCollection bookCollection;

    private BooksCollection() {
        booksList = new ArrayList<Book>();
    }

    public static BooksCollection getInstance() {
        if (bookCollection == null) {
            bookCollection = new BooksCollection();
        }
        //How to read config from memory?
        //So far there will be fake books list
        bookCollection.addBook(new MockRasterBook("Book One"));
        bookCollection.addBook(new MockRasterBook("Book Two"));
        bookCollection.addBook(new MockRasterBook("Book Three"));
        bookCollection.addBook(new MockRasterBook("Book Four"));
        //bookCollection.addBook(new MockRasterBook("Book Five"));
        return bookCollection;
    }

    public List<Book> getBooks() {
        return booksList;
    }

    public void addBook(Book book) {
        booksList.add(book);
    }

}
