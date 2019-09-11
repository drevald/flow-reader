package com.veve.flowreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.InputStream;

import com.veve.flowreader.test.R;

import static android.graphics.BitmapFactory.*;
import static junit.framework.Assert.assertNotNull;
//import com.veve.flowreader.R;

@RunWith(AndroidJUnit4.class)
public class LayoutParserTest {

    @Test
    public void test() {
        Context appContext = InstrumentationRegistry.getTargetContext();
//        InputStream is = appContext.getResources()
//                .openRawResource(R.raw.one_column);
//        assertNotNull(is);
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
//        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

//        Bitmap bitmap = BitmapFactory
//                .decodeResource(appContext.getResources(), R.raw.one_column);
//        assertNotNull(bitmap);
    }

}
