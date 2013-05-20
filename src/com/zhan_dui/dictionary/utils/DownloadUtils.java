package com.zhan_dui.dictionary.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ProgressBar;

import com.zhan_dui.dictionary.exceptions.SDCardUnmountedException;

/**
 * 下载的工具类
 * 
 * @author xuanqinanhai
 * 
 */

public class DownloadUtils {
	private DownloadUtils() {

	}

	/**
	 * 这是DownloadUtils的工具函数,分别处理下载开始，下载出错，下载异常三种情况
	 * 
	 * @author xuanqinanhai
	 * 
	 */
	public interface DownloadUtilsInterface {
		/**
		 * 在执行AsyncTask excute之前执行
		 */
		public boolean beforeDownload(String url);

		/**
		 * 线程执行开始前的行为
		 * 
		 * @param url
		 */
		public void beforeThread(String url);

		/**
		 * 下载进度更新，共更新100次
		 * 
		 * @param url
		 *            下载的文件地址
		 * @param fileDownladed
		 *            文件已经下载的大小
		 * @param fileSize
		 *            文件总大小
		 */
		public void update(String url, int fileDownladed, int fileSize);

		/**
		 * 下载完成时执行(在线程里..可以开一个线程做一些解压、重命名、移动等操作)
		 * 
		 * @param result
		 *            返回下载的结果
		 */
		public void afterThread(Boolean result, String url, String filePath);

		/**
		 * 线程执行即将结束的行为
		 * 
		 * @param result
		 *            下载文件是否成功
		 * @param url
		 *            文件URL
		 * @param filePath
		 *            文件保存的地址
		 */

		public void afterDownload(Boolean result, String url, String savePath);

		/**
		 * 出错执行函数，errorMsg代表错误解释,可以直接输出
		 * 
		 * @param errorMsg
		 */
		public void errorHand(String errorMsg, String url);

		/**
		 * 在线程下载时候调用，注意，不要执行只能在UI线程上执行的行为,最好函数体越简单越好，这样就对下载速度的影响变小
		 * 
		 * @param url
		 *            要终止下载的文件URL
		 * @return 返回是否终止下载
		 */
		public boolean isCanceled(String url);

		/**
		 * 终止一个下载
		 * 
		 * @param url
		 *            要终止下载的链接
		 */
		public void cancel(String url);
	}

	public static final String ERROR_CREATE_FILE = "创建文件错误";
	public static final String ERROR_IO = "文件读写异常";
	public static final String ERROR_WRONG_URL = "错误的URL格式";
	public static final String ERROR_OPEN_URL = "打开URL连接出错";
	public static final String ERROR_SD_CARD = "未检测到SD卡";
	public static final String ERROR_CANCEL_DOWNLOAD = "您终止了下载";

