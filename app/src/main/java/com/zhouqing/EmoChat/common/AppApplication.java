package com.zhouqing.EmoChat.common;

import android.app.Activity;
import android.app.Application;

import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppApplication extends Application {
    private static AppApplication mContext;
    private static List<Activity> mActivityList;

    public static HashMap<String, Integer> word2id;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = (AppApplication) getApplicationContext();
        mActivityList = new ArrayList<>();
        generateMap();
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

    private void generateMap() {
        // get vocabulary
        InputStream is = null;
        try {
            is = getApplicationContext().getAssets().open("weibo.vocab.txt");
            int length = 0;
            length = is.available();
            byte[]  buffer = new byte[length];
            is.read(buffer);
            String result = new String(buffer, "utf8");
            String[] results = result.split("\n");
            // get word2id
            word2id = new HashMap<>();
            for (int i = 0;i < results.length;i ++) {
                results[i] = results[i].replace(" ", "").replace("　", "");
                word2id.put(results[i], i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
