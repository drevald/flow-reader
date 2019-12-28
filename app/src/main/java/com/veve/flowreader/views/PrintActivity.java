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
import com.veve.flowreader.PageTailor;
import com.veve.flowreader.PagesSet;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CancellationSignal;
import android.os.Debug;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import static android.graphics.Bitmap.Config.ARGB_4444;

public class PrintActivity extends AppCompatActivity {

    static PageRenderer pageRenderer;

    BookRecord bookRecord;

    List<PagesSet> pagesSets;

    EditText pages;

    DevicePageContext context;

    PrintJob printJob;

    class PrintBookAdapter extends PrintDocumentAdapter {

        BookRecord bookRecord;

        PrintAttributes attributes;

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
//                    .setPageCount(10)
                        .build();
                attributes = newAttributes;
                callback.onLayoutFinished(info, false);
                Log.v(getClass().getName(), "onLayout 1");
            }
        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback) {

            Log.v(getClass().getName(), "onWrite");
            PdfDocument pdfDocument = new PdfDocument();

            int colNum = 2;
            int widthInMils = attributes.getMediaSize().getWidthMils();
            int heightInMils = attributes.getMediaSize().getHeightMils();
            int workWidthInMils = widthInMils - attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils();
            int workHeightInMils = heightInMils - attributes.getMinMargins().getTopMils() - attributes.getMinMargins().getBottomMils();
            int gap = (attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils()) / colNum;
            workWidthInMils = workWidthInMils - gap;
            int columnsWidthInMils = workWidthInMils / colNum;
            int gapPixels = (int)(gap * 0.001f  * attributes.getResolution().getHorizontalDpi());
            int topMarginInPixel = (int)(attributes.getMinMargins().getTopMils() * 0.001f * attributes.getResolution().getVerticalDpi());
            int leftMarginInPixel = (int)(attributes.getMinMargins().getLeftMils() * 0.001f * attributes.getResolution().getHorizontalDpi());
            int columnsWithInPixels = (int) (columnsWidthInMils * 0.001f * attributes.getResolution().getHorizontalDpi());
            int columnsHeightInPixels = (int) (workHeightInMils * 0.001f * attributes.getResolution().getVerticalDpi());
            try {
                context.setZoom(1);
                context.setWidth(columnsWithInPixels);
                context.setScreenRatio(columnsWithInPixels/(float)columnsHeightInPixels);
                PageTailor pageTailor = new PageTailor(pageRenderer, pagesSets, context, columnsHeightInPixels);
                Bitmap bitmap;
                int counter = 0;
                PdfDocument.Page page = null;
                while ((bitmap = pageTailor.read()) != null) {

                    // check for cancellation
                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        pdfDocument.close();
                        return;
                    }

                    int pageNum = (int)(counter/colNum);
                    PdfDocument.PageInfo.Builder pageBuilder = new PdfDocument.PageInfo.Builder(
                            (int) (attributes.getMediaSize().getWidthMils() * attributes.getResolution().getHorizontalDpi() * 0.001f),
                            (int) (attributes.getMediaSize().getHeightMils() * attributes.getResolution().getVerticalDpi() * 0.001f),
                            pageNum);
                    if (counter%colNum == 0) {
                        page = pdfDocument.startPage(pageBuilder.create());
                    }
                    Log.v(getClass().getName(), String.format("Drawing column from x:%d y:%d w:%d h:%d on page %d",
                            leftMarginInPixel + ((counter % colNum)*(leftMarginInPixel + columnsWithInPixels)),
                            topMarginInPixel,
                            bitmap.getWidth(),
                            bitmap.getHeight(),
                            pageNum));
                    page.getCanvas().drawBitmap(bitmap, leftMarginInPixel + ((counter % colNum)*(leftMarginInPixel + columnsWithInPixels)), topMarginInPixel, null);
                    if (Constants.DEBUG) {
                        page.getCanvas().drawRect(leftMarginInPixel + ((counter % colNum)*(leftMarginInPixel + columnsWithInPixels)), topMarginInPixel, bitmap.getWidth(), bitmap.getHeight(), new Paint());
                    }

                    if ((counter + 1)%colNum == 0) {
                        Log.v(getClass().getName(), String.format("Closing page %d, counter is %d number of columns %d", pageNum, counter, colNum));
                        pdfDocument.finishPage(page);
                    }

                    counter++;

                }

                try{
                    pdfDocument.finishPage(page);
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.getLocalizedMessage());
                }



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

            } catch (Exception e) {
                e.printStackTrace();
            }

            //PageRange[] writtenPages = computeWrittenPages();
            // Signal the print framework the document is complete
            //callback.onWriteFinished(writtenPages);
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

//
//    //...
            //}

        }

    }


    private void doPrint(BookRecord bookRecord) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getActivity()
                .getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getActivity().getString(R.string.app_name) + "_" + bookRecord.getTitle();

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        PrintAttributes printAttributes = builder.build();
        printJob = printManager.print(jobName, new PrintBookAdapter(getActivity(), bookRecord), printAttributes); //

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
        context = (DevicePageContext) getIntent().getSerializableExtra(Constants.BOOK_CONTEXT);
        bookRecord = BooksCollection.getInstance(getApplicationContext()).getBook(bookRecordId);

        try {
            pageRenderer = PageRendererFactory.getRenderer(BooksCollection.getInstance(getApplicationContext()), bookRecord);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
        }

        findViewById(R.id.print).setOnClickListener((view) -> {
                doPrint(bookRecord);
            }
        );

        findViewById(R.id.cancel).setOnClickListener((view) -> {
            if (printJob != null && printJob.isStarted() || printJob.isQueued())
                printJob.cancel();
            }
        );

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ((RadioGroup)findViewById(R.id.page_range)).check(R.id.custom_page_range);
        pages = ((EditText) findViewById(R.id.pages));
        pages.setText("1-10");
        parsePagesString();
        pages.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                parsePagesString();
            }
        });
    }

    private void parsePagesString() {
        try {
            pagesSets = PagesSet.getPagesSet((pages).getEditableText().toString());
            pages.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } catch (NumberFormatException e) {
            pages.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public void setPrintCurrentPage(View view) {
        ((EditText)findViewById(R.id.pages)).setText("" + (1 + bookRecord.getCurrentPage()));
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintAllPages(View view) {
        ((EditText)findViewById(R.id.pages)).setText("1-" + (1 + bookRecord.getPagesCount()));
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintCustomRange(View view) {
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_CLASS_TEXT);
        parsePagesString();
    }

}
