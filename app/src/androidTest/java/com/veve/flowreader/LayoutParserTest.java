package com.veve.flowreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static junit.framework.Assert.assertNotNull;
//import com.veve.flowreader.R;

@RunWith(AndroidJUnit4.class)
public class LayoutParserTest {

    @Test
    public void test() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputStream is = appContext.getResources().openRawResource(R.raw.one_column);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        assertNotNull(bitmap);

    }

}
