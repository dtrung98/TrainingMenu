package com.ldt.parallaximageview.util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public final class Util {
    static int[] screenSize;
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
    public static void getScreenSize(Context context, int[] size) {
        if(!HAD_GOT_SCREEN_SIZE) {
            Point p = new Point();
            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); // this will get the view of screen
            d.getRealSize(p);
            int width = p.x;
            int height = p.y;
            screenSize = new int[] {width,height};
            HAD_GOT_SCREEN_SIZE = true;
        }
        size[0] = screenSize[0];
        size[1] = screenSize[1];
    }
}
