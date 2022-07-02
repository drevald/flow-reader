package com.veve.flowreader.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.veve.flowreader.BookContentResolver;
import com.veve.flowreader.BuildConfig;
import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    public static final int FILE_OPEN_REQUEST = 42;
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    BookListAdapter bookListAdapter;

    BookGridAdapter bookGridAdapter;

    int columnsNumber;

    SharedPreferences preferences;

    Paint borderPaint;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        long bookId = intent.getLongExtra(Constants.BOOK_ID, 0);
        if (bookId > 0) {
            bookGridAdapter.removeBook(bookId);
            bookListAdapter.removeBook(bookId);
        }
    }

        @Override
    protected void onPostResume() {
        super.onPostResume();
        bookListAdapter.refresh();
        bookGridAdapter.refresh();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.book_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.delete_listed_book) {
            BookRecord bookRecord = (BookRecord)bookListAdapter.getItem(info.position);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.book_deletion)
                .setMessage(String.format(
                        getResources().getString(R.string.confirm_delete), bookRecord.getTitle()))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BooksCollection.getInstance(getParent()).deleteBook(bookRecord.getId());
                        bookListAdapter.refresh();
                        bookGridAdapter.refresh();
                    }
                })
                .setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (item.getItemId() == R.id.rename_listed_book) {
            BookRecord bookRecord = (BookRecord)bookListAdapter.getItem(info.position);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            EditText titleEditView = new EditText(getApplicationContext());
            //EditText titleEditView = (EditText)getLayoutInflater().inflate(R.layout.book_edit, null);
            titleEditView.setText(bookRecord.getTitle());
            titleEditView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            //titleEditView.setPadding(20, 5, 20, 5 );
            builder.setTitle(getResources().getString(R.string.rename_this_book))
                    .setMessage(R.string.new_title)
                    .setCancelable(false)
                    .setView(titleEditView)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bookRecord.setTitle(titleEditView.getText().toString());
                            BooksCollection.getInstance(getParent()).updateBook(bookRecord);
                            bookListAdapter.refresh();
                            bookGridAdapter.refresh();
                        }
                    })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // or finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == FILE_OPEN_REQUEST) {
            Uri uri = data.getData();

            try {
                Intent i = new Intent(MainActivity.this, GetBookActivity.class);
                i.setData(uri);
                startActivity(i);
            } catch (Exception e) {
                Log.v(getClass().getName(), "Couldn''t start GetBookActivity");
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        preferences = getPreferences(MODE_PRIVATE);
        bookListAdapter = new BookListAdapter();
        bookGridAdapter = new BookGridAdapter();

        requestPermissions();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ////////////    ADD BOOKS BUTTON     /////////////////////////////////////////////////////
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
//            boolean condition = android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
//            if (condition) {
//                Intent intentOne = new Intent(MainActivity.this, BrowseFilesActivity.class);
//                intentOne.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(intentOne);
//            } else {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("*/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/pdf", "image/vnd.djvu"});
                Intent chooser = Intent.createChooser(i, "Choose the file to read..");
                startActivityForResult(chooser, FILE_OPEN_REQUEST);
//            }
        });

        final GridView gridView = findViewById(R.id.grid);

        if (!preferences.contains(Constants.VIEW_TYPE)) {
            Log.d(getClass().getName(), "View type preference missing. Setting list as default");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Constants.VIEW_TYPE, Constants.GRID_VIEW_TYPE);
            editor.apply();
        }

        if (preferences.getInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE) == Constants.LIST_VIEW_TYPE) {
            Log.d(getClass().getName(), "Preference view is list");
            gridView.setNumColumns(1);
            gridView.setAdapter(bookListAdapter);
        } else {
            gridView.setAdapter(bookGridAdapter);
            Log.d(getClass().getName(),
                    String.format("Preference view is grid. Adapter class is %S",
                            gridView.getAdapter()));
        }

        gridView.setOnItemClickListener(
            (AdapterView<?> parent, View view, int position, long id) -> {
            Intent ii = new Intent(MainActivity.this, PageActivity.class);
            BookRecord selectedBook = (BookRecord)parent.getItemAtPosition(position);
            ii.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ii.putExtra(Constants.FILE_NAME, selectedBook.getUrl());
            ii.putExtra(Constants.BOOK_ID, selectedBook.getId());
            Log.v(getClass().getName(), String.format("INDENT BOOK_ID:%d URL:%s",
                    selectedBook.getId(), selectedBook.getUrl()));
            startActivity(ii);
        });

        gridView.setOnItemLongClickListener(
            (AdapterView<?> adapterView, View view, int i, long l) -> {
                Log.v(getClass().getName(), "adapterView = " + adapterView + "view = " + view +  " i = " + i + " l = " + l);
                return false;
            }
        );

        gridView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int bookThumbPx = (int) (Constants.BOOK_THUMB_WIDTH
                    * Resources.getSystem().getDisplayMetrics().density);
            int bookThumbPaddingPx = (int) (Constants.BOOK_THUMB_HOR_PADDING
                    * Resources.getSystem().getDisplayMetrics().density);
            columnsNumber = gridView.getWidth()/(bookThumbPx + 2 * bookThumbPaddingPx);
            if(preferences.getInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE) == Constants.LIST_VIEW_TYPE){
                gridView.setNumColumns(1);
            } else {
                gridView.setNumColumns(columnsNumber);
            }
        });
