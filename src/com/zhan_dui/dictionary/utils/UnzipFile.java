package com.zhan_dui.dictionary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Handler;
import android.os.Message;

/**
 * 解压缩文件类
 * 
 * @author xuanqinanhai
 * 
 */

public class UnzipFile {
	private Handler dealHandler;
	private InputStream inputStream;
	private String outputDirectory;
	private boolean isReWrite;

	public UnzipFile(Handler dealHandler, InputStream inputStream,
			String outputDirectory, boolean isReWrite) {
		super();
		this.dealHandler = dealHandler;
		this.inputStream = inputStream;
		this.outputDirectory = outputDirectory;
		this.isReWrite = isReWrite;
		this.dealHandler = dealHandler;
	}

	/**
	 * 解压assets的zip压缩文件到指定目录
	 * 
	 * @param context上下文对象
	 * @param assetName压缩文件名
	 * @param outputDirectory输出目录
	 * @param isReWrite是否覆盖
	 * @throws IOException
	 */

	public void unzip() {
		// 创建解压目标目录
		File file = new File(outputDirectory);
		// 如果目标目录不存在，则创建
		if (!file.exists()) {
			file.mkdirs();
		}
		// 打开压缩文件
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		try {
			// 读取一个进入点
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			// 使用1Mbuffer
			byte[] buffer = new byte[1024];
			// 解压时字节计数
			int count = 0;

			Message startMsg = Message.obtain(dealHandler,
					Constants.UNZIP_START);
			startMsg.sendToTarget();

			// 如果进入点为空说明已经遍历完所有压缩包中文件和目录
			while (zipEntry != null) {
				// 如果是一个目录
				if (zipEntry.isDirectory()) {
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// 文件需要覆盖或者是文件不存在
					if (isReWrite || !file.exists()) {
						file.mkdir();
					}
				} else {
					// 如果是文件
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// 文件需要覆盖或者文件不存在，则解压文件
					if (isReWrite || !file.exists()) {
						file.createNewFile();
						FileOutputStream fileOutputStream = new FileOutputStream(
								file);
						while ((count = zipInputStream.read(buffer)) > 0) {
							fileOutputStream.write(buffer, 0, count);
						}
						fileOutputStream.close();
					}
				}
				// 定位到下一个文件入口
				zipEntry = zipInputStream.getNextEntry();
			}
			zipInputStream.close();
			dealHandler.sendEmptyMessage(Constants.UNZIP_FINISH);
		} catch (Exception e) {
			dealHandler.sendEmptyMessage(Constants.UNZIP_ERROR);
			e.printStackTrace();
		}
	}
}
