package com.zalo.trainingmenu.downloader.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;


import androidx.core.content.FileProvider;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.model.DownloadItem;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.model.ServiceToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import es.dmoral.toasty.Toasty;

public class TaskServiceRemote {
    private static final String TAG = "TaskServiceRemote";

    private static TaskService mService;
    private static final WeakHashMap<Context, RemoteServiceBinder> mConnectionMap = new WeakHashMap<>();

    public static void appendTask(DownloadItem item) {
       if(mService!=null) {
           mService.addNewTask(item);
       }
    }

    public static ArrayList<TaskInfo> getSessionTaskList() {
        if(mService!=null)
            return mService.getSessionTaskList();
        return null;
    }

    public static TaskInfo getTaskInfoWithTaskId(int id) {
     if(mService!=null)
         return mService.getTaskInfoWithId(id);
        return null;
    }

    public static void pauseTaskWithTaskId(int id) {
        if(mService!=null)
            mService.pauseTaskWithTaskId(id);
    }

    public static void cancelTaskWithTaskId(int id) {
        if(mService!=null)
            mService.cancelTaskWithTaskId(id);
    }


    public static ServiceToken bindServiceAndStartIfNotRunning(Context context, ServiceConnection callback) {
        Activity realActivity = ((Activity)context).getParent();
        if(realActivity ==null) {
            realActivity  = (Activity)context;
        }

        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, TaskService.class));

        final RemoteServiceBinder binder = new RemoteServiceBinder(callback);

        if(contextWrapper.bindService(new Intent().setClass(contextWrapper, TaskService.class),binder,Context.BIND_AUTO_CREATE))
        {
            mConnectionMap.put(contextWrapper, binder);
            Log.d(TAG, "bindServiceAndStartIfNotRunning: put mService to connection map");
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

  /*  public static Boolean isServiceRunning(Context context) {
        Activity realActivity = ((Activity)context).getParent();
        if(realActivity ==null) {
            realActivity  = (Activity)context;
        }

        ActivityManager manager = (ActivityManager) realActivity.getSystemService(Context.ACTIVITY_SERVICE);
        if(manager==null) return null;
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            String serviceClassName = service.service.getClassName();
            if((TaskService.PACKAGE_NAME+'.'+ TaskService.TAG).equals(serviceClassName)) return true;
        }
        return false;
    }*/

    private static boolean isServiceRunning(Context c) {
        ActivityManager am = ((ActivityManager) c
                .getSystemService(Context.ACTIVITY_SERVICE));
        if(am==null) return false;
        List<ActivityManager.RunningServiceInfo> processes = am
                .getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : processes) {
            String pname = info.service.getShortClassName();

            if (pname.contains(TaskService.class.getSimpleName()))
                return true;
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
            mService.stopIfModeBackground();
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

    public static void resumeTaskWithTaskId(int id) {
        if(mService!=null) mService.resumeTaskWithTaskId(id);
    }
    public static void restartTaskWithTaskId(int id) {
        if(mService!=null) mService.restartTaskWithTaskId(id);
    }

    public static void clearTask(int id) {
        if(mService!=null) mService.clearTask(id);
    }
    public static void openFinishedTaskInfo(Context context, TaskInfo info) {
        if(info.getState()== BaseTask.SUCCESS) {
            try {
                File filePath = new File(info.getDirectory());
                File fileToWrite = new File(filePath, info.getFileTitle());
                final Uri data = FileProvider.getUriForFile(context, "com.zalo.trainingmenu.provider", fileToWrite);
                context.grantUriPermission(context.getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String fileExtension = info.getFileTitle().substring(info.getFileTitle().lastIndexOf("."));
                Log.d(TAG, "onItemClick: extension " + fileExtension);
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                if (fileExtension.contains("apk")) {
                    Log.d(TAG, "open as apk");
                    intent.setDataAndType(data, "application/vnd.android.package-archive");
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                else
                    intent.setData(data);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toasty.error(App.getInstance().getApplicationContext(),"Not found any app that could open this file").show();
            } catch (Exception e) {
                Toasty.error(App.getInstance().getApplicationContext(),"Sorry, something went wrong").show();
                Log.d(TAG, "exception when trying to open file: "+e.getMessage());
            }
        }
    }

    public static void clearAllTasks() {
        if(mService!=null) mService.clearAllTasks();
    }

    public static void restartAll() {
        if(mService!=null) mService.restartAll();
    }

    public static void clearTasks(List<Integer> ids) {
        if(mService!=null) mService.clearTasks(ids);
    }

    public static void restartTasks(List<Integer> ids) {
        if(mService!=null) mService.restartTasks(ids);
    }

    public static void tryToResume(int id) {
        if(mService!=null) mService.tryToResume(id);
    }


    public static class RemoteServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public RemoteServiceBinder(final ServiceConnection callback){
            mCallback = callback;
        }


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "RemoteServiceBinder: onServiceConnected: ");
            
            TaskService.Binder binder = (TaskService.Binder) iBinder;
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
