package com.veve.flowreader.model;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.veve.flowreader.MD5;
import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.DaoAccess;
import com.veve.flowreader.dao.PageGlyphRecord;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public BookRecord getBook(long id) {
        return getBook(id, true);
    }

    public BookRecord getBook(long id, boolean async) {
        if (!async) {
            return daoAccess.getBook(id);
        }
        BookGetterTask bookGetterTask = new BookGetterTask(daoAccess);
        bookGetterTask.execute(id);
        try {
            return bookGetterTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return null;
        }
    }

    public BookRecord getBook(String url) {
        Log.v("BOOK", "Getting by URL " + url);
        BookByUrlGetterTask bookGetterTask = new BookByUrlGetterTask(daoAccess);
        bookGetterTask.execute(url);
        try {
            return bookGetterTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return null;
        }
    }

    public void deleteBook (long bookId) {
        new BookDeleteTask(daoAccess).execute(bookId);
    }

//    public boolean hasBook (Uri uri) {
//        BookCheckerTask bookCheckerTask = new BookCheckerTask(daoAccess);
//        bookCheckerTask.execute(uri.toString());
//        try {
//            return bookCheckerTask.get();
//        } catch (Exception e) {
//            Log.e(getClass().getTitle(), e.getLocalizedMessage());
//            return false;
//        }
//    }

    public BookRecord getBookByChecksum (String checksum) {
        BookGetChecksumTask bookGetChecksumTask = new BookGetChecksumTask(daoAccess);
        bookGetChecksumTask.execute(checksum);
        try {
            return bookGetChecksumTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return null;
        }
    }


    public boolean hasBook (File file) {
        BookCheckerTask bookCheckerTask = new BookCheckerTask(daoAccess);
        bookCheckerTask.execute(file);
        try {
            return bookCheckerTask.get();
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            return false;
        }
    }

    public long addBook (BookRecord bookRecord) {
        return daoAccess.addBook(bookRecord);
    }

    public void updateBook(BookRecord bookRecord) {
        BookUpdateTask bookUpdateTask = new BookUpdateTask(daoAccess);
        bookUpdateTask.execute(bookRecord);
    }

    public List<PageGlyphRecord> getPageGlyphs(Long id, int position, boolean async) {
        if (!async) {
            return daoAccess.getPageGlyphs(id, position);
        }
        GetPageGlyphsTask getPageGlyphsTask = new GetPageGlyphsTask(daoAccess);
        Log.v(getClass().getName(), "1");
        getPageGlyphsTask.execute(id, position);
        try {
            Log.v(getClass().getName(), "2");
            return getPageGlyphsTask.get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            Log.e(getClass().getName(),
                    "Failed to get glyphs for book #" + id + " page " + position, e);
            return null;
        }
    }

    public void addGlyphs(List<PageGlyphRecord> glyphsToStore, boolean async) {
        if (!async) {
            daoAccess.insertGlyphs(glyphsToStore);
            return;
        }
        try {
            AddPageGlyphsTask addPageGlyphsTask = new AddPageGlyphsTask(daoAccess);
            addPageGlyphsTask.execute(glyphsToStore);
        } catch (Exception e) {
            Log.e(getClass().getName(),"Failed to insert glyphs", e);
        }
    }

    public void deleteGlyphs(long bookId, long position) {
        daoAccess.deleteBookPageGlyphs(bookId, position);

    }
}

///////////////////////   DB TASKS   ////////////////////////////////////////////

    class AddPageGlyphsTask extends AsyncTask<List<PageGlyphRecord>, Void, Void> {

        private DaoAccess daoAccess;

        AddPageGlyphsTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected final Void doInBackground(List<PageGlyphRecord>[] lists) {
            daoAccess.insertGlyphs(lists[0]);
            return null;
        }

    }

    class GetPageGlyphsTask extends AsyncTask<Object, Void, List<PageGlyphRecord>> {

        private DaoAccess daoAccess;

        GetPageGlyphsTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected List<PageGlyphRecord> doInBackground(Object... objects) {
            return daoAccess.getPageGlyphs((Long)objects[0], (Integer)objects[1]);
            //return daoAccess.getPageGlyphs();
        }

    }

    class BookUpdateTask extends AsyncTask<BookRecord, Void, Void> {

        private DaoAccess daoAccess;

        BookUpdateTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected Void doInBackground(BookRecord... bookRecords) {
            daoAccess.updateBook(bookRecords[0]);
            return null;
        }

    }

    class BookDeleteTask extends AsyncTask<Long, Void, Void> {

        private DaoAccess daoAccess;

        BookDeleteTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected Void doInBackground(Long... longs) {
            daoAccess.deleteBook(longs[0]);
            daoAccess.deleteBookGlyphs(longs[0]);
            return null;
        }

    }

    class BookGetterTask extends AsyncTask<Long, Void, BookRecord> {

        private DaoAccess daoAccess;

        BookGetterTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected BookRecord doInBackground(Long ... ids) {
            return daoAccess.getBook(ids[0]);
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

    class BookCheckerTask extends AsyncTask<File, Void, Boolean> {

        private DaoAccess daoAccess;

        BookCheckerTask(DaoAccess daoAccess) {
            this.daoAccess = daoAccess;
        }

        @Override
        protected Boolean doInBackground(File... files) {
            String checksum = MD5.fileToMD5(files[0].getPath());
            BookRecord bookRecords = daoAccess.fetchBookByChecksum(checksum);
            return bookRecords != null;
        }

    }

class BookByUrlGetterTask extends AsyncTask<String, Void, BookRecord> {

    private DaoAccess daoAccess;

    BookByUrlGetterTask(DaoAccess daoAccess) {
        this.daoAccess = daoAccess;
    }

    @Override
    protected BookRecord doInBackground(String... strings) {
        return daoAccess.fetchBook(strings[0]);
    }

}

class BookGetChecksumTask extends AsyncTask<String, Void, BookRecord> {

    private DaoAccess daoAccess;

    BookGetChecksumTask(DaoAccess daoAccess) {
        this.daoAccess = daoAccess;
    }

    @Override
    protected BookRecord doInBackground(String... strings) {
        return daoAccess.fetchBookByChecksum(strings[0]);
    }

}