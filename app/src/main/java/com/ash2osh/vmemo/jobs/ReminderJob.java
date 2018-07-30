package com.ash2osh.vmemo.jobs;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.ash2osh.vmemo.R;
import com.ash2osh.vmemo.data.RecodingItem;
import com.ash2osh.vmemo.data.RecodingItemDao;
import com.ash2osh.vmemo.data.RecodingItemDataBase;
import com.ash2osh.vmemo.ui.MainActivity;
import com.ash2osh.vmemo.viewmodel.RecordingViewModel;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

public class ReminderJob extends Job {
    public static final String TAG = "job_reminder_tag";
    private final Handler mHandler;
    private NotificationManager notifManager;

    public ReminderJob() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {

        RecodingItemDataBase db = RecodingItemDataBase.getDatabase(getContext());
        RecodingItemDao recodingItemDao = db.recodingItemDao();

        // run your job here
//            mHandler.post(() ->
//                Toast.makeText(getContext(), "Job Ran", Toast.LENGTH_SHORT).show()
//        );
//
        PersistableBundleCompat extras = params.getExtras();
        int id = extras.getInt("id", 0);
        Log.i("---->", "job Ran:" + String.valueOf(id));
        RecodingItem recodingItem = recodingItemDao.getRecordingItemByIdNormal(id);

        createNotification(recodingItem);
        return Result.SUCCESS;
    }

    public static void scheduleJob(long time, int id) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putInt("id", id);
        new JobRequest.Builder(ReminderJob.TAG)
                .setExact(time)
                .setExtras(extras)
                .build()
                .schedule();
    }

    public void createNotification(RecodingItem recodingItem) {
        final int NOTIFY_ID = 1002;

        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }


        intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        intent.putExtra("id",recodingItem.getId());
        intent.setAction("id"+String.valueOf(recodingItem.getId()));//https://stackoverflow.com/questions/3127957/#3128271
        pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(getContext(), id);

            builder.setContentTitle(getContext().getString(R.string.app_name))  // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(recodingItem.getFilename())  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(recodingItem.getFilename())
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {

            builder = new NotificationCompat.Builder(getContext(), id);

            builder.setContentTitle(getContext().getString(R.string.app_name))                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(recodingItem.getFilename())  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(recodingItem.getFilename())
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
    }
}
