package com.veve.flowreader.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.BookStorage;
import com.veve.flowreader.dao.DaoAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddreval on 12.01.2018.
 */

public class BooksCollection {

    private static BooksCollection bookCollection;

    private DaoAccess daoAccess;

    private BooksCollection(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        daoAccess = appDatabase.daoAccess();
    }

    public static BooksCollection getInstance(Context context) {
        if (bookCollection == null) {
            bookCollection = new BooksCollection(context);
        }
        return bookCollection;
    }

    public List<BookRecord> getBooks() {
        BooksGetterTask booksGetterTask = new BooksGetterTask(daoAccess);
        booksGetterTask.execute();
        try {
            return booksGetterTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return null;
        }
    }

    public void deleteBook (long bookId) {
        new BookDeleteTask(daoAccess).execute(bookId);
    }

    public boolean hasBook (File file) {
        BookCheckerTask bookCheckerTask = new BookCheckerTask(daoAccess);
        bookCheckerTask.execute(file.getAbsolutePath());
        try {
            return bookCheckerTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return false;
        }
    }

    public long addBook (BookRecord bookRecord) {
        BookAddTask bookAddTask = new BookAddTask(daoAccess);
        bookAddTask.execute(bookRecord);
        try {
            return bookAddTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return -1;
        }
    }


}

///////////////////////   DB TASKS   ////////////////////////////////////////////

class BookDeleteTask extends AsyncTask<Long, Void, Void> {

    private DaoAccess daoAccess;

    BookDeleteTask(DaoAccess daoAccess) {
        this.daoAccess = daoAccess;
    }

    @Override
    protected Void doInBackground(Long... longs) {
        daoAccess.deleteBook(longs[0]);
        return null;
    }

}

class BookAddTask extends AsyncTask<BookRecord, Void, Long> {

    private DaoAccess daoAccess;

    BookAddTask(DaoAccess daoAccess) {
        this.daoAccess = daoAccess;
    }

    @Override
    protected Long doInBackground(BookRecord... bookRecords) {
        return daoAccess.addBook(bookRecords[0]);
    }
}

class BooksGetterTask extends AsyncTask<Void, Void, List<BookRecord>> {

    private DaoAccess daoAccess;

    BooksGetterTask(DaoAccess daoAccess) {
        this.daoAccess = daoAccess;
    }

    @Override
    protected List<BookRecord> doInBackground(Void... voids) {
        return daoAccess.listBooks();
    }

}

    class BookCheckerTask extends AsyncTask<String, Void, Boolean> {

        private DaoAccess daoAccess;

        BookCheckerTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            List<BookRecord> bookRecords = daoAccess.fetchBook(strings[0]);
            return bookRecords != null && bookRecords.size() > 0;
        }

    }