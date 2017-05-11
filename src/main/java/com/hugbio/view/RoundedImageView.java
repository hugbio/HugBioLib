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

    private Drawable asyncDrawable = null;

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
        asyncDrawable = null;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        asyncDrawable = null;
    }

    @Override
    public void setBackground(Drawable background) {
        if (background instanceof FinalBitmap.AsyncDrawable) {
            asyncDrawable = background;
        } else {
            asyncDrawable = null;
        }
        super.setBackground(background);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        asyncDrawable = null;
    }

    @Override
    public void setBackgroundResource(int resId) {
        super.setBackgroundResource(resId);
        asyncDrawable = null;
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (background instanceof FinalBitmap.AsyncDrawable) {
            asyncDrawable = background;
        } else {
            asyncDrawable = null;
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof FinalBitmap.AsyncDrawable) {
            asyncDrawable = drawable;
        } else {
            asyncDrawable = null;
        }
        super.setImageDrawable(drawable);
    }

    @Override
    public Drawable getDrawable() {
        if (asyncDrawable != null) {
            return asyncDrawable;
        }
        return super.getDrawable();
    }
}
