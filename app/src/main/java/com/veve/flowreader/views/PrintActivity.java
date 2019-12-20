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
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
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

                    PdfDocument.PageInfo.Builder pageBuilder = new PdfDocument.PageInfo.Builder(
                            (int)(attributes.getMediaSize().getWidthMils()*attributes.getResolution().getHorizontalDpi()*0.001f) ,
                            (int)(attributes.getMediaSize().getHeightMils()*attributes.getResolution().getVerticalDpi()*0.001f),
                            i);
                    PdfDocument.Page page = pdfDocument.startPage(pageBuilder.create());

                    // check for cancellation
                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        pdfDocument.close();
                        return;
                    }

                    PageGetterTask pageGetterTask = new PageGetterTask();
                    int widthInMils = attributes.getMediaSize().getWidthMils();
                    int workWidthInMils = widthInMils - attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils();
                    int gap = (attributes.getMinMargins().getLeftMils() - attributes.getMinMargins().getRightMils())/2;
                    workWidthInMils = workWidthInMils - gap;
                    int columnsWidthInMils = workWidthInMils / 2;
                    int columnsWithInPixels = (int)(columnsWidthInMils * 0.001f * attributes.getResolution().getHorizontalDpi());
                    pageGetterTask.execute(i, columnsWithInPixels);
                    try {
                        Bitmap bitmap = pageGetterTask.get();
                        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
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
        String jobName = getActivity().getString(R.string.app_name) + "_" + bookRecord.getTitle();

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
        bookRecord = BooksCollection.getInstance(getApplicationContext()).getBook(bookRecordId);

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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ((RadioGroup)findViewById(R.id.page_range)).check(R.id.current_page);
        pages = ((EditText) findViewById(R.id.pages));
        pages.setText("" + bookRecord.getCurrentPage());
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
        ((EditText)findViewById(R.id.pages)).setText("" + bookRecord.getCurrentPage());
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintAllPages(View view) {
        ((EditText)findViewById(R.id.pages)).setText("1-" + bookRecord.getPagesCount());
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_NULL);
        parsePagesString();
    }

    public void setPrintCustomRange(View view) {
        ((EditText)findViewById(R.id.pages)).setInputType(EditorInfo.TYPE_CLASS_TEXT);
        parsePagesString();
    }

    static class PageGetterTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            //return pageRenderer.renderOriginalPage(integers[0]);
            DevicePageContext context = new DevicePageContextImpl(integers[1]);
            return pageRenderer.renderPage(context, integers[0]);
        }

    }

}

class PageBitmapReader {

    List<PagesSet> pages;
    int width;
    int height;
    Bitmap bitmap;
    Stack<Integer> singlePages;
    PrintActivity.PageGetterTask pageGetterTask;
    Bitmap bitmapSuffix;
    Bitmap appendedBitmap;
    Bitmap cutBitmap;
    Canvas canvas;

    public PageBitmapReader(List<PagesSet> pages, int width, int height) {
        this.pages = pages;
        this.width = width;
        this.height = height;
        this.pageGetterTask = new PrintActivity.PageGetterTask();
        initPages();
    }

    public Bitmap read() throws Exception {
        Bitmap result = null;
        // If bitmap is empty and there are pages to render then get bitmap
        if (bitmap == null && !singlePages.empty()) {
            pageGetterTask.execute(singlePages.pop(), width);
            bitmap = pageGetterTask.get();
        // If bitmap is empty and there are no pages to render then finish
        } else if (bitmap == null && singlePages.empty()) {
            return null;
        }
        // If bitmap is shorter
        if (bitmap.getHeight() < height) {
            Integer nextPage = singlePages.pop();
            // ... and there are pages to render then append bitmap with new page
            if (nextPage != null) {
                pageGetterTask.execute(singlePages.pop(), width);
                bitmapSuffix = pageGetterTask.get();
                appendedBitmap = Bitmap.createBitmap(width,
                        bitmap.getHeight() + bitmapSuffix.getHeight(), Bitmap.Config.ALPHA_8);
                canvas = new Canvas(appendedBitmap);
                canvas.drawBitmap(bitmap, 0, 0, null);
                canvas.drawBitmap(bitmapSuffix, 0, bitmap.getHeight(), null);
                result = Bitmap.createBitmap(appendedBitmap, 0, 0, width, height);
                bitmap = Bitmap.createBitmap(appendedBitmap, 0, height, width, appendedBitmap.getHeight()-height);
            // ... and there are no pages to render then return the remains
            } else {
                result = Bitmap.createBitmap(bitmap);
                bitmap = null;
            }
        // If we got bitmap large enough then cut required piece and keep the rest
        } else {
            result = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            cutBitmap = Bitmap.createBitmap(bitmap, 0, height, width, bitmap.getHeight()-height);
            bitmap = cutBitmap;
        }
        return result;
    }

    private void initPages() {
        SortedSet<Integer> sortedPages = new TreeSet<Integer>();
        for (PagesSet pagesSet : pages) {
            for (int i=pagesSet.getStart(); i<=pagesSet.getEnd(); i++) {
                sortedPages.add(i);
            }
        }
        singlePages = new Stack<Integer>();
        for(Integer page : singlePages) {
            singlePages.push(page);
        }
    }

}








