package com.hugbio.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 由 HBOK 在 2016/5/27 创建.
 */
public class ImageUtils {

    /**
     * 按正方形裁切图片
     */
    public static Bitmap ImageCropForSquare(Bitmap bitmap, boolean isRecycled) {

        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        // 下面这句是关键
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
        // false);
    }


    /**
     * Bitmap --> byte[]
     *
     * @param bmp
     * @return
     */
    public static byte[] toByteArray(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }


    /**
     * 将图像裁剪成圆形
     *
     * @param bitmap
     * @return
     */
    public static Bitmap ImageCropForCircle(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return output;
    }


    /**
     * 图片转圆角
     *
     * @param bitmap 需要转的bitmap
     * @param pixels 转圆角的弧度
     * @return 转圆角的bitmap
     */
    public static Bitmap toRoundImage(Bitmap bitmap, float w, float h, int pixels) {
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = getCropRect(bitmap.getWidth(),bitmap.getHeight(),w,h);
        final Rect dst = new Rect(0, 0, rect.right - rect.left, rect.bottom - rect.top);
        Bitmap output = Bitmap.createBitmap(rect.right - rect.left,
                rect.bottom - rect.top, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(dst);
        final float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, dst, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return output;
    }


    /**
     * 按照一定的宽高比例裁剪图片
     *
     * @param bitmap
     * @param width  宽比例
     * @param height 高比例
     * @return
     */
    public static Bitmap ImageCrop(Bitmap bitmap, float width, float height,
                                   boolean isRecycled) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        Rect cropRect = getCropRect(w, h, width, height);
        int retX, retY;
        int nw, nh;
        retX = cropRect.left;
        retY = cropRect.top;
        nw = cropRect.right - cropRect.left;
        nh = cropRect.bottom - cropRect.top;
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;
    }

    /**
     * 获取需要剪切的矩形位置坐标
     *
     * @param w     原图宽
     * @param h     原图高
     * @param cropw 需要剪切的宽比例
     * @param croph 需要剪切的高比例
     */
    private static Rect getCropRect(int w, int h, float cropw, float croph) {
        int retX, retY;
        int nw, nh;
        if (w > h) {
            if (h > w * croph / cropw) {
                nw = w;
                nh = (int) (w * croph / cropw);
                retX = 0;
                retY = (h - nh) / 2;
            } else {
                nw = (int) (h * cropw / croph);
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else {
            if (w > h * cropw / croph) {
                nh = h;
                nw = (int) (h * cropw / croph);
                retY = 0;
                retX = (w - nw) / 2;
            } else {
                nh = (int) (w * croph / cropw);
                nw = w;
                retY = (h - nh) / 2;
                retX = 0;
            }
        }
        return new Rect(retX, retY, retX + nw, retY + nh);
    }


}
