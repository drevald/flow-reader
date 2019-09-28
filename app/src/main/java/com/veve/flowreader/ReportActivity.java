package com.veve.flowreader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.veve.flowreader.dao.AppDatabase;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.dao.DaoAccess;
import com.veve.flowreader.dao.ReportRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    Bitmap originalBitmap;
    Bitmap overturnedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookId = getIntent().getLongExtra("reportId", -1);


        ReportGetterTask reportGetterTask = new ReportGetterTask();
        ReportRecord reportRecord;

        try {
            reportGetterTask.execute(bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportSenderTask reportSenderTask = new ReportSenderTask();
            }
        });

    }

    class ReportGetterTask extends AsyncTask<Long, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Long... longs) {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            DaoAccess daoAccess = appDatabase.daoAccess();
            return daoAccess.getReport(longs[0]);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            long totalSize = 0L;
            cursor.moveToFirst();
            totalSize += cursor.getBlob(cursor.getColumnIndex("overturnedPage")).length;
            totalSize += cursor.getBlob(cursor.getColumnIndex("originalPage")).length;
            totalSize += cursor.getBlob(cursor.getColumnIndex("glyphs")).length;
            TextView textView = findViewById(R.id.size_note);
            textView.setText(String.format(Locale.getDefault(),
                    getString(R.string.notify_send_size), totalSize));
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Log.v(getClass().getName(), "Progress " + values);
        }
    }

    class ReportSenderTask extends AsyncTask<ReportRecord, Long, Long> {

        Long reportIncomingId;

        @Override
        protected Long doInBackground(ReportRecord... reportRecords) {
//            publishProgress();

            try {
                URL url = new URL(Constants.REPORT_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                //https://developer.android.com/training/volley/simple.html
                //or do it by yourself - https://stackoverflow.com/questions/913626/what-should-a-multipart-http-request-with-multiple-files-look-like

            } catch (Exception e) {

            } finally {
                return null;

            }

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
        }
    }


}




