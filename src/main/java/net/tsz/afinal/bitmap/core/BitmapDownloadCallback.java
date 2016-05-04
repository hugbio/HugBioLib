package net.tsz.afinal.bitmap.core;

import android.graphics.Bitmap;

/**
 * 图片下载回调
 */
public interface BitmapDownloadCallback {
	public Bitmap onDownloadSuccessForProcess(Bitmap bitmap);

	/***
	 * 开始下载回调
	 */
	public void onBitmapDownloadPrepare();

	/****
	 * 下载中回调
	 */
	public void onBitmapDownloadLoading(int total, int percent);

	/****
	 * 下载成功回调
	 */
	public void onBitmapDownloadSuccess();

	/****
	 * 下载失败回调
	 */
	public void onBitmapDownloadFail();
}
