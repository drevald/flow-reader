package com.veve.flowreader.uitest

import android.content.Intent
import android.graphics.drawable.VectorDrawable
import android.widget.ImageButton
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.views.PageActivity
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ModePhoneTest : BookTest() {

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
        bookRecord.mode = Constants.VIEW_MODE_PHONE
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
        var iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { showButton.callOnClick() }
        iconDrawable = showButton.drawable as VectorDrawable
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone))
    }
}
