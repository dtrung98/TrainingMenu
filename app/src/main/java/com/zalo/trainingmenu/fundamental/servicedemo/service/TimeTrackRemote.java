package com.zalo.trainingmenu.fundamental.servicedemo.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.zalo.trainingmenu.model.ServiceToken;

import java.util.Map;
import java.util.WeakHashMap;

public class TimeTrackRemote {
    private static final String TAG = "TimeTrackRemote";

    private static TimeTrackService mService;
    private static final WeakHashMap<Context, RemoteServiceBinder> mConnectionMap = new WeakHashMap<>();

    public static ServiceToken bindServiceAndStartIfNotRunning(Context context, ServiceConnection callback) {
        Activity realActivity = ((Activity)context).getParent();
        if(realActivity ==null) {
            realActivity  = (Activity)context;
        }

        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper,TimeTrackService.class));

        final RemoteServiceBinder binder = new RemoteServiceBinder(callback);

        if(contextWrapper.bindService(new Intent().setClass(contextWrapper,TimeTrackService.class),binder,Context.BIND_AUTO_CREATE))
        {
            mConnectionMap.put(contextWrapper, binder);
            Log.d(TAG, "bindServiceAndStartIfNotRunning: put mService to connection map");
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    public static Boolean isServiceRunning(Context context) {
        Activity realActivity = ((Activity)context).getParent();
        if(realActivity ==null) {
            realActivity  = (Activity)context;
        }

        ActivityManager manager = (ActivityManager) realActivity.getSystemService(Context.ACTIVITY_SERVICE);
        if(manager==null) return null;
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            String serviceClassName = service.service.getClassName();
            if((TimeTrackService.PACKAGE_NAME+'.'+TimeTrackService.TAG).equals(serviceClassName)) return true;
        }
        return false;
    }

    public static void unBind(ServiceToken serviceToken) {
        if(serviceToken == null) return;

        final ContextWrapper contextWrapper = serviceToken.mWrappedContext;

        RemoteServiceBinder binder = mConnectionMap.remove(contextWrapper);

        if(binder!=null) {
            Log.d(TAG, "unBinding");
            contextWrapper.unbindService(binder);
        } else Log.d(TAG, "unBind: can't find mService to unbind");
        
        if(mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static void unBindAll() {
        for (Map.Entry<Context, RemoteServiceBinder> entry: mConnectionMap.entrySet()){
            Context context = entry.getKey();
            RemoteServiceBinder rsb = entry.getValue();
            context.unbindService(rsb);
        }

        mConnectionMap.clear();
        mService = null;
    }

    public static void stopService() {
        for (Map.Entry<Context, RemoteServiceBinder> entry: mConnectionMap.entrySet()){
            Context context = entry.getKey();
            RemoteServiceBinder rsb = entry.getValue();
            context.unbindService(rsb);
        }

        mConnectionMap.clear();

        if(mService!=null)
            mService.stopForegroundThenStopSelf();
        else Log.d(TAG, "stopService: oop, mService instance is null");
        mService = null;
    }


    public static class RemoteServiceBinder implements ServiceConnection {

        private final ServiceConnection mCallback;

        public RemoteServiceBinder(final ServiceConnection callback){
            mCallback = callback;
        }


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "RemoteServiceBinder: onServiceConnected: ");
            
            TimeTrackService.TimeTrackBinder binder = (TimeTrackService.TimeTrackBinder) iBinder;
            mService = binder.getService();

            if(mCallback!=null) {
                mCallback.onServiceConnected(componentName,iBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "RemoteServiceBinder: onServiceDisconnected: ");

            if(mCallback != null) {
                mCallback.onServiceDisconnected(componentName);
            }
            mService = null;
        }
    }
}
