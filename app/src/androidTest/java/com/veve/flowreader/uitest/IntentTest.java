package com.veve.flowreader.uitest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;

import com.veve.flowreader.views.MainActivity;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class IntentTest extends BookTest {

    @Test
    public void testIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("content://com.mi.android.globalFileexplorer.myprovider/external_files/Download/firstscope_eq.pdf"));
//        intent.setType("application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

    }


}
