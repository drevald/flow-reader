package com.veve.flowreader.espresso;

import android.content.Intent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.uitest.BookTest;
import com.veve.flowreader.views.MainActivity;
import com.veve.flowreader.views.PageActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ReportTest  extends BookTest {

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
    public void testSendReport() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        //Espresso.onView(ViewMatchers.withId(R.id.page_unreadable)).perform(click());
        onView(ViewMatchers.withText("Page unreadable")).perform(click());
        onIdle();
        onView(withId(R.id.send)).perform(click());
        onIdle();
        assert(true);
    }


}
