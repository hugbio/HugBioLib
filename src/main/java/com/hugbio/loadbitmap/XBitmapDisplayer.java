package com.hugbio.loadbitmap;

import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.SimpleDisplayer;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.ImageView;

public class XBitmapDisplayer extends SimpleDisplayer {

	@SuppressWarnings("deprecation")
	@Override
	public void loadCompletedisplay(View imageView, Bitmap bitmap, BitmapDisplayConfig config) {
		if(imageView instanceof com.makeramen.roundedimageview.RoundedImageView){
			final ImageView iv = (ImageView) imageView;
			BitmapDrawable bitmapDrawable = new BitmapDrawable(imageView.getResources(), bitmap);
			iv.setImageDrawable(bitmapDrawable);
		}else if(imageView instanceof ImageView){
        	final ImageView iv = (ImageView)imageView;
        	if(bitmap.hasAlpha()){
        		final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[] {
                                new ColorDrawable(android.R.color.transparent),
                                new BitmapDrawable(imageView.getResources(), bitmap)
                        });
        		iv.setImageDrawable(td);
        		td.startTransition(300);
        	}else{
        		final TransitionDrawable td = new XTransitionDrawable(
            			iv.getDrawable(),
            			new BitmapDrawable(imageView.getResources(), bitmap));
    			iv.setImageDrawable(td);
    			td.startTransition(500);
        	}
		}else{
        	final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
        			imageView.getBackground(),
        			new BitmapDrawable(imageView.getResources(), bitmap)});
			imageView.setBackgroundDrawable(td);
			td.startTransition(500);
		}
	}
	
	private static class XTransitionDrawable extends TransitionDrawable{
		Drawable mSecond;
		
		public XTransitionDrawable(Drawable first,Drawable second){
			super(new Drawable[]{first,second});
			mSecond = second;
		}
		@Override
		public int getIntrinsicWidth() {
			return mSecond.getIntrinsicWidth();
		}
		@Override
		public int getIntrinsicHeight() {
			return mSecond.getIntrinsicHeight();
		}
	}
}
