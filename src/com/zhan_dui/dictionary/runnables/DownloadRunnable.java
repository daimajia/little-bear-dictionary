package com.zhan_dui.dictionary.runnables;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.zhan_dui.dictionary.utils.Constants;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * 实现下载的Runnable接口
 * 
 * @author xuanqinanhai
 * 
 */
public class DownloadRunnable implements Runnable {

	private Handler handlers[];
	private String targetUrl;
	private String saveName;
	private String cnName;

	/**
	 * 如果想要得到每次的下载更新，则声明该接口
	 * 
	 * @author xuanqinanhai
	 * 
	 */
	public interface DownloadGetEveryUpdateInterface {

	}

	static {
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + Constants.SAVE_DIRECTORY);
			if (!file.exists())
				file.mkdir();
		}
	}

	/**
	 * 
	 * @param handlers
	 *            下载处理线程
	 * @param targetUrl
	 *            要下载的文件url
	 * @param saveName
	 *            要保存的文件名
	 */
	public DownloadRunnable(Handler handlers[], String targetUrl,
			String saveName, String cnName, boolean hidden, String prefix) {
		this.handlers = handlers;
		this.cnName = cnName;
		this.targetUrl = targetUrl;
		this.saveName = saveName;
		if (prefix != null) {
			this.saveName = prefix + this.saveName;
		}
		if (hidden == true) {
			this.saveName = "." + this.saveName;
		}
	}

	public static class DownloadInformation {
		public String downloadFileName;
		public String downloadSaveName;
		public String downloadUrl;
		public String downloadFlag;
		public long downloadCurrentSize;
		public long downloadId;
	}

	private boolean stopflag = false;

	public void stop() {
		stopflag = true;
	}

	@Override
	public void run() {
		Message msg = null;
		File file;
		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			file = new File(Environment.getExternalStorageDirectory() + "/"
					+ Constants.SAVE_DIRECTORY, saveName);
			if (file.exists() == false) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					for (Handler handler : handlers) {
						msg = Message.obtain(handler,
								Constants.FILE_CREATE_ERROR);
						msg.obj = cnName;
						msg.sendToTarget();
					}
					return;
				}
			}
			DownloadInformation downloadInformation = new DownloadInformation();
			downloadInformation.downloadFileName = cnName;
			downloadInformation.downloadSaveName = saveName;
			downloadInformation.downloadUrl = targetUrl;
			try {
				URL url = new URL(targetUrl);
				URLConnection connection = url.openConnection();
				connection.setReadTimeout(30000);
				connection.setConnectTimeout(30000);
				InputStream inputStream = connection.getInputStream();
				OutputStream outputStream = new FileOutputStream(file);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						outputStream);

				byte buffer[] = new byte[1024];
				int length;
				int downloaded = 0;
				int counter = 0;
				while ((length = inputStream.read(buffer)) > 0
						&& stopflag == false) {
					bufferedOutputStream.write(buffer, 0, length);
					downloaded += length;
					if (counter++ % 1024 == 0) {
						for (Handler handler : handlers) {
							if (handler instanceof DownloadGetEveryUpdateInterface) {
								msg = Message.obtain(handler,
										Constants.DOWNLOADING,
										downloaded / 1024, 0);
								msg.obj = downloadInformation;
								msg.sendToTarget();
							}
						}
					}
				}

				bufferedOutputStream.flush();
				bufferedOutputStream.close();
				inputStream.close();
				if (stopflag == true) {
					for (Handler handler : handlers) {
						msg = Message
								.obtain(handler, Constants.DOWNLOAD_CANCEL);
						msg.obj = downloadInformation;
						msg.sendToTarget();
					}
				} else {
					for (Handler handler : handlers) {
						msg = Message.obtain(handler,
								Constants.DOWNLOAD_SUCCESS);
						msg.obj = downloadInformation;
						msg.sendToTarget();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				for (Handler handler : handlers) {
					msg = Message.obtain(handler, Constants.CONNECTION_ERROR);
					msg.obj = downloadInformation;
					msg.sendToTarget();
				}
			}
		}

	}
}