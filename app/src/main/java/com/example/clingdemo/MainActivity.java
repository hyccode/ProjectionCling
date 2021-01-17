package com.example.clingdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clingdemo.browser.BrowserUpnpService;
import com.example.clingdemo.manager.ManagerDLNA;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.VideoItem;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;
import org.seamless.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    StandardGSYVideoPlayer videoPlayer;

    OrientationUtils orientationUtils;
    private TextView tv_message;
    private ClingDialog clingDialog;
    private ManagerDLNA managerDLNA;

    private String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
    private Device device;
    private Switch switch_mute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    private void init() {
        videoPlayer = (StandardGSYVideoPlayer) findViewById(R.id.video_player);
        tv_message = (TextView) findViewById(R.id.tv_message);
        switch_mute = (Switch) findViewById(R.id.switch_mute);


        initPlay();
    }

    private void initCling() {
//        managerDLNA = new ManagerDLNA(this,new BrowseRegistryListener());
//        managerDLNA.initConnection();

        getApplicationContext().bindService(
//            new Intent(this, AndroidUpnpServiceImpl.class),
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );


        // 静音开关
        switch_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Service avtService = device.findService(RENDERING_CONTROL_SERVICE);
                upnpService.getControlPoint().execute(new SetMute(avtService,isChecked) {
                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        Log.d("hhhh","failure");
                    }
                });
            }
        });

    }

    private void initPlay() {

        videoPlayer.setUp(source1, true, "测试视频");

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.ic_launcher);
        videoPlayer.setThumbImageView(imageView);
        //增加title
        videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        //设置返回键
        videoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils(this, videoPlayer);
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });
        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);
        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        videoPlayer.startPlayLogic();
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();

        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        videoPlayer.setVideoAllCallBack(null);
        super.onBackPressed();
    }

    private AndroidUpnpService upnpService;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Clear the list


            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    public class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void deviceAdded(Registry registry, Device device) {
            super.deviceAdded(registry, device);
            deviceAdded(device);
        }

        @Override
        public void deviceRemoved(Registry registry, Device device) {
            super.deviceRemoved(registry, device);
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);
                    clingDialog.add(d);
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    clingDialog.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    public static final ServiceType AV_TRANSPORT_SERVICE = new UDAServiceType("AVTransport");
    /** 控制服务 */
    public static final ServiceType RENDERING_CONTROL_SERVICE = new UDAServiceType("RenderingControl");
    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    public void touPing(View view) {
        clingDialog = ClingDialog.newInstance(new ChoiceDeviceListener() {

            @Override
            public void choiceDevice(Device device) {
                MainActivity.this.device = device;
                Service avtService = device.findService(AV_TRANSPORT_SERVICE);
                ControlPoint mControlPoint = upnpService.getControlPoint();
                mControlPoint.execute(new SetAVTransportURI(avtService, source1) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
//                        curState = PLAY;
                        if (clingDialog!=null){
                            clingDialog.dismiss();
                        }
                    }

                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//                        Log.e(TAG,"play error:"+defaultMsg);
//                        curState = ERROR;
                    }
                });

            }

            @Override
            public void startSearch() {
                initCling();
            }
        });
        clingDialog
                .show(getSupportFragmentManager());
    }


    public void stop(View view) {
        Service avtService = device.findService(AV_TRANSPORT_SERVICE);
        this.upnpService.getControlPoint().execute(new Stop(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

            }
        });
    }

    public void pause(View view) {
        Service avtService = device.findService(AV_TRANSPORT_SERVICE);
        this.upnpService.getControlPoint().execute(new Pause(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {

            }
        });

//        Service re = device.findService(RENDERING_CONTROL_SERVICE);
////        Service dm = device.findService(DMR_DEVICE_TYPE);
//
//        Action[] actions = avtService.getActions();

    }


    public interface ChoiceDeviceListener {
        void choiceDevice(Device device);
        void startSearch();
    }

}