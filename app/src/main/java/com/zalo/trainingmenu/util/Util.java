package com.zalo.trainingmenu.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;


import androidx.preference.PreferenceManager;

import com.zalo.trainingmenu.App;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.Environment.getExternalStoragePublicDirectory;

public final class Util {
    private static final String TAG = "Util";

   /* @SuppressLint("DefaultLocale")
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }*/

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCount(long bytes) {
        boolean si = true;
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static String formatPrettyDateTime(long time) {
        if(time==-1) return "Undefined";
        return DateUtils.formatDateTime(App.getInstance().getApplicationContext(), time, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    public static String formatPrettyDateTimeWithSecond(long time) {
        if(time==-1) return "Undefined";
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss E, dd MMM yyyy",getCurrentLocale(App.getInstance().getApplicationContext()));
        return formatter.format(new Date(time));
    }

    @SuppressLint("DefaultLocale")
    public static String formatDuration(long durationInMillis) {
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }
    public static boolean setClipboard(Context context,String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard!=null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            return true;
        }
        return false;
    }

    public static boolean hasSoftKeys(WindowManager windowManager){
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    public static int getNavigationHeight(Activity activity)
    {

        int navigationBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        if(!hasSoftKeys(activity.getWindowManager())) return 0;
        return  navigationBarHeight;
    }

    public static void vibrate() {
        Vibrator vibrator = (Vibrator) App.getInstance().getSystemService(VIBRATOR_SERVICE);
        if(vibrator!=null) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    public static File getDefaultDirectory() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return App.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        } else
            return getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static String getDefaultDirectoryPath() {
        File file = getDefaultDirectory();
        return (file==null) ? "":file.getAbsolutePath();
    }

    public static String getCurrentDownloadDirectoryPath() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).getString("downloadsFolder",getDefaultDirectoryPath());
    }

    public static String generateTitle(String url, String directoryPath) {

        String autoTitle = URLUtil.guessFileName(url, null, null);
        if(autoTitle==null || autoTitle.isEmpty())
            autoTitle = "Unknown.std";

        return makeSureFileNameDoesNotExist(directoryPath ,autoTitle);
    }

    public static String generateTitle(String url, String directoryPath, String contentDisposition, String mimeType) {
        Log.d(TAG, "try to generate title with mimetype "+mimeType);
        String autoTitle;
        String extensionInUrl = MimeTypeMap.getFileExtensionFromUrl(url);

        if(extensionInUrl.isEmpty())
        autoTitle = URLUtil.guessFileName(url, contentDisposition, mimeType);
        else autoTitle = URLUtil.guessFileName(url,contentDisposition,extensionInUrl);
        if(autoTitle==null || autoTitle.isEmpty())
            autoTitle = "Untitled.bin";

        return makeSureFileNameDoesNotExist(directoryPath ,autoTitle);
    }

    public static String makeSureFileNameDoesNotExist(String parentFolder, String title) {
        Log.d(TAG, "update name for "+title);
        if( new File(parentFolder,title).exists()) {
            Pattern p = Pattern.compile("(.*?)?(\\..*)?");
            Matcher m = p.matcher(title);
            if(m.matches()) {
                String base = m.group(1);
                String extension = m.group(2);
                int i = 1;
                do {
                    title = base + " ("+i+")"+extension;
                    i++;
                } while (new File(parentFolder,title).exists());
            } else {
                String base = title;
                int i = 1;
                do {
                    title = base +" ("+i+")";
                } while (new File(parentFolder,title).exists());
            }
        }
        return title;
    }

    public static void openDirectoryIntent(Context context, String path)
    {
        Uri selectedUri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");
        if(intent.resolveActivityInfo(context.getPackageManager(),0) == null) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(selectedUri,"file/*");
        }

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toasty.error(App.getInstance().getApplicationContext(), "No apps found to open this folder").show();
        } catch (Exception e) {
            Toasty.error(App.getInstance().getApplicationContext(), "Sorry, something went wrong!").show();
        }
    }

    private static int[] screenSize;
    public static boolean HAD_GOT_SCREEN_SIZE = false;
    public static int[] getScreenSize(Context context)
    {
        if(!HAD_GOT_SCREEN_SIZE) {
            Point p = new Point();
            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); // this will get the view of screen
            d.getRealSize(p);
            int width = p.x;
            int height = p.y;
            screenSize = new int[] {width,height};
            HAD_GOT_SCREEN_SIZE = true;
        }
        return screenSize;
    }

    public static Bitmap getBitmapWithUri(Context context, Uri selectedimg) throws IOException {
        //    BitmapFactory.Options options = new BitmapFactory.Options();

        // options.inSampleSize = 2;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor =
                context.getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        Bitmap original = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
        return original;
    }
}
