package com.veve.flowreader.views;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrowseFilesActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();

        setContentView(R.layout.activity_browse_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FloatingActionButton home = (FloatingActionButton) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(BrowseFilesActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        final FileListAdapter fileListAdapter = new FileListAdapter();
        setListAdapter(fileListAdapter);

        ListView listView = (ListView) findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(this.getClass().getName(), i + " clicked");
                if (fileListAdapter.currentFiles.get(i).isFile()) {
                    File file = fileListAdapter.currentFiles.get(i);
                    if (!file.getName().toLowerCase().endsWith(".djvu")) {
                        Snackbar.make(view, "Only DJVU files supported", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Intent ii = new Intent(BrowseFilesActivity.this, PageViewActivity.class);
                        ii.putExtra("filename", file.getAbsolutePath());
                        startActivity(ii);
                    }
                } else if (!fileListAdapter.currentFiles.get(i).canRead()) {
                    Snackbar.make(view, "You could not open this", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                fileListAdapter.setRoot(i);
            }
        });
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

    class FileListAdapter extends BaseAdapter {

        private File rootDir;

        public File currentDirectory;
        List<File> currentFiles;

        public FileListAdapter() {
            super();
            try {
                rootDir = new File("/");
                currentDirectory = rootDir;
                currentFiles = new ArrayList<File>();
                currentFiles.addAll(Arrays.asList(currentDirectory.listFiles()));
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
                Log.e(this.getClass().getName(), e.getMessage());
                e.printStackTrace();
            }
        }

        public void setRoot(int index) {
            Log.d(this.getClass().getName(), "setRoot");
            File newRoot = currentFiles.get(index);
            if (newRoot.isDirectory()) {
                if (newRoot.canRead()) {
                    currentFiles.clear();
                    currentFiles.addAll(Arrays.asList(newRoot.listFiles()));
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
            textView.setText(selectedFile.getAbsolutePath());
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

}