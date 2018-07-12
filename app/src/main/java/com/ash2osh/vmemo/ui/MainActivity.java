package com.ash2osh.vmemo.ui;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ash2osh.vmemo.R;
import com.ash2osh.vmemo.data.RecodingItem;
import com.ash2osh.vmemo.rv.RecordingItemsAdapter;
import com.ash2osh.vmemo.viewmodel.RecordingViewModel;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
            RecodingItem item = (RecodingItem) adapter1.getItem(position);

            BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = this.getLayoutInflater().inflate(R.layout.bottom_sheet, null);

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

        });
    }


    @OnClick(R.id.recordFAB)
    void recordFABClick(View v) {

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
        recordAudio(filename + ".3gp");
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
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicAlbumStorageDir(String filename) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory("vmemo").getAbsolutePath()
                + "/" + filename);

        if (!file.getParentFile().mkdirs()) { //create parent directory
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    private void StopRecording() {
        //stop the recording and change icon and save file and database item
        mIsRecording = false;
        mRecorder.stop();
        mRecorder.release();
        recordingViewModel.insertItem(new RecodingItem(mCurrentFile.getName(), mCurrentFile.getAbsolutePath(), new Date()));
        mRecorder = null;
        mCurrentFile = null;
        button.setImageResource(R.drawable.ic_mic_black_24dp);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    public void PlayRecording(RecodingItem item) {
        //TODO show exo player Dialog

    }

    private void EditRecording(RecodingItem item) {

    }
    private void DeleteRecording(RecodingItem item) {

    }


    private void ShareRecording(RecodingItem item) {
    }

    private void RemindRecording(RecodingItem item) {
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        Toast.makeText(this, R.string.permission_neverask, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


}
