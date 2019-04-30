package com.veve.flowreader.views;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.Book;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrowseFilesActivity extends AppCompatActivity {

    private static final String INTERNAL_ROOT = "/storage/sdcard0/";

    private static final String EXTERNAL_ROOT = "/storage/sdcard1/";

    FileListAdapter fileListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();

        setContentView(R.layout.activity_browse_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //GET BACK BUTTON
        FloatingActionButton home = (FloatingActionButton) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(BrowseFilesActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });


        // FILES LIST
        fileListAdapter = new FileListAdapter();
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(fileListAdapter);
        listView.setOnItemClickListener(new FileListener(fileListAdapter));
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }


///    ADAPTERS    ///////////////////////////////////////////

    class FileListAdapter extends BaseAdapter {

        private File rootDir;

        public File currentDirectory;
        List<File> currentFiles;

        public FileListAdapter() {
            super();
            setRoot(INTERNAL_ROOT);
        }

        protected void setRoot(String path) {
            try {
                rootDir = new File(path);
                currentDirectory = rootDir;
                currentFiles = new ArrayList<File>();
                for (File file : currentDirectory.listFiles()) {
                    if (file.canRead())
                        currentFiles.add(file);
                }
                Collections.sort(currentFiles, new Comparator<File>() {
                    public int compare(File fileOne, File fileTwo) {
                        if (fileOne.isDirectory() && fileTwo.isFile()) {
                            return -1;
                        } else if (fileOne.isFile() && fileTwo.isDirectory()) {
                            return 1;
                        } else return fileOne.getName().compareTo(fileTwo.getName());
                    }
                });
            } catch (Exception e) {
//                Log.e(this.getClass().getName(), e.getMessage());
                e.printStackTrace();
            }
        }

        private void setRoot(int index) {
            Log.d(this.getClass().getName(), "setRoot");
            File newRoot = currentFiles.get(index);
            if (newRoot.isDirectory()) {
                if (newRoot.canRead()) {
                    currentFiles.clear();
                    for (File file : newRoot.listFiles()) {
                        if (file.canRead())
                            currentFiles.add(file);
                    }
                    if (!rootDir.equals(newRoot))
                        currentFiles.add(0, newRoot.getParentFile());
                    currentDirectory = newRoot;
                    notifyDataSetChanged();
                    Log.i(this.getClass().getName(), "Changing root to " + newRoot.getAbsolutePath());
                } else {
                    Log.i(this.getClass().getName(), "Sorry, you can not read " + newRoot.getAbsolutePath());
                }
            } else {
                Log.i(this.getClass().getName(), newRoot.getAbsolutePath() + " is file");
            }
        }

        @Override
        public int getCount() {
            return currentFiles.size();
        }

        @Override
        public Object getItem(int i) {
            return currentFiles.get(i);
        }

        @Override
        public long getItemId(int i) {
            return currentFiles.get(i).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.items_list, container, false);
            }
            TextView textView = (TextView) ((ConstraintLayout) convertView).getChildAt(0);
            File selectedFile = currentFiles.get(position);
            textView.setText(selectedFile.getName());
            if (!selectedFile.canRead()) {
                textView.setTextColor(Constants.LIGHT_PINK);
            } else if (selectedFile.canWrite()) {
                textView.setTextColor(Constants.GREEN);
            } else {
                textView.setTextColor(Color.BLACK);
            }
            if (selectedFile.isDirectory()) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }
            if (position == 0 && !currentDirectory.equals(rootDir)) {
                textView.setText("..");
            }
            return convertView;
        }

    }

///   LISTENERS    ///////////////////////////////////////////

    public class FileListener implements AdapterView.OnItemClickListener {

        FileListAdapter fileListAdapter;

        public FileListener(FileListAdapter fileListAdapter) {
            this.fileListAdapter = fileListAdapter;
        }

        @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(this.getClass().getName(), i + " clicked");
            if (fileListAdapter.currentFiles.get(i).isFile()) {
                File file = fileListAdapter.currentFiles.get(i);
                if (!file.getName().toLowerCase().endsWith(".djvu")
                        && !file.getName().toLowerCase().endsWith(".pdf")) {
                    Snackbar.make(view, getString(R.string.ui_unsupported_format),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    if (BooksCollection.getInstance(getApplicationContext()).hasBook(file)) {
                        Snackbar.make(view, getString(R.string.ui_book_already_added),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        BookRecord newBook = BookFactory.getInstance().createBook(file);
                        newBook.setCurrentPage(0);
                        newBook.setUrl(file.getAbsolutePath());
                        BooksCollection.getInstance(getApplicationContext()).addBook(newBook);
                        Intent ii = new Intent(BrowseFilesActivity.this, MainActivity.class);
                        startActivity(ii);
                    }
                }
            } else if (!fileListAdapter.currentFiles.get(i).canRead()) {
                Snackbar.make(view, getString(R.string.ui_no_permission),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            fileListAdapter.setRoot(i);
        }

    }

    class BookCreatorTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... files) {

            return null;
        }
    }

    ///////////////////////////////////////////////

    public void browseInternalMemory(View view) {
        //fileListAdapter.setRoot(INTERNAL_ROOT);
        //fileListAdapter.setRoot(getApplicationContext().getFilesDir().getAbsolutePath());
        //fileListAdapter.setRoot("/storage/sdcard0/");
        fileListAdapter.setRoot("/storage/emulated/0/");
        fileListAdapter.notifyDataSetChanged();
    }

    public void browseExternalMemory(View view) {
        //fileListAdapter.setRoot(EXTERNAL_ROOT);
        //fileListAdapter.setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        fileListAdapter.setRoot("/storage/emulated/1/");
        fileListAdapter.notifyDataSetChanged();
    }


}

