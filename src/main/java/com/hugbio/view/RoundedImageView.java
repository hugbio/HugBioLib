package com.hugbio.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import net.tsz.afinal.FinalBitmap;

import java.lang.ref.WeakReference;

/**
 * 由 huangbiao在 2016/8/5 创建.
 */
public class RoundedImageView extends com.makeramen.roundedimageview.RoundedImageView {

    private WeakReference<FinalBitmap.BitmapLoadAndDisplayTask> bitmapWorkerTaskReference  = null;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setImageDrawable(Drawable drawable) {
        if(drawable instanceof FinalBitmap.AsyncDrawable){
            FinalBitmap.BitmapLoadAndDisplayTask bitmapWorkerTask = ((FinalBitmap.AsyncDrawable) drawable).getBitmapWorkerTask();
            if(bitmapWorkerTask != null){
                bitmapWorkerTaskReference = new WeakReference<FinalBitmap.BitmapLoadAndDisplayTask>(bitmapWorkerTask);
            }else {
                bitmapWorkerTaskReference = null;
            }
        }else {
            bitmapWorkerTaskReference = null;
        }
        super.setImageDrawable(drawable);
    }

    @Override
    public Drawable getDrawable() {
        if(bitmapWorkerTaskReference != null){
            FinalBitmap.BitmapLoadAndDisplayTask bitmapLoadAndDisplayTask = bitmapWorkerTaskReference.get();
            if(bitmapLoadAndDisplayTask != null){
                return new FinalBitmap.AsyncDrawable(getResources(),null,bitmapLoadAndDisplayTask);
            }
        }
        return super.getDrawable();
    }
}
