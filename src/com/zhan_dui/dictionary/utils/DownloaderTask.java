package com.zhan_dui.dictionary.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class DownloaderTask {

	private String mFileUrl;
	private String mTargetDirecory;
	private String mNewFileName;
	private int mThreadCount;
	private DownloaderTaskCallback mDownloaderTaskCallback;
	private String mDownloadStatus;
	private File mFile;

	private static final int BUFFER_SIZE = 1024;
	private int mContentSize;

	public DownloaderTask(String fileUrl, String targetDirectory,
			String newFileName, int threadCount, DownloaderTaskCallback callback) {
		this.mFileUrl = fileUrl;
		this.mNewFileName = newFileName;
		this.mDownloaderTaskCallback = callback;
		this.mTargetDirecory = targetDirectory;
		this.mThreadCount = threadCount;
	}

	public void startDownload() {
		try {
			this.mFile = getFile();
			mContentSize = getContentLength(mFileUrl);
			long subLen = mContentSize / mThreadCount;
			for (int i = 0; i < mThreadCount; i++) {
				DownloadAsyncTask downloadThread = new DownloadAsyncTask(subLen
						* i, subLen * (i + 1) - 1, i);
				downloadThread.execute();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private interface DownloaderTaskCallback {

	}

	public interface DownloaderCallback extends DownloaderTaskCallback {

	}

	public void cancel() {

	}

	public boolean isCanceled() {
		return false;
	}

	private int getContentLength(String fileUrl) throws IOException {
		URL url = new URL(fileUrl);
		URLConnection connection = url.openConnection();
		return connection.getContentLength();
	}

	private File getFile() throws IOException {
		File dir = new File(mTargetDirecory);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		String path = mTargetDirecory + File.separator + mNewFileName;
		File file = new File(path);
		file.createNewFile();
		return file;
	}

	private class DownloadAsyncTask extends AsyncTask<Void, Integer, Boolean> {

		private long mStartPos;
		private long mEndPos;
		private int mThreadID;

		private int mCurrentPos = 0;
		private boolean isNewThread;

		public DownloadAsyncTask(long startPos, long endPos, int threadID) {
			this.mStartPos = startPos;
			this.mEndPos = endPos;
			this.mThreadID = threadID;
			this.isNewThread = true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			System.out.println("Task ID:" + mThreadID + " has been started!");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			BufferedInputStream bufferedInputStream = null;
			RandomAccessFile randomAccessFile = null;
			byte[] buf = new byte[BUFFER_SIZE];
			URLConnection urlConnection = null;
			try {
				URL url = new URL(mFileUrl);
				urlConnection = url.openConnection();
				if (isNewThread) {
					urlConnection.setRequestProperty("Range", "bytes="
							+ mStartPos + "-" + mEndPos);
					randomAccessFile = new RandomAccessFile(mFile, "rw");
					randomAccessFile.seek(mStartPos);
				} else {
					urlConnection.setRequestProperty("Range", "bytes="
							+ mCurrentPos + "-" + mEndPos);
					randomAccessFile = new RandomAccessFile(mFile, "rw");
					randomAccessFile.seek(mCurrentPos);
				}
				bufferedInputStream = new BufferedInputStream(
						urlConnection.getInputStream());
				while (mCurrentPos < mEndPos) {
					int len = bufferedInputStream.read(buf, 0, BUFFER_SIZE);
					if (len == -1)
						break;
					else {
						randomAccessFile.write(buf, 0, len);
						mCurrentPos += len;
					}
				}
				bufferedInputStream.close();
				randomAccessFile.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			System.out.println("Taks ID:" + mThreadID + " has been finished!");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			System.out.println("Task ID:" + mThreadID + " is been canceled!");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://dic.zhan-dui.com/download.php?name=phrase.zip";
		DownloaderTask downloaderTask = new DownloaderTask(url,
				"/Users/xuanqinanhai/Downloads/bootstrap", "bt.zip", 10, null);
		downloaderTask.startDownload();
	}

}
