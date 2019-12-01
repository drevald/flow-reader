package com.veve.flowreader;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
