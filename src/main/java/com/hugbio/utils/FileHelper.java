package com.hugbio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class FileHelper {


	/**
	 * 获取可以使用的缓存目录
	 *
	 * @param context
	 * @param uniqueName
	 *            目录名称
	 * @return
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) ? getExternalCacheDir(context)
				.getPath() : context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * 获取程序外部的缓存目录
	 *
	 * @param context
	 * @return
	 */
	public static File getExternalCacheDir(Context context) {
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ cacheDir);
	}



	public static FileOutputStream createFileOutputStream(String strPath) throws Exception {
		final File file = new File(strPath);
		try{
			return new FileOutputStream(file);
		}catch(Exception e){
			final File fileParent = file.getParentFile();
			if(!fileParent.exists()){
				if(fileParent.mkdirs()){
					return new FileOutputStream(file);
				}
			}
		}
		
		return null;
	}
	
	public static boolean isFileExists(String path){
		return new File(path).exists();
	}
	
	public static void checkOrCreateDirectory(String path){
		File file = new File(path);
		if(!file.exists()){
			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
		}
	}
	
	public static void deleteFile(String strPath){
		File file = new File(strPath);
		file.delete();
	}
	
	public static void deleteFolder(String strPath){
		File file = new File(strPath);
		if(file.isDirectory()){
			File fileChilds[] = file.listFiles();
			if(fileChilds == null){
				file.delete();
			}else{
				final int nLength = fileChilds.length;
				if(nLength > 0){
					for(File fileChild : fileChilds){
						if(fileChild.isDirectory()){
							deleteFolder(fileChild.getAbsolutePath());
						}else{
							fileChild.delete();
						}
					}
					file.delete();
				}else{
					file.delete();
				}
			}
		}else{
			file.delete();
		}
	}
	public static boolean deleteFileOrDir(File path) {
		if (path == null || !path.exists()) {
			return true;
		}
		if (path.isFile()) {
			return path.delete();
		}
		File[] files = path.listFiles();
		if (files != null) {
			for (File file : files) {
				deleteFileOrDir(file);
			}
		}
		return path.delete();
	}

	
	public static void saveBitmapToFile(String pathDst, Bitmap bmp){
		saveBitmapToFile(pathDst, bmp, 80);
	}
	
	public static void saveBitmapToFile(String pathDst, Bitmap bmp, int quality){
		try {
			FileOutputStream fos = createFileOutputStream(pathDst);
			bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void copyFile(String strPathDst, String strPathSrc){
		if(strPathDst != null && !strPathDst.equals(strPathSrc)){
			FileOutputStream fos = null;
			FileInputStream fis = null;
			try {
				fos = createFileOutputStream(strPathDst);
				fis = new FileInputStream(strPathSrc);
				byte buf[] = new byte[1024];
				int nReadBytes = 0;
				while((nReadBytes = fis.read(buf, 0, buf.length)) != -1){
					fos.write(buf, 0, nReadBytes);
				}
				fos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(fos != null){
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static String readFileToString(String strFilePath){
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(strFilePath),"GBK"));
			final StringBuffer sb = new StringBuffer();
			String strLine = null;
			while((strLine = br.readLine()) != null){
				sb.append(strLine);
			}
			return sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static String getFileExt(String fileName, String def){
		if(fileName == null){
			return def;
		}
		int pos = fileName.lastIndexOf(".");
		if(pos >= 0){
			return fileName.substring(pos + 1);
		}
		return def;
	}

	public static boolean rename(String src, String dst, boolean isCover) {
		if (!src.equals(dst)) {
			File newFile = new File(dst);
			File oldfile = new File(src);
			if (!oldfile.exists()) {
				return false;
			}
			if (newFile.exists()) {
				if (isCover && !newFile.isDirectory()) {
					newFile.delete();
				} else {
					return false;
				}
			}
			return oldfile.renameTo(newFile);
		}
		return true;
	}

	//----------------------------------------------2016.9.1新增-----------------------------------------------------
	/**
	 * 存储单个Parcelable对象
	 *
	 * @param fileName
	 *            保存路径
	 * @param parcelObject
	 *            对象必须实现Parcelable
	 */
	public static boolean writeParcelable(String fileName, Parcelable parcelObject)
	{
		boolean success = false;
		FileOutputStream fos = null;
		try
		{
			fos = createFileOutputStream(fileName);
			Parcel parcel = Parcel.obtain();
			parcel.writeParcelable(parcelObject, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
			byte[] data = parcel.marshall();
			fos.write(data);

			success = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}

		return success;
	}

	/**
	 * 存储List对象
	 *
	 * @param fileName
	 *            保存路径
	 * @param list
	 *            对象数组集合，对象必须实现Parcelable
	 */
	public static boolean writeParcelableList(String fileName, List<Parcelable> list)
	{
		boolean success = false;
		FileOutputStream fos = null;
		try
		{
			if (list instanceof List)
			{
				fos = createFileOutputStream(fileName);
				Parcel parcel = Parcel.obtain();
				parcel.writeList(list);
				byte[] data = parcel.marshall();
				fos.write(data);

				success = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}

		return success;
	}

	/**
	 * 读取单个数据对象
	 *
	 * @param fileName
	 *            文件路径
	 * @return Parcelable, 读取到的Parcelable对象，失败返回null
	 */
	public static Parcelable readParcelable(String fileName, ClassLoader classLoader)
	{
		Parcelable parcelable = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try
		{
			fis = new FileInputStream(fileName);
			if (fis != null)
			{
				bos = new ByteArrayOutputStream();
				byte[] b = new byte[4096];
				int bytesRead;
				while ((bytesRead = fis.read(b)) != -1)
				{
					bos.write(b, 0, bytesRead);
				}

				byte[] data = bos.toByteArray();

				Parcel parcel = Parcel.obtain();
				parcel.unmarshall(data, 0, data.length);
				parcel.setDataPosition(0);
				parcelable = parcel.readParcelable(classLoader);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			parcelable = null;
		}
		finally
		{
			if (fis != null) try
			{
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (bos != null) try
			{
				bos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return parcelable;
	}

	/**
	 * 读取数据对象列表
	 *
	 * @param fileName
	 *            文件路径
	 * @return List, 读取到的对象数组，失败返回null
	 */
	public static List<Parcelable> readParcelableList(String fileName, ClassLoader classLoader)
	{
		List<Parcelable> results = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try
		{
			fis = new FileInputStream(fileName);
			if (fis != null)
			{
				bos = new ByteArrayOutputStream();
				byte[] b = new byte[4096];
				int bytesRead;
				while ((bytesRead = fis.read(b)) != -1)
				{
					bos.write(b, 0, bytesRead);
				}

				byte[] data = bos.toByteArray();

				Parcel parcel = Parcel.obtain();
				parcel.unmarshall(data, 0, data.length);
				parcel.setDataPosition(0);
				results = parcel.readArrayList(classLoader);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			results = null;
		}
		finally
		{
			if (fis != null) try
			{
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (bos != null) try
			{
				bos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return results;
	}

	public static boolean writeSerializable(String fileName, Serializable data)
	{
		boolean success = false;
		ObjectOutputStream oos = null;
		try
		{
			oos = new ObjectOutputStream(createFileOutputStream(fileName));
			oos.writeObject(data);
			success = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return success;
	}

	public static Serializable readSerialLizable(String fileName)
	{
		Serializable data = null;
		ObjectInputStream ois = null;
		try
		{
			ois = new ObjectInputStream(new FileInputStream(fileName));
			data = (Serializable) ois.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (ois != null)
			{
				try
				{
					ois.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		return data;
	}




}
