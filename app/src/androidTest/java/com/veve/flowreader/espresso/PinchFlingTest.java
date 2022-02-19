package com.veve.flowreader.espresso;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.uitest.BookTest;
import com.veve.flowreader.views.PageActivity;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
public class PinchFlingTest extends BookTest {

    @Rule
    public ActivityTestRule<PageActivity> activityRule
            = new ActivityTestRule<>(PageActivity.class, false, false);

    @Before
    public void getActivity() {
        bookRecord.setMode(Constants.VIEW_MODE_PHONE);
        booksCollection.updateBook(bookRecord);
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, testBookId);
        intent.putExtra(Constants.POSITION, 1);
        activityRule.launchActivity(intent);
    }

    @Test
    public void testPinchOut() {
        onView(withId(R.id.scroll))
                .perform(doPinchOut())
                .check(matches(isDisplayed()));
        Log.d(getClass().getName(),
                String.format("Final zoom %f", Math.abs(activityRule.getActivity().zoomFactor)));
        Assert.assertEquals(0, activityRule.getActivity().currentPage, 0.0);
        Assert.assertEquals(activityRule.getActivity().zoomFactor, 3, 1.0);
    }

    @Test
    public void testPinchIn() {
        onView(withId(R.id.scroll))
                .perform(doPinchIn())
                .check(matches(isDisplayed()));
        Log.d(getClass().getName(),
                String.format("Final zoom %f", Math.abs(activityRule.getActivity().zoomFactor)));
        Assert.assertEquals(0, activityRule.getActivity().currentPage, 0.0);
        Assert.assertEquals(1/activityRule.getActivity().zoomFactor, 3, 1.0);
    }

    @Test
    public void testFling() {
        onView(withId(android.R.id.content))
                .perform(swipeLeft())
                .check(matches(isDisplayed()));
        Assert.assertEquals(1, activityRule.getActivity().currentPage, 0.0);
        onView(withId(android.R.id.content))
                .perform(swipeRight())
                .check(matches(isDisplayed()));
        Assert.assertEquals(0, activityRule.getActivity().currentPage, 0.0);
        Assert.assertEquals(1, activityRule.getActivity().zoomFactor, 0.0);
    }

    private ViewAction doPinchOut() {
        return new ViewAction() {

            private static final int VIEW_DISPLAY_PERCENTAGE = 90;

            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {

                try {

                    List<MotionEvent> motionEvents = new ArrayList<>();

                    int stepsNum = 50;

                    int xStep = (view.getWidth()/(4 * stepsNum));
                    int yStep = (view.getHeight()/(4 * stepsNum));

                    int x0start = (3 * view.getWidth()/8);
                    int y0start = (3 * view.getHeight()/8);

                    int x1start = (5 * view.getWidth()/8);
                    int y1start = (5 * view.getHeight()/8);

                    int x0end = (view.getWidth()/8);
                    int y0end = (view.getHeight()/8);

                    int x1end = (7 *view.getWidth()/8);
                    int y1end = (7 * view.getHeight()/8);

                    long start = SystemClock.uptimeMillis();
                    long tick = 10;
                    long blink = 10;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0start, y0start,
                            ACTION_DOWN));
                    start += tick;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0start, y0start,
                            x1start, y1start,
                            ACTION_POINTER_DOWN | 0x0100));
                    start += tick;

                    for (int i = 1; i < stepsNum; i++) {
                        motionEvents.add(getEvent(
                                start, start + blink,
                                x0start - i * xStep, y0start - i * yStep,
                                x1start + i * xStep, y1start + i * yStep,
                                ACTION_MOVE));
                        start += tick;
                    }

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0end, y0end,
                            x1end, y1end,
                            ACTION_POINTER_UP | 0x0100));
                    start += tick;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0end, y0end,
                            ACTION_UP));

                    Assert.assertTrue(uiController.injectMotionEventSequence(motionEvents));

                } catch (Exception e) {
                    Log.e(getClass().getName(), "Failed to inject event", e);
                }
            }
        };
    }

    private ViewAction doPinchIn() {
        return new ViewAction() {

            private static final int VIEW_DISPLAY_PERCENTAGE = 90;

            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(VIEW_DISPLAY_PERCENTAGE);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {

                try {

                    List<MotionEvent> motionEvents = new ArrayList<>();

                    int stepsNum = 50;

                    int xStep = (view.getWidth()/(4 * stepsNum));
                    int yStep = (view.getHeight()/(4 * stepsNum));

                    int x0end = (3 * view.getWidth()/8);
                    int y0end = (3 * view.getHeight()/8);

                    int x1end = (5 * view.getWidth()/8);
                    int y1end = (5 * view.getHeight()/8);

                    int x0start = (view.getWidth()/8);
                    int y0start = (view.getHeight()/8);

                    int x1start = (7 *view.getWidth()/8);
                    int y1start = (7 * view.getHeight()/8);

                    long start = SystemClock.uptimeMillis();
                    long tick = 10;
                    long blink = 10;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0start, y0start,
                            ACTION_DOWN));
                    start += tick;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0start, y0start,
                            x1start, y1start,
                            ACTION_POINTER_DOWN | 0x0100));
                    start += tick;

                    for (int i = 1; i < stepsNum; i++) {
                        motionEvents.add(getEvent(
                                start, start + blink,
                                x0start + i * xStep, y0start + i * yStep,
                                x1start - i * xStep, y1start - i * yStep,
                                ACTION_MOVE));
                        start += tick;
                    }

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0end, y0end,
                            x1end, y1end,
                            ACTION_POINTER_UP | 0x0100));
                    start += tick;

                    motionEvents.add(getEvent(
                            start, start + blink,
                            x0end, y0end,
                            ACTION_UP));

                    Assert.assertTrue(uiController.injectMotionEventSequence(motionEvents));

                } catch (Exception e) {
                    Log.e(getClass().getName(), "Failed to inject event", e);
                }
            }
        };
    }

    private MotionEvent getEvent(long downTime, long eventTime, int x, int y, int metaState) {

        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        pointerProperties[0] =  new MotionEvent.PointerProperties();
        pointerProperties[0].id = 0;
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords[] pointerCoordinates = new MotionEvent.PointerCoords[1];
        pointerCoordinates[0] =  new MotionEvent.PointerCoords();
        pointerCoordinates[0].x = x;
        pointerCoordinates[0].y = y;
        pointerCoordinates[0].pressure = 1;
        pointerCoordinates[0].size = 10;

        return MotionEvent.obtain(downTime, eventTime,
                metaState, 1, pointerProperties, pointerCoordinates, 0,
                0, 0, 0, 0,0, 0, 0);

    }

    private MotionEvent getEvent(long downTime, long eventTime, int x0, int y0, int x1, int y1, int metaState) {

        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[2];
        pointerProperties[0] =  new MotionEvent.PointerProperties();
        pointerProperties[0].id = 0;
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;
        pointerProperties[1] =  new MotionEvent.PointerProperties();
        pointerProperties[1].id = 1;
        pointerProperties[1].toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords[] pointerCoordinates = new MotionEvent.PointerCoords[2];
        pointerCoordinates[0] =  new MotionEvent.PointerCoords();
        pointerCoordinates[0].x = x0;
        pointerCoordinates[0].y = y0;
        pointerCoordinates[0].pressure = 1;
        pointerCoordinates[0].size = 10;
        pointerCoordinates[1] =  new MotionEvent.PointerCoords();
        pointerCoordinates[1].x = x1;
        pointerCoordinates[1].y = y1;
        pointerCoordinates[1].pressure = 1;
        pointerCoordinates[1].size = 10;

        return MotionEvent.obtain(downTime, eventTime,
                metaState, 2, pointerProperties, pointerCoordinates, 0,
                0, 0, 0, 0,0, 0, 0);

    }


}
