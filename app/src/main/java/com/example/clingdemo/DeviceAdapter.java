package com.example.clingdemo;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;

public class DeviceAdapter extends BaseQuickAdapter<DeviceDisplay, BaseViewHolder> {
    public DeviceAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DeviceDisplay deviceDisplay) {
        String name = "";
        if (deviceDisplay.getDevice().getDetails() != null && deviceDisplay.getDevice().getDetails().getFriendlyName() != null) {
            name = deviceDisplay.getDevice().getDetails().getFriendlyName();
        } else {
            name = deviceDisplay.getDevice().getDisplayString();
        }

        baseViewHolder.setText(R.id.tv_name, name);
    }
}
