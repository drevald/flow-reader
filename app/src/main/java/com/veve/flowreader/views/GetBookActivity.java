package com.veve.flowreader.views;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class GetBookActivity extends AppCompatActivity {

    private static final String DOWNLOAD_CONTENT_PREFIX =
            "content://com.android.providers.downloads.ui.fileprovider/external_files";

    private static final String DOWNLOAD_FILE_PREFIX = "/storage/emulated/0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_book);
        Uri uri = getIntent().getData();
        try {
            File file = new File(getFilePath(uri));
            BooksCollection booksCollection = BooksCollection.getInstance(getApplicationContext());
            if (booksCollection.hasBook(file)) {
                BookRecord bookRecord = booksCollection.getBook(file.getPath());
                Intent ii = new Intent(GetBookActivity.this, PageActivity.class);
                ii.putExtra(Constants.BOOK_ID, bookRecord.getId());
                ii.putExtra(Constants.FILE_NAME, bookRecord.getUrl());
                startActivity(ii);
            } else {
                new BookCreatorTask().execute(file);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "URI is " + uri, e);
            Intent intent = new Intent (GetBookActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        }
    }

    private String getFilePath(Uri uri) throws Exception {
        Log.v(getClass().getName(), "URI=" + uri + " scheme=" + uri.getScheme());
        if (uri.getScheme().equals("file")) {
            return uri.getPath();
        } else {
            ContentResolver resolver = getApplicationContext().getContentResolver();
            InputStream fis = resolver.openInputStream(uri);
            String extension = uri.getPath().substring(uri.getPath().lastIndexOf("."));
            File bookFile = File.createTempFile("book", extension);
            FileOutputStream fileOutputStream = new FileOutputStream(bookFile);
            byte[] buffer = new byte[100];
            while(fis.read(buffer)!=-1) {
                fileOutputStream.write(buffer);
                fileOutputStream.flush();
            }
            fileOutputStream.close();
            fis.close();
            Log.v(getClass().getName(), "bookFile.getAbsolutePath()=" + bookFile.getAbsolutePath());
            return bookFile.getAbsolutePath();
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
            ii.putExtra(Constants.BOOK_ID, bookId);
            ii.putExtra(Constants.FILE_NAME, newBook.getUrl());
            startActivity(ii);
        }

    }


}

