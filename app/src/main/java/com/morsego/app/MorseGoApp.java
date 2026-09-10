package com.morsego.app;

import android.app.Application;

public class MorseGoApp extends Application {
    private static MorseGoApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MorseGoApp getInstance() {
        return instance;
    }
}
