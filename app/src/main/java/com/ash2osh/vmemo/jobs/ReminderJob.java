package com.ash2osh.vmemo.jobs;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

public class ReminderJob extends Job {
    public static final String TAG = "job_reminder_tag";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        // run your job here
        //TODO show notifacation
        Toast.makeText(getContext(), "Job Ran", Toast.LENGTH_SHORT).show();
        Log.i("---->","job Raan");
        return Result.SUCCESS;
    }

    public static void scheduleJob(long time) {
        new JobRequest.Builder(ReminderJob.TAG)
                .setExact(time)
                .build()
                .schedule();
    }
}