//
        registerForContextMenu(gridView);

        /////    SWITCH TO LIST VIEW     ///////////////////////////////

        ImageButton listBooksButton = findViewById(R.id.books_list);
        listBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "List View");
                gridView.setNumColumns(1);
                gridView.setAdapter(bookListAdapter);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(Constants.VIEW_TYPE, Constants.LIST_VIEW_TYPE);
                editor.apply();
            }
        });


        //////   SWITCH TO GRID VIEW   ////////////////////////////////

        ImageButton gridBooksButton = findViewById(R.id.books_grid);
        gridBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(getClass().getName(), "Grid View");
                gridView.setNumColumns(columnsNumber);
                gridView.setAdapter(bookGridAdapter);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(Constants.VIEW_TYPE, Constants.GRID_VIEW_TYPE);
                editor.apply();
            }
        });

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(1f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == R.id.about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View copyrightView = getLayoutInflater().inflate(R.layout.copyright_text, null);
            builder.setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_icon_flowbook_pink)
                    .setMessage(String.format(getResources().getString(R.string.program_info),
                            BuildConfig.GitHash, sdf.format(new Date())))
                    .setView(copyrightView)
                    .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();

        }
        return true;
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public void changeTheme(View view) {
        changeTheme();
    }

/////////////////////////   ADAPTERS   ///////////////////////////////////////////////////////////////

    public class BookListAdapter extends BaseAdapter {

        private List<BookRecord> booksList;

        private BookListAdapter() {
            Log.i(this.getClass().getName(), "Constructing BookListAdapter");
            booksList = BooksCollection.getInstance(getApplicationContext()).getBooks();
            notifyDataSetChanged();
        }

        void removeBook(long bookId) {
            BookRecord recordToRemove = null;
            for (BookRecord record : booksList) {
                if (record.getId() == bookId) {
                    recordToRemove = record;
                    break;
                }
            }
            booksList.remove(recordToRemove);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return booksList.size();
        }

        @Override
        public Object getItem(int position) {
            return booksList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return booksList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.book_items_list, container, false);
            }
            ImageView imageView = convertView.findViewById(R.id.thumbnail);
            byte[] bytes = booksList.get(position).getPreview();
            Bitmap thumbnailBitmap = Bitmap
                    .createScaledBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), 60, 90, false);
//            Canvas canvas = new Canvas(thumbnailBitmap);
//            canvas.drawRect(0, 0, thumbnailBitmap.getWidth(), thumbnailBitmap.getHeight(),
//                    borderPaint);
            imageView.setImageBitmap(thumbnailBitmap);
            TextView textView = convertView.findViewById(R.id.text);
                textView.setText(booksList.get(position).getTitle());
            return convertView;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof BookListAdapter;
        }

        public void refresh() {
            booksList = BooksCollection.getInstance(getApplicationContext()).getBooks();
            notifyDataSetChanged();
        }


    }
//
////////////////////////////////////////////////////////////////////////////////////////////////////
//
    public class BookGridAdapter extends BaseAdapter {

        private List<BookRecord> booksList;

        private BookGridAdapter() {
            Log.i(this.getClass().getName(), "Constructing BookListAdapter");
            booksList = BooksCollection.getInstance(getApplicationContext()).getBooks();
            for (BookRecord record : booksList)
                Log.v(getClass().getName(), String.format("BOOK ID:%d PATH:%s MD5:%s",
                        record.getId(), record.getUrl(), record.getMd5()));
            notifyDataSetChanged();
        }

        void removeBook(long bookId) {
            BookRecord recordToDelete = null;
            for (BookRecord record : booksList) {
                if (record.getId() == bookId) {
                    recordToDelete = record;
                    break;
                }
            }
            booksList.remove(recordToDelete);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if( booksList == null) {
                return -1;
            } else {
                return booksList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return booksList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return booksList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater()
                        .inflate(R.layout.book_preview, container, false);
            }
            TextView textView = convertView.findViewById(R.id.caption);
            ImageView imageView = convertView.findViewById(R.id.thumbnail);
            byte[] bytes = booksList.get(position).getPreview();
            Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Canvas canvas = new Canvas(thumbnailBitmap);
//            canvas.drawRect(0, 0, thumbnailBitmap.getWidth(), thumbnailBitmap.getHeight(),
//                    borderPaint);
            imageView.setImageBitmap(thumbnailBitmap);
            textView.setText(booksList.get(position).getTitle());
            Log.v(getClass().getName(), convertView.toString());
            return convertView;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof BookGridAdapter;
        }

        public void refresh() {
            booksList = BooksCollection.getInstance(getApplicationContext()).getBooks();
            notifyDataSetChanged();
        }

    }

///////////////////////////////////////////////////////////////////////////////////////////////////

}

