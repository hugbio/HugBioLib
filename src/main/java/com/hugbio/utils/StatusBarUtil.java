package com.hugbio.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;


/**
 * 状态栏工具。支持设置沉浸式状态栏或设置状态栏颜色，支持设置状态栏高亮，附带一些虚拟按键的操作
 * 设置状态栏颜色和高亮需要api版本支持
 * 作者： huangbiao
 * 时间： 2017-08-12
 */
public class StatusBarUtil {
    private final static int SHADOW_COLOR = 0x80000000;  //半透明颜色

    /**
     * 设置状态栏半透明
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setTranslucent(Activity activity) {
        setColor(activity, SHADOW_COLOR);
    }

    /**
     * 设置状态栏为透明
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setTransparent(Activity activity) {
        setColor(activity, Color.TRANSPARENT);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity
     * @param color
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setColor(Activity activity, int color) {
        View decorView = activity.getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE //
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;//全屏，显示状态栏
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;//隐藏虚拟按键
        decorView.setSystemUiVisibility(option);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //添加flag来设置状态栏颜色
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(color);//设置状态栏颜色
//            if (hasNavigationBar(activity)) {//若有虚拟按键
//                activity.getWindow().setNavigationBarColor(color);//设置虚拟按键颜色
//            }
        } else {//处理4.4
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 设置状态栏为高亮模式
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setStatusBarLight(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//设置文本颜色为暗色
        } else {
            setTranslucent(activity);//设置背景颜色为半透明
        }
    }

    /**
     * 设置沉浸式状态栏
     * 在 onWindowFocusChanged() 中调用
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setImmersive(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE//
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//全屏，显示状态栏
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//隐藏虚拟按键
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN//全屏,隐藏状态栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //添加flag来设置状态栏颜色
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(SHADOW_COLOR);//设置状态栏颜色
//            if (hasNavigationBar(activity)) {//若有虚拟按键
//                activity.getWindow().setNavigationBarColor(SHADOW_COLOR);//设置虚拟按键颜色
//            }
        } else {//处理4.4
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * 获得状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    /**
     * 是否存在虚拟按键
     *
     * @param context
     * @return
     */
    public static boolean hasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        try {
            Resources rs = context.getResources();
            int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                hasNavigationBar = rs.getBoolean(id);
            }
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }

    /**
     * 获取虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        if (hasNavigationBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * 隐藏虚拟按键
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void hideNavigationBar(Activity activity) {
        if (hasNavigationBar(activity)) {
            View decorView = activity.getWindow().getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(systemUiVisibility//
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);//隐藏虚拟按键
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN//全屏，显示状态栏
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