	private static class DownloadAsync extends
			AsyncTask<Void, Integer, Boolean> {

		String fileUrl, savePath;
		DownloadUtils.DownloadUtilsInterface downloadBehavior;
		String errorMsg;
		int size = 0;// 文件大小
		double downloaded = 0.0;
		NotificationManager notificationManager;
		Notification notification;
		int progressbarId;
		int notificationId;
		boolean isUpdateNotification = false;
		ProgressBar progressBar;

		public DownloadAsync(String fileUrl, String savePath,
				DownloadUtilsInterface downloadBehavior, ProgressBar progressBar) {
			this.fileUrl = fileUrl;
			this.savePath = savePath;
			this.downloadBehavior = downloadBehavior;
			this.progressBar = progressBar;
		}

		public DownloadAsync(String fileUrl, String savePath,
				DownloadUtilsInterface downloadBehavior,
				NotificationManager notificationManager,
				Notification notification, int progressbarId, int notificationId) {
			this.fileUrl = fileUrl;
			this.savePath = savePath;
			this.downloadBehavior = downloadBehavior;
			this.notification = notification;
			this.progressbarId = progressbarId;
			this.isUpdateNotification = true;
			this.notificationId = notificationId;
			this.notificationManager = notificationManager;
		}

		protected void setFileSize(int size) {
			this.size = size;
			if (!isUpdateNotification) {
				progressBar.setMax(size);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (downloadBehavior.beforeDownload(this.fileUrl) == false) {
				downloadBehavior.cancel(this.fileUrl);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (isUpdateNotification) {
				notification.contentView.setProgressBar(progressbarId, 100,
						values[0], false);
				this.notificationManager.notify(notificationId, notification);
				downloadBehavior.update(fileUrl, values[1], values[2]);
			} else {
				progressBar.setProgress(values[0]);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result)
				downloadBehavior.errorHand(errorMsg, this.fileUrl);
			downloadBehavior.afterDownload(result, this.fileUrl, this.savePath);
			if (isUpdateNotification) {
				notificationManager.cancel(notificationId);
			}
		}

		@Override
		/**
		 * 后台线程下载
		 */
		protected Boolean doInBackground(Void... params) {
			if (downloadBehavior.isCanceled(fileUrl)) {
				errorMsg = ERROR_CANCEL_DOWNLOAD;
				return false;
			}

			downloadBehavior.beforeThread(this.fileUrl);
			File file = new File(savePath);
			URL url = null;

			InputStream fileInputStream = null;
			HttpURLConnection httpURLConnection = null;
			FileOutputStream fileOutputStream = null;
			BufferedOutputStream bufferedOutputStream = null;
			try {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					throw new SDCardUnmountedException();

				}
				if (file.exists() == false) {
					if (file.getParentFile().exists() == false) {
						file.getParentFile().mkdirs();
					}
					file.createNewFile();
				}
				url = new URL(fileUrl);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setInstanceFollowRedirects(true);
				fileInputStream = httpURLConnection.getInputStream();
				setFileSize(httpURLConnection.getContentLength());
				fileOutputStream = new FileOutputStream(file);
				bufferedOutputStream = new BufferedOutputStream(
						fileOutputStream);
				byte[] dataBuffer = new byte[1024];
				int length = 0;
				int downloadedPercentage;
				int everyPieceSize = size / 100;
				int oneTimeDownloadPieceSize = 0;
				while ((length = fileInputStream.read(dataBuffer)) > 0) {
					bufferedOutputStream.write(dataBuffer, 0, length);

					oneTimeDownloadPieceSize += length;

					if (oneTimeDownloadPieceSize > everyPieceSize) {
						downloaded += oneTimeDownloadPieceSize;
						downloadedPercentage = (int) ((downloaded / size) * 100);
						publishProgress(downloadedPercentage, (int) downloaded,
								size);
						oneTimeDownloadPieceSize = 0;
					}

					if (downloadBehavior.isCanceled(fileUrl)) {
						errorMsg = ERROR_CANCEL_DOWNLOAD;
						break;
					}
				}
				publishProgress(100, size, size);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				errorMsg = ERROR_WRONG_URL;
			} catch (SDCardUnmountedException e) {
				e.printStackTrace();
				errorMsg = ERROR_SD_CARD;
			} catch (IOException e) {
				e.printStackTrace();
				errorMsg = ERROR_IO;
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						errorMsg = ERROR_IO;
					}
				}
				if (bufferedOutputStream != null) {
					try {
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						errorMsg = ERROR_IO;
					}
				}

			}
			downloadBehavior.afterThread(errorMsg == null, fileUrl, savePath);
			return errorMsg == null;
		}
	}

	/**
	 * 下载文件，更新进度条
	 * 
	 * @param fileUrl
	 *            文件网络地址
	 * @param savePath
	 *            保存的地址
	 * @param progressBar
	 *            进度条对象，用来更新
	 * @param downlaodInterface
	 *            下载行为接口，分别会在下载前，下载时，下载后发送消息
	 */
	public static void download(String fileUrl, String savePath,
			ProgressBar progressBar, DownloadUtilsInterface downlaodInterface) {
		new DownloadAsync(fileUrl, savePath, downlaodInterface, progressBar)
				.execute();
	}

	/**
	 * 下载文件并且通过notification更新
	 * 
	 * @param fileUrl
	 *            文件地址
	 * @param savePath
	 *            保存地址
	 * @param downloadBehavior
	 *            下载行为接口，分别会在下载前，下载时，下载后发送消息
	 * @param notification
	 *            通知栏对象，用来更新通知栏下载进度
	 * @param progressbarId
	 *            通知栏中进度条ID
	 * 
	 */
	public static void download(String fileUrl, String savePath,
			DownloadUtilsInterface downloadBehavior,
			NotificationManager notificationManager, Notification notification,
			int progressbarId, int notificationId) {
		new DownloadAsync(fileUrl, savePath, downloadBehavior,
				notificationManager, notification, progressbarId,
				notificationId).execute();
	}
}
