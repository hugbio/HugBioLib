package net.tsz.afinal.bitmap.core;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION_CODES;
import android.support.v4.util.LruCache;

public class MyLruCache extends LruCache<String, BitmapDrawable> {
	
	private Set<SoftReference<Bitmap>> mReusableBitmaps;
	
	public MyLruCache(int maxSize) {
		super(maxSize);
		if (Utils.hasHoneycomb()) {
			mReusableBitmaps =
					Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected int sizeOf(String key, BitmapDrawable bitmap) {
		// 重写此方法来衡量每张图片的大小，默认返回图片数量。
		final int bitmapSize = getBitmapSize(bitmap) / 1024;
        return bitmapSize == 0 ? 1 : bitmapSize;
	}
	
	@Override
	protected void entryRemoved(boolean evicted, String key,
			BitmapDrawable oldValue, BitmapDrawable newValue) {
		// TODO Auto-generated method stub
		 if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
             // The removed entry is a recycling drawable, so notify it
             // that it has been removed from the memory cache
             ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
         } else {
             // The removed entry is a standard BitmapDrawable

             if (Utils.hasHoneycomb()) {
                 // We're running on Honeycomb or later, so add the bitmap
                 // to a SoftReference set for possible use with inBitmap later
					mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
             }
         }
	}

//	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
//		if (getBitmapFromMemCache(key) == null) {
//			put(key, bitmap);
//		}
//	}
//
//	public Bitmap getBitmapFromMemCache(String key) {
//		return get(key);
//	}
	
	 @TargetApi(VERSION_CODES.KITKAT)
	    public static int getBitmapSize(BitmapDrawable value) {
	        Bitmap bitmap = value.getBitmap();

	        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
	        // larger than bitmap byte count.
	        if (Utils.hasKitKat()) {
				try {
					int allocationByteCount = bitmap.getAllocationByteCount();
					return allocationByteCount;
				}catch (Exception e){
					e.printStackTrace();
				}
				return 0;
	        }

	        if (Utils.hasHoneycombMR1()) {
	            return bitmap.getByteCount();
	        }

	        // Pre HC-MR1
	        return bitmap.getRowBytes() * bitmap.getHeight();
	    }
	 
	 /**
	     * @param options - BitmapFactory.Options with out* options populated
	     * @return Bitmap that case be used for inBitmap
	     */
	    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
	        //BEGIN_INCLUDE(get_bitmap_from_reusable_set)
	        Bitmap bitmap = null;

	        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
	            synchronized (mReusableBitmaps) {
	                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
	                Bitmap item;

	                while (iterator.hasNext()) {
	                    item = iterator.next().get();

	                    if (null != item && item.isMutable()) {
	                        // Check to see it the item can be used for inBitmap
	                        if (canUseForInBitmap(item, options)) {
	                            bitmap = item;

	                            // Remove from reusable set so it can't be used again
	                            iterator.remove();
	                            break;
	                        }
	                    } else {
	                        // Remove from the set if the reference has been cleared.
	                        iterator.remove();
	                    }
	                }
	            }
	        }

	        return bitmap;
	        //END_INCLUDE(get_bitmap_from_reusable_set)
	    }
	    
	    @TargetApi(VERSION_CODES.KITKAT)
	    private static boolean canUseForInBitmap(
	            Bitmap candidate, BitmapFactory.Options targetOptions) {
	        //BEGIN_INCLUDE(can_use_for_inbitmap)
	        if (!Utils.hasKitKat()) {
	            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
	            return candidate.getWidth() == targetOptions.outWidth
	                    && candidate.getHeight() == targetOptions.outHeight
	                    && targetOptions.inSampleSize == 1;
	        }

	        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
	        // is smaller than the reusable bitmap candidate allocation byte count.
	        int width = targetOptions.outWidth / targetOptions.inSampleSize;
	        int height = targetOptions.outHeight / targetOptions.inSampleSize;
	        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
	        return byteCount <= candidate.getAllocationByteCount();
	        //END_INCLUDE(can_use_for_inbitmap)
	    }
	    
	    private static int getBytesPerPixel(Config config) {
	        if (config == Config.ARGB_8888) {
	            return 4;
	        } else if (config == Config.RGB_565) {
	            return 2;
	        } else if (config == Config.ARGB_4444) {
	            return 2;
	        } else if (config == Config.ALPHA_8) {
	            return 1;
	        }
	        return 1;
	    }
}
