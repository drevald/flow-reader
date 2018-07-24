package com.veve.flowreader;

import android.graphics.Canvas;
import android.graphics.Point;

import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.PageRendererImpl;
import com.veve.flowreader.model.impl.djvu.DjvuBookSource;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

//    static {
//        System.loadLibrary("native-lib");
//    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


//    @Test
//    public void testDjvu() throws Exception {
//        BookSource bookSource = new DjvuBookSource("src/main/res/raw/djvu_sample.djvu");
//        PageRenderer renderer = new PageRendererImpl(bookSource);
//        DevicePageContext context = new DevicePageContextImpl();
//        renderer.renderPage(context, 1);
//    }

}