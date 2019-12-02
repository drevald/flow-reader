package com.veve.flowreader;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

public class PrintActivity extends AppCompatActivity {

    class MyPrintDocumentAdapter extends PrintDocumentAdapter {

        public MyPrintDocumentAdapter(Activity activity) {
            Log.v(getClass().getName(), "Construct");
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
                            final PrintDocumentAdapter.WriteResultCallback callback) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Log.v(getClass().getName(), "onWrite");

                PdfDocument pdfDocument = null;
                pdfDocument = new PdfDocument();

                PdfDocument.PageInfo.Builder pageOneBuilder = new PdfDocument.PageInfo.Builder(640, 480, 1);
                PdfDocument.Page page = pdfDocument.startPage(pageOneBuilder.create());
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawText("HERE I AM", 100, 100, paint);
                pdfDocument.finishPage(page);
                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (Exception e) {
                    e.printStackTrace();
                }


//            // Iterate over each page of the document,
//            // check if it's in the output range.
//            for (int i = 0; i < totalPages; i++) {
//                // Check to see if this page is in the output range.
//                if (containsPage(pageRanges, i)) {
//                    // If so, add it to writtenPagesArray. writtenPagesArray.size()
//                    // is used to compute the next output page index.
//                    writtenPagesArray.append(writtenPagesArray.size(), i);
//                    PdfDocument.Page page = pdfDocument.startPage(i);
//
//                    // check for cancellation
//                    if (cancellationSignal.isCanceled()) {
//                        callback.onWriteCancelled();
//                        pdfDocument.close();
//                        pdfDocument = null;
//                        return;
//                    }
//
//                    // Draw page content for printing
//                    drawPage(page);
//
//                    // Rendering is complete, so page can be finalized.
//                    pdfDocument.finishPage(page);
//                }
//            }
//
//            // Write PDF document to file
//            try {
//                pdfDocument.writeTo(new FileOutputStream(
//                        destination.getFileDescriptor()));
//            } catch (IOException e) {
//                callback.onWriteFailed(e.toString());
//                return;
//            } finally {
//                pdfDocument.close();
//                pdfDocument = null;
//            }
//            PageRange[] writtenPages = computeWrittenPages();
//            // Signal the print framework the document is complete
//            callback.onWriteFinished(writtenPages);
//
//    //...

            }
        }



    }

    private void doPrint() {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getActivity().getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new MyPrintDocumentAdapter(getActivity()),
                null); //
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                doPrint();

            }
        });
    }

}
