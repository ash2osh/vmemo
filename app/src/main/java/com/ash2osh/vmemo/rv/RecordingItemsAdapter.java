package com.ash2osh.vmemo.rv;

import android.support.annotation.Nullable;
import android.widget.ImageButton;

import com.ash2osh.vmemo.R;
import com.ash2osh.vmemo.data.RecodingItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class RecordingItemsAdapter extends BaseQuickAdapter<RecodingItem,BaseViewHolder> {

    public RecordingItemsAdapter(@Nullable List<RecodingItem> data) {
        super(R.layout.item_recording, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, RecodingItem item) {
        holder.setText(R.id.titleItemTV,item.getFilename());
        holder.setText(R.id.dateItemTV,item.getFiledate().toString());//TODO format date
    }
}
