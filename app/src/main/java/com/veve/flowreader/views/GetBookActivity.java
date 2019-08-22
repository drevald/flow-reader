package com.veve.flowreader.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;

public class GetBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_book);
        File file = new File(getIntent().getData().getPath());
        BooksCollection booksCollection = BooksCollection.getInstance(getApplicationContext());
        if (booksCollection.hasBook(file)) {
            BookRecord bookRecord = booksCollection.getBook(file.getPath());
            Intent ii = new Intent(GetBookActivity.this, PageActivity.class);
            ii.putExtra("bookId", bookRecord.getId());
            ii.putExtra("filename", bookRecord.getUrl());
            startActivity(ii);
        } else {
            new BookCreatorTask().execute(file);
        }
    }


    class BookCreatorTask extends AsyncTask<File, Void, Void> {

        private BookRecord newBook;

        private long bookId;

        @Override
        protected Void doInBackground(File... files) {
            File file = files[0];
            Log.v(getClass().getName(), "Start parsing new book at " + file.getPath());
            newBook = BookFactory.getInstance().createBook(file);
            newBook.setCurrentPage(0);
            newBook.setUrl(file.getAbsolutePath());
            newBook.setName(file.getName());
            bookId = BooksCollection.getInstance(getApplicationContext()).addBook(newBook);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent ii = new Intent(GetBookActivity.this, PageActivity.class);
            ii.putExtra("bookId", bookId);
            ii.putExtra("filename", newBook.getUrl());
            startActivity(ii);
        }
    }


}

