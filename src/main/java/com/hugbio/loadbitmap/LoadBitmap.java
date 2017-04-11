package com.hugbio.loadbitmap;

import java.lang.ref.SoftReference;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDownloadCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class LoadBitmap {
    private static FinalBitmap finalBitmap = null;
    private static SparseArray<SoftReference<Bitmap>> mapResIdToBitmap = new SparseArray<SoftReference<Bitmap>>();
    private static Context applicationContext = null;
    protected static int bmpMaxWidth;
    protected static int bmpMaxHeight;

    public static FinalBitmap getFinalBitmap(Context mContext) {
        if (finalBitmap == null) {
            initFinalBitmap(mContext);
        }
        return finalBitmap;
    }

    public static void initFinalBitmap(Context mContext) {
        applicationContext = mContext.getApplicationContext();
        finalBitmap = FinalBitmap.create(applicationContext);
        finalBitmap.configDisplayer(new XBitmapDisplayer());
        WindowManager wm = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        bmpMaxWidth = dm.widthPixels / 2;
        bmpMaxHeight = bmpMaxWidth;
    }

    /***
     * 根据url获取Bitmap，会首先从缓存（内存缓存和磁盘缓存）中获取。如果缓存没有获取到则从网络下载
     * 注意。不能在ui线程中调用该方法。
     */
    public static Bitmap getOrDownloadBitmap(String url) {
        if (finalBitmap == null) {
            return null;
        }
        return finalBitmap.getOrDownloadBitmap(url);
    }

    public static Bitmap getOrDownloadBitmap(String url, int w, int h) {
        if (finalBitmap == null) {
            return null;
        }
        return finalBitmap.getOrDownloadBitmap(url, w, h);
    }

    public static byte[] downloadByte(String url) {
        if (finalBitmap == null) {
            return null;
        }
        return finalBitmap.downloadByte(url);
    }

    public static void addToMemoryCache(String urikey, Bitmap bitmap, int w, int h) {
        if (finalBitmap == null) {
            return;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            finalBitmap.addToMemoryCache(urikey, bitmap, w, h);
        }
    }

    public static void addToMemoryCache(String urikey, Bitmap bitmap) {
        if (finalBitmap == null) {
            return;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            finalBitmap.addToMemoryCache(urikey, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemoryCache(String key) {
        if (finalBitmap == null) {
            return null;
        }
        return finalBitmap.getBitmapFromMemoryCache(key);
    }

    public static Bitmap getBitmapFromMemoryCache(String key,int w,int h) {
        if (finalBitmap == null) {
            return null;
        }
        return finalBitmap.getBitmapFromMemoryCache(key,w,h);
    }

    public static void setBitmapEx(final View view, String url, int defaultResId) {
        if (finalBitmap == null) {
            return;
        }
        if (TextUtils.isEmpty(url) && defaultResId != 0) {
            if (view instanceof ImageView) {
                final ImageView iv = (ImageView) view;
                iv.setImageResource(defaultResId);
            } else {
                view.setBackgroundResource(defaultResId);
            }
        } else {
            Bitmap bmpLoading = getResBitmap(defaultResId);
            Bitmap bmpFail = bmpLoading;
            finalBitmap.display(view, url, bmpMaxWidth, bmpMaxHeight,
                    bmpLoading, bmpFail);
        }
    }

    public static void setBitmapCallback(final View view, String url, int defaultResId, BitmapDownloadCallback callback, String bitmapProcessName) {
        setBitmapCallback(view, url, bmpMaxWidth, bmpMaxHeight, defaultResId, callback, bitmapProcessName);
    }

    public static void setBitmapCallback(final View view, String url, int defaultResId, BitmapDownloadCallback callback) {
        setBitmapCallback(view, url, bmpMaxWidth, bmpMaxHeight, defaultResId, callback, null);
    }

    public static void setBitmapCallback(final View view, String url, int bmpWidth, int bmpHeight, int defaultResId, BitmapDownloadCallback callback) {
        setBitmapCallback(view, url, bmpWidth, bmpHeight, defaultResId, callback, null);
    }


    public static void setBitmapCallback(final View view, String url, int bmpWidth, int bmpHeight, int defaultResId, BitmapDownloadCallback callback,
                                         String bitmapProcessName) {
        if (finalBitmap == null) {
            return;
        }
        if (TextUtils.isEmpty(url) && defaultResId != 0) {
            if (view instanceof ImageView) {
                final ImageView iv = (ImageView) view;
                iv.setImageResource(defaultResId);
            } else {
                view.setBackgroundResource(defaultResId);
            }
        } else {
            Bitmap bmpLoading = getResBitmap(defaultResId);
            Bitmap bmpFail = bmpLoading;
            if (callback != null) {
                if (!TextUtils.isEmpty(bitmapProcessName)) {
                    finalBitmap.display(view, url, bmpWidth, bmpHeight, bmpLoading, bmpFail, callback, bitmapProcessName);
                } else {
                    finalBitmap.display(view, url, bmpWidth, bmpHeight, bmpLoading, bmpFail, callback);
                }
            } else {
                finalBitmap.display(view, url, bmpWidth, bmpHeight, bmpLoading, bmpFail);
            }
        }
    }

    public static void setBitmapEx(final View view, String url, int bmpWidth,
                                   int bmpHeight, int defaultResId) {
        if (finalBitmap == null) {
            return;
        }
        if (TextUtils.isEmpty(url) && defaultResId != 0) {
            if (view instanceof ImageView) {
                final ImageView iv = (ImageView) view;
                iv.setImageResource(defaultResId);
            } else {
                view.setBackgroundResource(defaultResId);
            }
        } else {
            Bitmap bmpLoading = getResBitmap(defaultResId);
            Bitmap bmpFail = bmpLoading;
            finalBitmap.display(view, url, bmpWidth, bmpHeight, bmpLoading,
                    bmpFail);
        }
    }

    public static void setBitmapTransition(final ImageView iv, String url) {
        setBitmapTransition(iv, url, 0);
    }

    public static void setBitmapTransition(final ImageView iv, String url,
                                           int defaultResId) {
        if (finalBitmap == null) {
            return;
        }
        Bitmap bmpLoading = null;
        Bitmap bmpFail = null;
        final Drawable d = iv.getDrawable();
        if (d != null) {
            if (d instanceof TransitionDrawable) {
                final TransitionDrawable td = (TransitionDrawable) d;
                try {
                    final BitmapDrawable bd = (BitmapDrawable) td
                            .getDrawable(1);
                    bmpLoading = bd.getBitmap();
                } catch (Exception e) {

                }
            }
        }
        if (bmpLoading == null) {
            bmpLoading = getResBitmap(defaultResId);
        }

        bmpFail = bmpLoading;
        finalBitmap.display(iv, url, bmpMaxWidth, bmpMaxHeight, bmpLoading,
                bmpFail);
    }

    protected static Bitmap getResBitmap(int resId) {
        if (resId == 0) {
            return null;
        }
        Bitmap bmp;
        SoftReference<Bitmap> sf = mapResIdToBitmap.get(resId);
        bmp = sf == null ? null : sf.get();
        if (bmp == null) {
            try {
                final Drawable d = applicationContext.getResources().getDrawable(resId);
                if (d instanceof BitmapDrawable) {
                    bmp = ((BitmapDrawable) d).getBitmap();
                }
                if (bmp != null) {
                    mapResIdToBitmap.put(resId, new SoftReference<Bitmap>(bmp));
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }
}
