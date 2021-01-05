package com.example.clingdemo.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.clingdemo.DeviceDisplay;
import com.example.clingdemo.MainActivity;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wilbur on 2018/1/19.
 */

public class ManagerDLNA {
    private AndroidUpnpService mUpnpService = null;
    private MediaServer mediaServer = null;
    private Context mContext;
    private List<DeviceDisplay> listServiceInfo = null;
    private boolean isServerPrepared = false;
    private RegistryListener registryListener = null;
    private ServiceConnection mServiceConnection = null;
    private ControlPoint mControlPoint = null;

    public ManagerDLNA(Context context, MainActivity.BrowseRegistryListener listener) {
        mContext = context;
        listServiceInfo = new ArrayList();
        registryListener = listener;
    }

    public ControlPoint getControlPoint() {
        return mControlPoint;
    }

    public void setUpnpService(AndroidUpnpService upnpService) {
        mUpnpService = upnpService;
    }

    public MediaServer getMediaServer() {
        return this.mediaServer;
    }

    public void setMediaServer(MediaServer mediaServer) {
        this.mediaServer = mediaServer;
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public void initConnection() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                setUpnpService((AndroidUpnpService) service);
                if (mediaServer == null) {
                    try {
                        mediaServer = new MediaServer(mContext);
                        mUpnpService.getRegistry().addDevice(mediaServer.getDevice());
                        addServiceDevices(mediaServer.getDevice());
                        mUpnpService.getRegistry().addListener(registryListener);
                        mControlPoint = mUpnpService.getControlPoint();
                        mUpnpService.getControlPoint().search();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mUpnpService = null;
                setServerPrepared(false);


            }
        };
    }


    public void addServiceDevices(Device device) {
        DeviceDisplay info = new DeviceDisplay(device);
        info.setName(this.getDeviceName(device));
        info.setDevice(device);
        this.listServiceInfo.add(info);
    }

    public String getDeviceName(Device device) {
        String name = "";
        if (device.getDetails() != null && device.getDetails().getFriendlyName() != null) {
            name = device.getDetails().getFriendlyName();
        } else {
            name = device.getDisplayString();
        }

        return name;
    }

    public boolean isServerPrepared() {
        return this.isServerPrepared;
    }

    public void setServerPrepared(boolean isServerPrepared) {
        this.isServerPrepared = isServerPrepared;
    }
}
