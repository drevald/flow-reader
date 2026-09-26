package com.veve.flowreader.uitest

import android.content.Intent
import android.graphics.drawable.VectorDrawable
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.views.PageActivity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ModeBookTest : BookTest() {

    lateinit var pageActivity: PageActivity
    lateinit var iconDrawableToPhone: VectorDrawable
    lateinit var iconDrawableToBook: VectorDrawable

    @Rule
    @JvmField
    val pageActivityRule = ActivityTestRule(
        PageActivity::class.java,
        true,   // initialTouchMode
        false   // launchActivity
    )

    @Before
    fun getIcons() {
        iconDrawableToPhone = appContext.resources.getDrawable(R.drawable.ic_to_phone, null) as VectorDrawable
        iconDrawableToBook = appContext.resources.getDrawable(R.drawable.ic_to_book, null) as VectorDrawable
    }

    @Before
    fun getActivity() {
        bookRecord.mode = Constants.VIEW_MODE_ORIGINAL
        booksCollection.updateBook(bookRecord)
        val intent = Intent("com.veve.flowreader.views.PageActivity").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constants.BOOK_ID, testBookId)
            putExtra(Constants.POSITION, 1)
        }
        pageActivity = pageActivityRule.launchActivity(intent)
    }

    @Test
    fun testShowButton() {
        val showButton = pageActivity.findViewById<ImageButton>(R.id.show)
        assertTrue(showButton.visibility == VISIBLE)
        var iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(pageActivity.findViewById<android.view.View>(R.id.bottomBar).visibility == INVISIBLE)
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { showButton.callOnClick() }
        iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook))
    }

    @Test
    fun testSwitchShowButton() {
        val showButton = pageActivity.findViewById<ImageButton>(R.id.show)
        var iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone))

        val page = pageActivity.findViewById<LinearLayout>(R.id.page)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { page.callOnClick() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        InstrumentationRegistry.getInstrumentation().runOnMainSync { showButton.callOnClick() }
        iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook))
        assertTrue(pageActivity.book.mode == Constants.VIEW_MODE_PHONE)
    }

    @Test
    fun testZoom() {
        assertEquals(pageActivity.book.zoomOriginal, 1.0f)
        val zoomInButton = pageActivity.findViewById<ImageButton>(R.id.larger_text)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { zoomInButton.callOnClick() }
        assertEquals(pageActivity.book.zoomOriginal, 1.25f)
        val zoomOutButton = pageActivity.findViewById<ImageButton>(R.id.smaller_text)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { zoomOutButton.callOnClick() }
        assertEquals(pageActivity.book.zoomOriginal, 1.0f)

        for (i in 0 until 16) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { zoomOutButton.callOnClick() }
            Thread.sleep(1000)
            Log.v(javaClass.name, "Zooming out with ratio ${pageActivity.book.zoomOriginal}")
        }

        for (i in 0 until 16) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { zoomInButton.callOnClick() }
            Thread.sleep(1000)
            Log.v(javaClass.name, "Zooming in with ratio ${pageActivity.book.zoomOriginal}")
        }
    }
}
