package com.ash2osh.vmemo.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class RecodingItemRepository {
    private  RecodingItemDao recodingItemDao;



    public RecodingItemRepository(Application application) {
        RecodingItemDataBase db = RecodingItemDataBase.getDatabase(application);
         recodingItemDao = db.recodingItemDao();
    }



    public LiveData<List<RecodingItem>> getRecordingsList(){
        return recodingItemDao.getRecordingItems();
    }

    public LiveData<RecodingItem> getRecodingItem(int id){
        return recodingItemDao.getRecordingItemById(id);
    }

    public Long createNewRecording(RecodingItem item){
        return recodingItemDao.insertRecodingItem(item);
    }

    public int deleteRecordingItem(RecodingItem item){
       return recodingItemDao.deleteRecodingItem(item);
    }
}
