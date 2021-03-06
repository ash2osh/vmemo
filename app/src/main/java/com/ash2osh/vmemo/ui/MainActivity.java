package com.ash2osh.vmemo.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ash2osh.vmemo.R;
import com.ash2osh.vmemo.data.RecodingItem;
import com.ash2osh.vmemo.jobs.ReminderJob;
import com.ash2osh.vmemo.rv.RecordingItemsAdapter;
import com.ash2osh.vmemo.viewmodel.RecordingViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity->";
    @BindView(R.id.recordingsRV)
    RecyclerView rv;
    @BindView(R.id.recordFAB)
    FloatingActionButton button;
    private MaterialDialog mPlayerDialog;
    private RecordingViewModel recordingViewModel;
    private List<RecodingItem> recodingItemList;
    private RecordingItemsAdapter adapter;
    private MediaRecorder mRecorder;
    private boolean mIsRecording = false;
    private File mCurrentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recordingViewModel = ViewModelProviders.of(this).get(RecordingViewModel.class);

        // Create the observer which updates the UI.
        final Observer<List<RecodingItem>> RecordingsObserver = (List<RecodingItem> items) ->
        {
            recodingItemList = items;
            if (adapter != null) {
                adapter.setNewData(items);
                adapter.notifyDataSetChanged();
            }


        };
        recordingViewModel.getListItems().observe(this, RecordingsObserver);

        setupRecycler();


        CheckExtraId(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {//https://stackoverflow.com/questions/18037991/
        super.onNewIntent(intent);
        CheckExtraId(intent);

    }

    private void CheckExtraId(Intent intent) {
        if (intent.hasExtra("id")) {
            int id = intent.getIntExtra("id", 0);
            LiveData<RecodingItem> recodingItemById = recordingViewModel.getRecodingItemById(id);
            Observer<RecodingItem> itemObserver = recodingItem ->
                    MainActivityPermissionsDispatcher.PlayRecordingWithPermissionCheck(this, recodingItem);
            recodingItemById.observe(this, itemObserver);
        }
    }

    private void setupRecycler() {
        adapter = new RecordingItemsAdapter(recodingItemList);
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
        SetUpBottomSheetClickers();

        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void SetUpBottomSheetClickers() {
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            if (mIsRecording) {
                Toast.makeText(this, "Recording in Progress", Toast.LENGTH_SHORT).show();
                return;
            }
            RecodingItem item = (RecodingItem) adapter1.getItem(position);


            BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = this.getLayoutInflater().inflate(R.layout.bottom_sheet, null);

            if (item != null) {

                //set click listeners
                LinearLayout play = sheetView.findViewById(R.id.bottom_sheet_play);
                LinearLayout edit = sheetView.findViewById(R.id.bottom_sheet_edit);
                LinearLayout delete = sheetView.findViewById(R.id.bottom_sheet_delete);
                LinearLayout remind = sheetView.findViewById(R.id.bottom_sheet_reminder);
                LinearLayout share = sheetView.findViewById(R.id.bottom_sheet_share);
                play.setOnClickListener(v -> {
                    mBottomSheetDialog.dismiss();
                    MainActivityPermissionsDispatcher.PlayRecordingWithPermissionCheck(this, item);
                });
                edit.setOnClickListener(v -> {
                    mBottomSheetDialog.dismiss();
                    EditRecording(item);
                });
                delete.setOnClickListener(v -> {
                    mBottomSheetDialog.dismiss();
                    DeleteRecording(item);
                });
                remind.setOnClickListener(v -> {
                    mBottomSheetDialog.dismiss();
                    RemindRecording(item);
                });
                share.setOnClickListener(v -> {
                    mBottomSheetDialog.dismiss();
                    ShareRecording(item);
                });

                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();
            }//end if (item != null) {

        });

    }


    @OnClick(R.id.recordFAB)
    void recordFABClick() {
        if (mIsRecording) {
            StopRecording();
        } else {
            MainActivityPermissionsDispatcher.StartRecordingWithPermissionCheck(this);

        }

    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    public void StartRecording() {
        button.setImageResource(R.drawable.exo_icon_stop);
        String filename = String.valueOf(System.currentTimeMillis() / 1000);
        recordAudio(filename + ".amr");
    }

    public void recordAudio(String fileName) {
        mIsRecording = true;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        File file;
        if (isExternalStorageWritable()) {
            file = getPublicAlbumStorageDir(fileName);

        } else {
            file = new File(this.getFilesDir(), fileName);
        }
        mRecorder.setOutputFile(file.getAbsolutePath());
        mCurrentFile = file;
        try {
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }


        mRecorder.start();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getPublicAlbumStorageDir(String filename) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory("vmemo").getAbsolutePath()
                + "/" + filename);

        if (!file.getParentFile().mkdirs()) { //create parent directory
            Log.i(TAG, "Directory not created !!?");
        } else {
            Log.i(TAG, "Directory created :)");
            //create .nomedia file
            File nm = new File(Environment.getExternalStoragePublicDirectory("vmemo").getAbsolutePath()
                    + "/.nomedia");
            try {
                nm.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void StopRecording() {
        //stop the recording and change icon and save file and database item
        mIsRecording = false;
        mRecorder.stop();
        mRecorder.release();
        String FileNameWithoutExtension=mCurrentFile.getName().substring(0, mCurrentFile.getName().lastIndexOf('.'));
        recordingViewModel.insertItem(new RecodingItem(FileNameWithoutExtension, mCurrentFile.getAbsolutePath(), new Date()));
        mRecorder = null;
        mCurrentFile = null;
        button.setImageResource(R.drawable.ic_mic_black_24dp);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    public void PlayRecording(RecodingItem item) {
        File itemFile = new File(item.getFileurl());
        //exo player Dialog

        mPlayerDialog = new MaterialDialog.Builder(this)
                .title(item.getFilename())
                .customView(R.layout.exo_player_dialog, false)
                .positiveText(R.string.close)
                .backgroundColor(Color.BLACK)
                .positiveColor(Color.WHITE)
                .build();


        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
// 2. Create the player
        SimpleExoPlayer player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        //set view
        View view = mPlayerDialog.getView();
        PlayerView playerView = view.findViewById(R.id.dialog_player_view);
        playerView.setPlayer(player);
        //always show controls
        playerView.setControllerShowTimeoutMs(999999999);
        playerView.setControllerHideOnTouch(false);

// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "vmemo"));
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(itemFile));
// Prepare the player with the source.
        player.prepare(videoSource);
        player.setVolume(1);
        player.setPlayWhenReady(true);//auto play
        //release player when dismiss dialog
        mPlayerDialog.setOnDismissListener(dialog1 -> player.release());

        mPlayerDialog.show();
    }

    private void EditRecording(RecodingItem item) {
//Show Edit Dialog
        new MaterialDialog.Builder(this)
                .title("Edit Recording Title")
                .content("Please Enter New Title")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("New Title ", item.getFilename(), (dialog, input) -> {
                    item.setFilename(input.toString());
                    recordingViewModel.insertItem(item);

                    dialog.dismiss();

                }).show();
    }

    private void DeleteRecording(RecodingItem item) {
        File f = new File(item.getFileurl());
        recordingViewModel.deleteItem(item);
        f.delete();
    }


    private void ShareRecording(RecodingItem item) {

        ShareCompat.IntentBuilder.from(this)
                .setType("audio/AMR")
                .setChooserTitle("Share Recording")
                .setText(item.getFilename())
                .setStream(Uri.fromFile(new File(item.getFileurl())))
                .startChooser();

    }

    private void RemindRecording(RecodingItem item) {
        showDateTimePicker(item);
    }


    public void showDateTimePicker(RecodingItem item) {
        Calendar mRemindDate;
        final Calendar currentDate = Calendar.getInstance();
        mRemindDate = Calendar.getInstance();
        //TODO this fires two times on kitkat ??
        new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            mRemindDate.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(MainActivity.this, (view1, hourOfDay, minute) -> {
                mRemindDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mRemindDate.set(Calendar.MINUTE, minute);
                Log.v(TAG, "The chosen one " + mRemindDate.getTime());
                CreateReminderJob(item, mRemindDate);
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void CreateReminderJob(RecodingItem item, Calendar mRemindDate) {
        long t = mRemindDate.getTimeInMillis() - System.currentTimeMillis();
        if (t > 0) {
            ReminderJob.scheduleJob(t, item.getId()); //offset from now
            Toast.makeText(this, "Reminder Set", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: Date Must be in the Future", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayerDialog != null) {
            mPlayerDialog.dismiss();
        }
        if (mIsRecording) {
            StopRecording();
        }
    }

    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showRationaleForBoth(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showDeniedForBoth() {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showNeverAskForBoth() {
        Toast.makeText(this, R.string.permission_never_ask, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


}
