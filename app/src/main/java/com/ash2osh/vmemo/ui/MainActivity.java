package com.ash2osh.vmemo.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ash2osh.vmemo.R;
import com.ash2osh.vmemo.data.RecodingItem;
import com.ash2osh.vmemo.viewmodel.RecordingViewModel;

import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private RecordingViewModel recordingViewModel;

    @BindView(R.id.resultTV)
    TextView mResultTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recordingViewModel = ViewModelProviders.of(this).get(RecordingViewModel.class);


        // Create the observer which updates the UI.
        final Observer<List<RecodingItem>> RecodingsObserver = items -> {
            String txt = "";
            for (RecodingItem item :
                    items) {
                txt += item.getId() + "  " + item.getFilename() +" "+item.getFiledate()+ "\n";
            }

            mResultTV.setText(txt);
        };
        recordingViewModel.getListItems().observe(this, RecodingsObserver);

    }

    @OnClick(R.id.insertBTN)
    void insertBtn() {
        recordingViewModel.insertItem(new RecodingItem(new Random().nextInt(), "fokak meny", "asset", new Date()));
    }


}
