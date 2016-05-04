package com.hugbio.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class ViewTools {
	static float  sDensity = 0;
	public static int dipToPixel(Context context,int nDip) {
		if(sDensity <= 0){
			final WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);
			sDensity = dm.density;
		}
		return (int) (sDensity * nDip);
	}
	public static View createXProgressDialog(Context context) {
		FrameLayout layout = new FrameLayout(context);
		layout.setBackgroundResource(com.hugbio.androidevent.R.drawable.loading_bg);
		ProgressBar pb = new ProgressBar(context);
		pb.setIndeterminate(true);
		int pbSize = FrameLayout.LayoutParams.WRAP_CONTENT;
		FrameLayout.LayoutParams lpPb = new FrameLayout.LayoutParams(pbSize,
				pbSize);
		lpPb.gravity = Gravity.CENTER;
		layout.addView(pb, lpPb);
		return layout;
	}
}

