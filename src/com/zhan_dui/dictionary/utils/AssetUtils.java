package com.zhan_dui.dictionary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;

/**
 * 用来挪动asset的文件到SD卡
 * 
 * @author xuanqinanhai
 * 
 */
public class AssetUtils {
	private AssetUtils() {

	}

	public interface AssetCopyInterface {
		public void onCopyStart(String assetFilePath, String copyDirecotry,
				String newFileName);

		public void onUpdate(int current, int total);

		public void onFinish(Boolean result, String assetFilePath,
				String copyDirecotry, String newFileName);

		public void onError(String assetFilePath, String copyDirecotry,
				String newFileName, String errorMsg);
	}

	public static void copyFromAsset(Context context, String assetFilePath,
			String copyDirecotry, String newFileName,
			AssetCopyInterface copyBehavior) {
		new MovingAsyncTask(context, assetFilePath, copyDirecotry, newFileName,
				copyBehavior).execute();
	}

	private static class MovingAsyncTask extends AsyncTask<Void, Integer, Void> {

		private Context mContext;
		private String mAssetFileName;
		private String mSavingDirectory;
		private String mNewFileName;
		private AssetCopyInterface mCopyBehavior;

		private Boolean mResult;
		private String mMessage;

		public MovingAsyncTask(Context context, String assetFileName,
				String copyDirecory, String newFileName,
				AssetCopyInterface copyBehavior) {
			this.mAssetFileName = assetFileName;
			this.mSavingDirectory = copyDirecory;
			this.mNewFileName = newFileName;
			this.mCopyBehavior = copyBehavior;
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mCopyBehavior.onCopyStart(this.mAssetFileName,
					this.mSavingDirectory, this.mNewFileName);
		}

		@Override
		protected Void doInBackground(Void... params) {
			AssetManager manager = mContext.getAssets();
			try {
				InputStream inputStream = manager.open(mAssetFileName);
				File file = new File(mSavingDirectory);

				if (file.exists() == false) {
					file.mkdirs();
				}

				String file_path = mSavingDirectory + File.separator
						+ mNewFileName;
				File save_file = new File(file_path);
				if (save_file.exists() == false) {
					save_file.createNewFile();
				}
				OutputStream outputStream = new FileOutputStream(save_file);
				byte[] buffer = new byte[1024];
				int length = 0;
				int counter = 0;
				int size = inputStream.available();
				int piece = size / 10;
				int piece_counter = 0;
				while ((length = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, length);
					counter += length;
					piece_counter += length;
					if (piece_counter > piece) {
						Integer[] progress = { counter, size };
						publishProgress(progress);
						piece_counter = 0;
					}
				}
				Integer[] progress = { size, size };
				publishProgress(progress);
				outputStream.flush();
				outputStream.close();
				inputStream.close();
				mResult = true;
			} catch (IOException e) {
				mResult = false;
				mMessage = "文件读写错误";
				e.printStackTrace();
			} catch (Exception e) {
				mResult = false;
				mMessage = e.getMessage();
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mCopyBehavior.onUpdate(values[0], values[1]);
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mResult == false) {
				mCopyBehavior
						.onError(this.mAssetFileName, this.mSavingDirectory,
								this.mNewFileName, this.mMessage);
			}
			mCopyBehavior.onFinish(mResult, mAssetFileName, mSavingDirectory,
					mNewFileName);
		}
	}
}
