package com.ash2osh.vmemo.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

class migrations {
    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //nothing is needed here
//            database.execSQL("ALTER TABLE recordings "
//                    +"ADD COLUMN address String");

        }
    };
}
