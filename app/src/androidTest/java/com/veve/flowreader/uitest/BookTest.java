package com.veve.flowreader.uitest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.test.platform.app.InstrumentationRegistry;

import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookFactory;
import com.veve.flowreader.model.BooksCollection;


import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BookTest {

    protected long testBookId;
    BookRecord bookRecord;
    Context appContext;
    BooksCollection booksCollection;

    @Before
    public void prepareCollection() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        booksCollection = BooksCollection.getInstance(appContext);
        File file = new File(appContext.getExternalFilesDir(null), "pdf_sample.pdf");
//        File file = new File(appContext.getExternalFilesDir(null), "djvu_sample.djvu");
        file.createNewFile();
        BookRecord oldBookRecord = booksCollection.getBook(file.getPath());
        if (oldBookRecord != null) {
            booksCollection.deleteBook(oldBookRecord.getId());
        }
        InputStream is = appContext.getResources().openRawResource(R.raw.pdf_sample);
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[100];
        while(is.read(buffer) != -1) {
            os.write(buffer);
        }
        os.close();
        is.close();
        bookRecord = BookFactory.getInstance().createBook(file);
        testBookId = booksCollection.addBook(bookRecord);
        bookRecord = booksCollection.getBook(testBookId);
    }


    public static boolean areDrawablesIdentical(Drawable drawableA, Drawable drawableB) {
        Drawable.ConstantState stateA = drawableA.getConstantState();
        Drawable.ConstantState stateB = drawableB.getConstantState();
        // If the constant state is identical, they are using the same drawable resource.
        // However, the opposite is not necessarily true.
        return (stateA != null && stateB != null && stateA.equals(stateB))
                || getBitmap(drawableA).sameAs(getBitmap(drawableB));
    }

    public static Bitmap getBitmap(Drawable drawable) {
        Bitmap result;
        if (drawable instanceof BitmapDrawable) {
            result = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            // Some drawables have no intrinsic width - e.g. solid colours.
            if (width <= 0) {
                width = 1;
            }
            if (height <= 0) {
                height = 1;
            }

            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return result;
    }

}
