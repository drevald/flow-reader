package com.veve.flowreader.espresso

import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.Espresso.onView
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.veve.flowreader.Constants
import com.veve.flowreader.R
import com.veve.flowreader.uitest.BookTest
import com.veve.flowreader.views.PageActivity
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class PinchFlingTest : BookTest() {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(PageActivity::class.java, false, false)

    @Before
    fun getActivity() {
        bookRecord.mode = Constants.VIEW_MODE_PHONE
        booksCollection.updateBook(bookRecord)
        val intent = Intent("com.veve.flowreader.views.PageActivity").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constants.BOOK_ID, testBookId)
            putExtra(Constants.POSITION, 1)
        }
        activityRule.launchActivity(intent)
    }

    @Test
    fun testPinchOut() {
        onView(withId(R.id.scroll))
            .perform(doPinchOut())
            .check(matches(isDisplayed()))
        Log.d(javaClass.name, "Final zoom ${Math.abs(activityRule.activity.zoomFactor)}")
        Assert.assertEquals(0.0, activityRule.activity.currentPage.toDouble(), 0.0)
        Assert.assertEquals(activityRule.activity.zoomFactor.toDouble(), 3.0, 1.0)
    }

    @Test
    fun testPinchIn() {
        onView(withId(R.id.scroll))
            .perform(doPinchIn())
            .check(matches(isDisplayed()))
        Log.d(javaClass.name, "Final zoom ${Math.abs(activityRule.activity.zoomFactor)}")
        Assert.assertEquals(0.0, activityRule.activity.currentPage.toDouble(), 0.0)
        Assert.assertEquals((1 / activityRule.activity.zoomFactor).toDouble(), 3.0, 1.0)
    }

    @Test
    fun testFling() {
        onView(withId(android.R.id.content))
            .perform(swipeLeft())
            .check(matches(isDisplayed()))
        Assert.assertEquals(1.0, activityRule.activity.currentPage.toDouble(), 0.0)
        onView(withId(android.R.id.content))
            .perform(swipeRight())
            .check(matches(isDisplayed()))
        Assert.assertEquals(0.0, activityRule.activity.currentPage.toDouble(), 0.0)
        Assert.assertEquals(1.0, activityRule.activity.zoomFactor.toDouble(), 0.0)
    }

    private fun doPinchOut(): ViewAction = object : ViewAction {
        private val VIEW_DISPLAY_PERCENTAGE = 90

        override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE)
        override fun getDescription(): String? = null

        override fun perform(uiController: UiController, view: View) {
            try {
                val motionEvents = mutableListOf<MotionEvent>()
                val stepsNum = 50
                val xStep = view.width / (4 * stepsNum)
                val yStep = view.height / (4 * stepsNum)
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]
                val x0start = viewX + 3 * view.width / 8
                val y0start = viewY + 3 * view.height / 8
                val x1start = viewX + 5 * view.width / 8
                val y1start = viewY + 5 * view.height / 8
                val x0end = viewX + view.width / 8
                val y0end = viewY + view.height / 8
                val x1end = viewX + 7 * view.width / 8
                val y1end = viewY + 7 * view.height / 8
                var start = SystemClock.uptimeMillis()
                val tick = 10L
                val blink = 10L
                motionEvents.add(getEvent(start, start + blink, x0start, y0start, MotionEvent.ACTION_DOWN))
                start += tick
                motionEvents.add(getEvent(start, start + blink, x0start, y0start, x1start, y1start, MotionEvent.ACTION_POINTER_DOWN or 0x0100))
                start += tick
                for (i in 1 until stepsNum) {
                    motionEvents.add(getEvent(start, start + blink, x0start - i * xStep, y0start - i * yStep, x1start + i * xStep, y1start + i * yStep, MotionEvent.ACTION_MOVE))
                    start += tick
                }
                motionEvents.add(getEvent(start, start + blink, x0end, y0end, x1end, y1end, MotionEvent.ACTION_POINTER_UP or 0x0100))
                start += tick
                motionEvents.add(getEvent(start, start + blink, x0end, y0end, MotionEvent.ACTION_UP))
                Assert.assertTrue(uiController.injectMotionEventSequence(motionEvents))
            } catch (e: Exception) {
                Log.e(javaClass.name, "Failed to inject event", e)
            }
        }
    }

    private fun doPinchIn(): ViewAction = object : ViewAction {
        private val VIEW_DISPLAY_PERCENTAGE = 90

        override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE)
        override fun getDescription(): String? = null

        override fun perform(uiController: UiController, view: View) {
            try {
                val motionEvents = mutableListOf<MotionEvent>()
                val stepsNum = 50
                val xStep = view.width / (4 * stepsNum)
                val yStep = view.height / (4 * stepsNum)
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]
                val x0end = viewX + 3 * view.width / 8
                val y0end = viewY + 3 * view.height / 8
                val x1end = viewX + 5 * view.width / 8
                val y1end = viewY + 5 * view.height / 8
                val x0start = viewX + view.width / 8
                val y0start = viewY + view.height / 8
                val x1start = viewX + 7 * view.width / 8
                val y1start = viewY + 7 * view.height / 8
                var start = SystemClock.uptimeMillis()
                val tick = 10L
                val blink = 10L
                motionEvents.add(getEvent(start, start + blink, x0start, y0start, MotionEvent.ACTION_DOWN))
                start += tick
                motionEvents.add(getEvent(start, start + blink, x0start, y0start, x1start, y1start, MotionEvent.ACTION_POINTER_DOWN or 0x0100))
                start += tick
                for (i in 1 until stepsNum) {
                    motionEvents.add(getEvent(start, start + blink, x0start + i * xStep, y0start + i * yStep, x1start - i * xStep, y1start - i * yStep, MotionEvent.ACTION_MOVE))
                    start += tick
                }
                motionEvents.add(getEvent(start, start + blink, x0end, y0end, x1end, y1end, MotionEvent.ACTION_POINTER_UP or 0x0100))
                start += tick
                motionEvents.add(getEvent(start, start + blink, x0end, y0end, MotionEvent.ACTION_UP))
                Assert.assertTrue(uiController.injectMotionEventSequence(motionEvents))
            } catch (e: Exception) {
                Log.e(javaClass.name, "Failed to inject event", e)
            }
        }
    }

    private fun getEvent(downTime: Long, eventTime: Long, x: Int, y: Int, metaState: Int): MotionEvent {
        val pointerProperties = arrayOf(MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_FINGER })
        val pointerCoordinates = arrayOf(MotionEvent.PointerCoords().apply { this.x = x.toFloat(); this.y = y.toFloat(); pressure = 1f; size = 10f })
        return MotionEvent.obtain(downTime, eventTime, metaState, 1, pointerProperties, pointerCoordinates, 0, 0, 0f, 0f, 0, 0, 0, 0)
    }

    private fun getEvent(downTime: Long, eventTime: Long, x0: Int, y0: Int, x1: Int, y1: Int, metaState: Int): MotionEvent {
        val pointerProperties = arrayOf(
            MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_FINGER },
            MotionEvent.PointerProperties().apply { id = 1; toolType = MotionEvent.TOOL_TYPE_FINGER }
        )
        val pointerCoordinates = arrayOf(
            MotionEvent.PointerCoords().apply { this.x = x0.toFloat(); this.y = y0.toFloat(); pressure = 1f; size = 10f },
            MotionEvent.PointerCoords().apply { this.x = x1.toFloat(); this.y = y1.toFloat(); pressure = 1f; size = 10f }
        )
        return MotionEvent.obtain(downTime, eventTime, metaState, 2, pointerProperties, pointerCoordinates, 0, 0, 0f, 0f, 0, 0, 0, 0)
    }
}
