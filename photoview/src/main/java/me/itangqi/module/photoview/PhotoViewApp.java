package me.itangqi.module.photoview;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class PhotoViewApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
    }
}
