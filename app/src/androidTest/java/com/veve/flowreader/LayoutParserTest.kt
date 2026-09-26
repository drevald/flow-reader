package com.veve.flowreader

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LayoutParserTest {

    @Test
    fun test() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream = appContext.resources.openRawResource(R.raw.one_column)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        assertNotNull(bitmap)
    }
}
