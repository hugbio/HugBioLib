package com.hugbio.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 由 HBOK 在 2016/5/27 创建.
 */
public class ImageUtils {
    public static final int ALL = 0;
    public static final int TOP = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM = 4;


    // 生成圆角图片(只支持生成正方形圆角)
    public static Bitmap GetRoundedBitmap(Bitmap bitmap,int type) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float roundPx = 15;
            float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
            if (width <= height) {
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
            final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
            final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);

            paint.setAntiAlias(true);

            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
//            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            if( TOP == type ){
                clipTop(canvas,paint,roundPx,(int) dst_right,(int) dst_bottom);
            }else if( LEFT == type ){
                clipLeft(canvas,paint,roundPx,(int) dst_right,(int) dst_bottom);
            }else if( RIGHT == type ){
                clipRight(canvas,paint,roundPx,(int) dst_right,(int) dst_bottom);
            }else if( BOTTOM == type ){
                clipBottom(canvas,paint,roundPx,(int) dst_right,(int) dst_bottom);
            }else{
                clipAll(canvas,paint,roundPx,(int) dst_right,(int) dst_bottom);
            }
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, dst, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }


    private static void clipLeft(final Canvas canvas,final Paint paint,float offset,int width,int height){
        final RectF block = new RectF(offset,0,width,height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, 0, offset * 2 , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }

    private static void clipRight(final Canvas canvas,final Paint paint,float offset,int width,int height){
        final RectF block = new RectF(0, 0, width-offset, height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(width - offset * 2, 0, width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }

    private static void clipTop(final Canvas canvas,final Paint paint,float offset,int width,int height){
        final RectF block = new RectF(0, offset, width, height);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, 0, width , offset * 2);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }

    private static void clipBottom(final Canvas canvas,final Paint paint,float offset,int width,int height){
        final RectF block = new RectF(0, 0, width, height - offset);
        canvas.drawRect(block, paint);
        final RectF rectF = new RectF(0, height - offset * 2 , width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }

    private static void clipAll(final Canvas canvas,final Paint paint,float offset,int width,int height){
        final RectF rectF = new RectF(0, 0, width , height);
        canvas.drawRoundRect(rectF, offset, offset, paint);
    }
}
