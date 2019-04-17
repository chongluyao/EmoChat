package com.zhouqing.EmoChat.common;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class AppApplication extends Application {
    private static AppApplication mContext;
    private static List<Activity> mActivityList;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = (AppApplication) getApplicationContext();
        mActivityList = new ArrayList<>();
    }

    public static AppApplication getInstance() {
        return mContext;
    }

    public synchronized void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public synchronized void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public Activity findOneActivity(Class activityName) {
        for (Activity activity : mActivityList) {
            if (activity.getClass().getName() == activityName.getName()) {
                return activity;
            }
        }
        return null;
    }

    //离开App
    public void exit() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
        System.exit(0);
    }
}
