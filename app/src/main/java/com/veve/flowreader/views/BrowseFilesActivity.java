package com.veve.flowreader.views;

import android.Manifest;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseFilesActivity extends AppCompatActivity {

    FileListAdapter fileListAdapter;

    private ProgressBar progress;

    @Override
    protected void onResume() {
        super.onResume();
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();

        setContentView(R.layout.activity_browse_files);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress);

        //GET BACK BUTTON
        FloatingActionButton home = findViewById(R.id.home);
        home.setOnClickListener(view -> {
                Intent i = new Intent(BrowseFilesActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
        });


        // FILES LIST
        fileListAdapter = new FileListAdapter();
        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(fileListAdapter);
        listView.setOnItemClickListener(new FileListener(fileListAdapter));
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


///    ADAPTERS    ///////////////////////////////////////////

    class FileListAdapter extends BaseAdapter {

        private File rootDir;

        File currentDirectory;
        List<File> currentFiles;

        FileListAdapter() {
            super();
            setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        protected void setRoot(String path) {
            try {
                rootDir = new File(path);
                currentDirectory = rootDir;
                currentFiles = new ArrayList<>();
                for (File file : currentDirectory.listFiles()) {
                    if (file.canRead())
                        currentFiles.add(file);
                }
                Collections.sort(currentFiles, (File fileOne, File fileTwo) -> {
                    if (fileOne.isDirectory() && fileTwo.isFile()) {
                        return -1;
                    } else if (fileOne.isFile() && fileTwo.isDirectory()) {
                        return 1;
                    } else return fileOne.getName().compareTo(fileTwo.getName());
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

        FileListener(FileListAdapter fileListAdapter) {
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
                        new BookCreatorTask().execute(file);
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

        private BookRecord newBook;

        private long bookId;

        @Override
        protected Void doInBackground(File... files) {
            File file = files[0];
            runOnUiThread(
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            Log.v(getClass().getName(), "Setting progress bar visible - adding book");
                            progress.setVisibility(View.VISIBLE);
                        }
                    })
            );
            Log.v(getClass().getName(), "Start parsing new book");
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
            Intent ii = new Intent(BrowseFilesActivity.this, PageActivity.class);
            ii.putExtra("bookId", bookId);
            ii.putExtra("filename", newBook.getUrl());
            startActivity(ii);
        }
    }

    ///////////////////////////////////////////////

    public void browseInternalMemory(View view) {
        fileListAdapter.setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        fileListAdapter.notifyDataSetChanged();
    }

    public void browseExternalMemory(View view) {
        fileListAdapter.setRoot("/storage/sdcard1/");
        fileListAdapter.notifyDataSetChanged();
    }

}

