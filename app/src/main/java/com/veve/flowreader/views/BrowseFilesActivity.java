package com.veve.flowreader.views;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.veve.flowreader.model.BooksCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseFilesActivity extends AppCompatActivity {

    FileListAdapter fileListAdapter;

    private static final String EXTERNAL_MEMORY_DIR = "/storage/sdcard1/";

    private ProgressBar progress;

    @Override
    protected void onResume() {
        super.onResume();
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse_files);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress);

        if (new File(EXTERNAL_MEMORY_DIR).exists()) {
            findViewById(R.id.internal_memory).setVisibility(View.VISIBLE);
            findViewById(R.id.external_memory).setVisibility(View.VISIBLE);
        }

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
                Log.d(getClass().getName(), String.format("setRoot(%s)", path));
                currentDirectory = rootDir;
                currentFiles = new ArrayList<>();
                Log.d(getClass().getName(), String.format("currentDirectory = " + currentDirectory));
                Log.d(getClass().getName(), String.format("currentDirectory.listFiles() = " + currentDirectory.listFiles()));
                Log.d(getClass().getName(), String.format("currentDirectory.listFiles() = ",
                        currentDirectory.listFiles() == null ? "null" :
                                currentDirectory.listFiles().length + " files"));
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
            Intent bookIntent = new Intent("android.intent.action.VIEW", Uri.fromFile(file));
            bookIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            bookIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            bookIntent.setComponent(ComponentName.unflattenFromString("com.veve.flowreader/.views.GetBookActivity"));
            getApplicationContext().startActivity(bookIntent);
            return null;
        }

    }

    ///////////////////////////////////////////////

    public void browseInternalMemory(View view) {
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            fileListAdapter.setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        } else {
            fileListAdapter.setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        fileListAdapter.notifyDataSetChanged();
    }

    public void browseExternalMemory(View view) {
        fileListAdapter.setRoot(EXTERNAL_MEMORY_DIR);
        fileListAdapter.notifyDataSetChanged();
    }

}

