package com.ash2osh.vmemo.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.ash2osh.vmemo.data.RecodingItem;
import com.ash2osh.vmemo.data.RecodingItemRepository;

import java.util.List;

public class RecordingViewModel extends AndroidViewModel {
private RecodingItemRepository repository;


    public RecordingViewModel(Application application) {
        super(application);
        this.repository = new RecodingItemRepository(application);
    }


    public LiveData<List<RecodingItem>> getListItems() {
        return repository.getRecordingsList();
    }

    public LiveData<RecodingItem> getRecodingItemById(int id){
        return repository.getRecodingItem(id);
    }

    public void insertItem(RecodingItem item) {
        InsertItemTask task = new InsertItemTask();
        task.execute(item);
    }

    public void deleteItem(RecodingItem item) {
        DeleteItemTask deleteItemTask = new DeleteItemTask();
        deleteItemTask.execute(item);
    }

    private class  DeleteItemTask extends AsyncTask<RecodingItem, Void, Integer> {

        @Override
        protected Integer doInBackground(RecodingItem... item) {
           return repository.deleteRecordingItem(item[0]);

        }
    }

    private class InsertItemTask extends AsyncTask<RecodingItem, Void, Long> {

        @Override
        protected Long doInBackground(RecodingItem... item) {

            return repository.createNewRecording(item[0]);
        }
    }
}
