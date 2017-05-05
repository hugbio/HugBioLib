package com.hugbio.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import net.tsz.afinal.FinalBitmap;

import java.lang.ref.WeakReference;

/**
 * 由 huangbiao在 2016/8/5 创建.
 */
public class RoundedImageView extends com.makeramen.roundedimageview.RoundedImageView {

//    private WeakReference<FinalBitmap.BitmapLoadAndDisplayTask> bitmapWorkerTaskReference  = null;

    private WeakReference<FinalBitmap.AsyncDrawable> asyncDrawableReference = null;

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
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if(drawable instanceof FinalBitmap.AsyncDrawable){
            asyncDrawableReference = new WeakReference<FinalBitmap.AsyncDrawable>((FinalBitmap.AsyncDrawable)drawable);
        }else {
            asyncDrawableReference = null;
        }
        super.setImageDrawable(drawable);
    }

    @Override
    public Drawable getDrawable() {
        if(asyncDrawableReference != null){
            FinalBitmap.AsyncDrawable asyncDrawable = asyncDrawableReference.get();
            if(asyncDrawable != null){
                return asyncDrawable;
            }
        }
        return super.getDrawable();
    }
}
