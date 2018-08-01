package com.ash2osh.vmemo.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * A DAO, or Data Access Object, is a layer of abstraction (interface) between Java Objects and
 * SQL Statements.
 *
 * For a given entity, it defines how we may manage said entity within the Database.
 */
@Dao
public interface RecodingItemDao {

    /**
     * Get entity by itemId. For this App, we will pass in an ID when the detail Activity starts;
     * therefore we need not use LiveData as the Data will not change during the Activity's
     * Lifecycle.
     * @param id A Unique identifier for a given record within the Database.
     * @return
     */
    @Query("SELECT * FROM recordings WHERE id = :id")
    LiveData<RecodingItem> getRecordingItemById(int id);

    @Query("SELECT * FROM recordings WHERE id = :id")
    RecodingItem getRecordingItemByIdNormal(int id);

    /**
     * Get all entities of type ListItem
     * @return
     */
    @Query("SELECT * FROM recordings order by filedate desc")
    LiveData<List<RecodingItem>> getRecordingItems();


    /**
     * Insert a new ListItem
     * @param item
     */
    @Insert(onConflict = REPLACE)
    Long insertRecodingItem(RecodingItem item);

    /**
     * Delete a given ListItem
     * @param item
     */
    @Delete
    int deleteRecodingItem(RecodingItem item);


}
