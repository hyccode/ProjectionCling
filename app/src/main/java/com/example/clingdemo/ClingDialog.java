package com.example.clingdemo;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.clingdemo.dialogFragment.BaseDialog;
import com.example.clingdemo.dialogFragment.ViewHolder;

public class ClingDialog extends BaseDialog {


    private DeviceAdapter deviceAdapter;
    private MainActivity.ChoiceDeviceListener choiceDeviceListener;

    public static ClingDialog newInstance(MainActivity.ChoiceDeviceListener choiceDeviceListener) {
        ClingDialog dialog = new ClingDialog();
        dialog.setListener(choiceDeviceListener);
        return dialog;
    }

    private void setListener(MainActivity.ChoiceDeviceListener choiceDeviceListener) {
        this.choiceDeviceListener = choiceDeviceListener;
    }


    @Override
    public int setUpLayoutId() {
        return R.layout.dialog_for_screen;
    }

    @Override
    public void convertView(ViewHolder holder, BaseDialog dialog) {
        deviceAdapter = new DeviceAdapter(R.layout.item_device);

        RecyclerView recyclerView = holder.getView(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(deviceAdapter);

        deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                choiceDeviceListener.choiceDevice(deviceAdapter.getItem(position).getDevice());
            }
        });
        if (choiceDeviceListener != null) {
            choiceDeviceListener.startSearch();
        }
    }

    public void remove(DeviceDisplay deviceDisplay) {
        if (deviceAdapter == null) {
            return;
        }
        deviceAdapter.remove(deviceDisplay);
    }


    public void add(DeviceDisplay d) {
        if (deviceAdapter == null) {
            return;
        }
        deviceAdapter.addData(d);
    }
}
