package com.veve.flowreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.veve.flowreader.model.BookPage;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.pdf.PdfBook;
import com.veve.flowreader.model.impl.pdf.PdfBookPage;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.veve.flowreader", appContext.getPackageName());
    }

//    @Test
//    public void testPdf() throws Exception {
//        PdfBook pdfBook = new PdfBook("/sdcard/shared-mime-info-spec.pdf");
//        BookPage pdfBookPage = pdfBook.getPage(0);
//        assertEquals(pdfBookPage.getHeight(), 789);
//        assertEquals(pdfBookPage.getWidth(), 609);
//        DevicePageContext context = new DevicePageContextImpl();
//        context.setDisplayDpi(72);
//        Bitmap bitmap = pdfBookPage.getAsBitmap(context);
//        assertEquals(bitmap.getHeight(), 789);
//        assertEquals(bitmap.getWidth(), 609);
//    }


}
