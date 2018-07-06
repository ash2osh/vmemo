package com.ash2osh.vmemo.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;


/**
 * Room will use the Class name a sa default table name.
 *
 * This can be chagned by setting @Entity(tableName = "tableName")
 */

@Entity(tableName = "recordings")
public class RecodingItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String filename;
    private String fileurl;

    private Date filedate;


    public RecodingItem( String filename, String fileurl, Date filedate) {
        this.filename = filename;
        this.fileurl = fileurl;
        this.filedate = filedate;
    }

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    public Date getFiledate() {
        return filedate;
    }

    public void setFiledate(Date filedate) {
        this.filedate = filedate;
    }
}
