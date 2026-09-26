package com.veve.flowreader.uitest

import android.content.Intent
import android.view.View.VISIBLE
import android.widget.GridView
import android.widget.ImageButton
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.veve.flowreader.R
import com.veve.flowreader.views.MainActivity
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test

class MainActivityTest : BookTest() {

    var mainActivity: MainActivity? = null

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(
        MainActivity::class.java,
        true,   // initialTouchMode
        false   // launchActivity
    )

    @Test
    fun testActivity() {
        val intent = Intent("com.veve.flowreader.views.MainActivity").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mainActivity = activityRule.launchActivity(intent)
        val grid = mainActivity!!.findViewById<GridView>(R.id.grid)
        assertNotNull(grid)
        assertTrue(grid.visibility == VISIBLE)
        val bookView = grid.getChildAt(0)
        assertNotNull("Grid has no items", bookView)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { bookView.callOnClick() }
    }
}
