/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal.bitmap.core;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BytesBufferPool.BytesBuffer;
import net.tsz.afinal.bitmap.download.Downloader;

@SuppressLint("DefaultLocale")
public class BitmapProcess {
	private Downloader mDownloader;
	private BitmapCache mCache;
	
	private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);

	public BitmapProcess(Downloader downloader,BitmapCache cache) {
		this.mDownloader = downloader;
		this.mCache = cache;
	}

	public Bitmap getBitmap(String url, BitmapDisplayConfig config, FinalBitmap.BitmapLoadAndDisplayTask task) {
		Bitmap bitmap = null;
		bitmap = getFromDisk(url, config);
		if (bitmap == null) {
			byte[] data = mDownloader.download(url,task);
			if (data != null && data.length > 0) {
				if (config != null){
					bitmap =  BitmapDecoder.decodeSampledBitmapFromByteArray(data, 0, data.length, config.getBitmapWidth(), config.getBitmapHeight(),mCache);
				}else{
					bitmap = BitmapDecoder.decodeSampledBitmapFromByteArray(data, 0, data.length, 200,200, mCache);
				}
				if (url.trim().toLowerCase().startsWith("http")) {
					mCache.addToDiskCache(url, data);
					data = null;
				}
				return bitmap;
			}
		}
		return bitmap;
	}
	
	public Bitmap getFromDisk(String key,BitmapDisplayConfig config) {
        BytesBuffer buffer = sMicroThumbBufferPool.get();
        Bitmap b = null;
        try {
        	boolean found = mCache.getImageData(key, buffer);
            if ( found && buffer.length - buffer.offset > 0) {
    	        if( config != null){
    	            b = BitmapDecoder.decodeSampledBitmapFromByteArray(buffer.data,buffer.offset, buffer.length ,config.getBitmapWidth(),config.getBitmapHeight(),mCache);
    	        }else{
    	        	b = BitmapDecoder.decodeSampledBitmapFromByteArray(buffer.data, buffer.offset, buffer.length, 200, 200, mCache);
    	        }
            }
        } finally {
        	sMicroThumbBufferPool.recycle(buffer);
        }
        return b;
    }
}
