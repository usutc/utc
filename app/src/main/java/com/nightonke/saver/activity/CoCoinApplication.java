package com.nightonke.saver.activity;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by 伟平 on 2015/11/2.
 */

public class UtcCoinApplication extends Application {

    public static final int VERSION = 120;

    private static Context mContext;

    public static RefWatcher getRefWatcher(Context context) {
        UtcCoinApplication application = (UtcCoinApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();

        refWatcher = LeakCanary.install(this);
        UtcCoinApplication.mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return UtcCoinApplication.mContext;
    }

    public static String getAndroidId() {
        return Settings.Secure.getString(
                getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
