package com.ash2osh.vmemo.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {RecodingItem.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class RecodingItemDataBase  extends RoomDatabase {
    public abstract RecodingItemDao recodingItemDao();


    private static RecodingItemDataBase INSTANCE;


   public static RecodingItemDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RecodingItemDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RecodingItemDataBase.class, "RecodingItemDataBase")
                            .addMigrations(migrations.MIGRATION_2_3)
                            .build();

                }
            }
        }
        return INSTANCE;
    }

}
