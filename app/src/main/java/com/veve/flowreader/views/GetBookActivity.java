package com.veve.flowreader.views;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GetBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_book);
        Uri uri = getIntent().getData();
        try {
            BooksCollection booksCollection = BooksCollection.getInstance(getApplicationContext());
            File file = getFile(uri);
            if (booksCollection.hasBook(file)) {
                BookRecord bookRecord = booksCollection.getBook(uri.getEncodedPath());
                Intent ii = new Intent(GetBookActivity.this, PageActivity.class);
                ii.putExtra(Constants.BOOK_ID, bookRecord.getId());
                ii.putExtra(Constants.FILE_NAME, bookRecord.getUrl());
                startActivity(ii);
            } else {
                new BookCreatorTask().execute(file, uri);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "URI is " + uri, e);
            Intent intent = new Intent (GetBookActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        }
    }

    private File getFile(Uri uri) throws IOException {
        File bookFile;
        if (uri.getScheme().equals("file")) {
            bookFile = new File(uri.getPath());
        } else {
            ContentResolver resolver = getApplicationContext().getContentResolver();
            InputStream fis = resolver.openInputStream(uri);
            String extension = uri.getPath().substring(uri.getPath().lastIndexOf("."));
            bookFile = File.createTempFile("book", extension);
            FileOutputStream fileOutputStream = new FileOutputStream(bookFile);
            byte[] buffer = new byte[100];
            while(fis.read(buffer)!=-1) {
                fileOutputStream.write(buffer);
                fileOutputStream.flush();
            }
            fileOutputStream.close();
            fis.close();
            Log.v(getClass().getName(), "bookFile.getAbsolutePath()=" + bookFile.getAbsolutePath());
        }
        return bookFile;
    }

    class BookCreatorTask extends AsyncTask<Object, Void, Void> {

        private BookRecord newBook;

        private long bookId;

        @Override
        protected Void doInBackground(Object... objects){

            File bookFile = (File)objects[0];
            Uri uri = (Uri)objects[1];
            Log.v(getClass().getName(), "Start parsing new book at " + bookFile.getPath());
            newBook = BookFactory.getInstance().createBook(bookFile);
            newBook.setCurrentPage(0);
            newBook.setUrl(uri.getEncodedPath());
            bookId = BooksCollection.getInstance(getApplicationContext()).addBook(newBook);

            if(uri.getScheme() != null && uri.getScheme().equals("content")) {
                bookFile.delete();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent ii = new Intent(GetBookActivity.this, PageActivity.class);
            ii.putExtra(Constants.BOOK_ID, bookId);
            ii.putExtra(Constants.FILE_NAME, newBook.getUrl());
            startActivity(ii);
        }

    }

}

