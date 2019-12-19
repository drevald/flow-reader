package com.veve.flowreader.views;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.PageRendererImpl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.Bitmap.Config.ARGB_4444;

public class PrintActivity extends AppCompatActivity {

    static PageRenderer pageRenderer;

    class PrintBookAdapter extends PrintDocumentAdapter {

        BookRecord bookRecord;

        PrintAttributes newAttributes;

        public PrintBookAdapter(Activity activity, BookRecord bookRecord) {
            Log.v(getClass().getName(), "Construct");
            this.bookRecord = bookRecord;
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.v(getClass().getName(), "on start");
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle extras) {
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Log.v(getClass().getName(), "onLayout");
            PrintDocumentInfo info = new PrintDocumentInfo
                    .Builder("print_output.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(10)
                    .build();
                callback.onLayoutFinished(info, false);
                Log.v(getClass().getName(), "onLayout 1");
            }
        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback)  {
            Log.v(getClass().getName(), "onWrite");

            PdfDocument pdfDocument = new PdfDocument();

            // Iterate over each page of the document,
            // check if it's in the output range.
            //for (int i = 0; i < totalPages; i++) {
            for (int i = 1; i < 5; i++) {
                // Check to see if this page is in the output range.
                //if (containsPage(pageRanges, i)) {
                    // If so, add it to writtenPagesArray. writtenPagesArray.size()
                    // is used to compute the next output page index.
                    //writtenPagesArray.append(writtenPagesArray.size(), i);
                    //PdfDocument.Page page = pdfDocument.startPage(i);

                    PdfDocument.PageInfo.Builder pageBuilder = new PdfDocument.PageInfo.Builder(100, 100, i);
                    PdfDocument.Page page = pdfDocument.startPage(pageBuilder.create());

                    // check for cancellation
                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        pdfDocument.close();
                        pdfDocument = null;
                        return;
                    }

                    // Draw page content for printing
                    // drawPage(page);

//                    View content = findViewById(R.id.box);
//                    content.draw(page.getCanvas());
                    //Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_icon_flowbook);

//                    Bitmap bitmap = Bitmap.createBitmap(160, 160,  Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(bitmap);
//                    Paint paint = new Paint();
//                    paint.setColor(Color.GREEN);
//                    paint.setStyle(Paint.Style.STROKE);
//                    paint.setTextSize(100);
//                    //paint.setFontFeatureSettings();               //
//                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
//                    paint.setColor(Color.RED);
//                    paint.setStyle(Paint.Style.FILL);
//                    canvas.drawText(String.valueOf(i), 50, 50, paint);
//                    canvas.getDensity();
//
//                    Bitmap bitmap1 = pageRenderer.renderOriginalPage(i);

                    //page.getCanvas().drawBitmap(bitmap1, 50, 50, null);

                    //page.getCanvas().drawBitmap(bitmap3, 0, 0, new Paint());

                    PageGetterTask pageGetterTask = new PageGetterTask();
                    pageGetterTask.execute(i);
                    try {
                        Bitmap bitmap = Bitmap.createScaledBitmap(pageGetterTask.get(), 100, 100, true);
                        page.getCanvas().drawBitmap(bitmap, 20, 20, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // Rendering is complete, so page can be finalized.
                    pdfDocument.finishPage(page);
                //}
            }
//
            // Write PDF document to file
            try {
                pdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                pdfDocument.close();
                pdfDocument = null;
            }
            //PageRange[] writtenPages = computeWrittenPages();
            // Signal the print framework the document is complete
            //callback.onWriteFinished(writtenPages);
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

//
//    //...
        }

    }

    private void doPrint(BookRecord bookRecord) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getActivity().getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new PrintBookAdapter(getActivity(), bookRecord),
                null); //
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookRecordId = getIntent().getLongExtra(Constants.BOOK_ID, -1);
        BookRecord bookRecord = BooksCollection.getInstance(getApplicationContext()).getBook(bookRecordId);

        try {
            pageRenderer = PageRendererFactory.getRenderer(BooksCollection.getInstance(getApplicationContext()), bookRecord);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrint(bookRecord);
            }
        });
    }

    static class PageGetterTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            return pageRenderer.renderOriginalPage(integers[0]);
        }

    }

}



