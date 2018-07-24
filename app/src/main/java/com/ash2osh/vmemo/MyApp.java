package com.ash2osh.vmemo;

import android.app.Application;

import com.ash2osh.vmemo.jobs.DemoJobCreator;
import com.evernote.android.job.JobManager;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new DemoJobCreator());
    }
}
