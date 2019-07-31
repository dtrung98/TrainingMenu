package com.zalo.servicetraining.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;

import com.zalo.servicetraining.App;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Util {
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
}
