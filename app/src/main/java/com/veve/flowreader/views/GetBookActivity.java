package com.veve.flowreader.views;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static com.veve.flowreader.BookContentResolver.contentToFile;

import com.veve.flowreader.Constants;
import com.veve.flowreader.MD5;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;

public class GetBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_book);
        Uri uri = getIntent().getData();
        try {
            BooksCollection booksCollection = BooksCollection.getInstance(getApplicationContext());
            File file = new File(contentToFile(getApplicationContext(), uri));
            String checksum = MD5.fileToMD5(file.getPath());
            BookRecord bookRecord = booksCollection.getBookByChecksum(checksum);
            Log.d(getClass().getName(), "Getting doc " + uri.toString());
            if (bookRecord!=null) {
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

    class BookCreatorTask extends AsyncTask<File, Void, Void> {

        private BookRecord newBook;

        private long bookId;

        @Override
        protected Void doInBackground(File... files){
            File bookFile = files[0];
            Log.v(getClass().getName(), "Start parsing new book at " + bookFile.getPath());
            newBook = BookFactory.getInstance().createBook(bookFile);
            bookId = BooksCollection.getInstance(getApplicationContext()).addBook(newBook);
            Log.v("BOOK", "Inserted with URL " + newBook.getUrl());
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

