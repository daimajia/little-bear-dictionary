package com.zhan_dui.dictionary.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.AsyncTask;

/**
 * 解压缩文件工具类
 * 
 * @author xuanqinanhai
 * 
 */
public class UnzipUtils {

	public interface UnzipInterface {
		/**
		 * beforeUnzip 在Unzip之前做的事情
		 * 
		 * @param source
		 *            源文件地址
		 * @param outputDirectory
		 *            带解压目录
		 */
		public boolean beforeUnzip(String source, String outputDirectory);

		/**
		 * afterUnzip unzip结束时执行的函数 UI线程
		 * 
		 * @param result
		 *            解压结果 true为成功，false为失败
		 * @param source
		 *            源文件地址
		 * @param outputDirectory
		 *            解压目标文件夹地址
		 */
		public void afterUnzip(Boolean result, String source,
				String outputDirectory);

		/**
		 * errorOccur 出错时调用该函数 UI线程
		 * 
		 * @param errorMsg
		 *            错误信息
		 * @param source
		 *            源文件地址
		 * @param outputDirectory
		 *            解压目标文件夹地址
		 */

		/**
		 * beforeUnzipThread 线程开始前 线程级别，不要做UI操作
		 */
		public void beforeUnzipThread(String source, String outputDirectory);

		/**
		 * afterUnzipThread 线程结束前 线程级别，不要做UI操作
		 * 
		 * @param result
		 *            是否出错
		 * @param source
		 *            源地址
		 * @param outputDirectory
		 *            解压目标地址
		 */
		public void afterUnzipThread(Boolean result, String source,
				String outputDirectory);

		public void errorOccur(String errorMsg, String source,
				String outputDirectory);
		/**
		 * onZipStart 在解压开始时调用 UI线程
		 * 
		 * @param source
		 * @param outputDirectory
		 */

	}

	public static final String CREATE_DIRECTORY_ERROR = "无法创建解压文件夹";
	public static final String FILE_NOT_EXSIT = "待解压的文件不存在";
	public static final String ZIP_POINT_ERROR = "目标文件地址不是压缩文件或者已经被损坏";
	public static final String ZIP_OUTPUTSTREAM_ERROR = "无法创建要解压的目标文件";
	public static final String FILESTREAM_CANNOT_CLOSE = "文件流无法关闭";
	public static final String ZIP_NEXT_POINT_ERROR = "无法定位到下一个解压点";
	public static final String ABORT = "已经被用户取消";
	public static final String BEFORE_TASK_STOP = "before函数结束了解压";

	public static void unzipFile(UnzipInterface unzipBehavior, String source,
			String outputDirectory, Boolean rewrite) {
		new UnzipTask(source, outputDirectory, unzipBehavior, rewrite)
				.execute();
	}

	private static class UnzipTask extends AsyncTask<Void, Integer, Boolean> {

		private String source;
		private String outputDirectory;
		private UnzipInterface unzipBehavior;
		private Boolean rewrite;
		private String errorMsg = null;
		private boolean stop = false;

		public UnzipTask(String source, String outputDirectory,
				UnzipInterface unzipBehavior, Boolean rewrite) {
			super();
			this.source = source;
			this.outputDirectory = outputDirectory;
			this.unzipBehavior = unzipBehavior;
			this.rewrite = rewrite;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 如果before的工作返回false则不开始解压
			stop = !unzipBehavior.beforeUnzip(source, outputDirectory);
		}

		@SuppressWarnings("resource")
		@Override
		protected Boolean doInBackground(Void... params) {

			if (stop) {
				errorMsg = BEFORE_TASK_STOP;
				return false;
			}

			unzipBehavior.beforeUnzipThread(source, outputDirectory);
			// 创建解压目标目录

			File file = new File(outputDirectory);
			// 如果目标目录不存在，则创建
			if (!file.exists()) {
				if (file.mkdirs() == false) {
					errorMsg = CREATE_DIRECTORY_ERROR;
					unzipBehavior.afterUnzipThread(false, source,
							outputDirectory);
					return false;
				}
			}
			InputStream inputStream;
			try {
				inputStream = new FileInputStream(new File(source));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				errorMsg = FILE_NOT_EXSIT;
				unzipBehavior.afterUnzipThread(false, source, outputDirectory);
				return false;
			}
			// 打开压缩文件
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);

			ZipEntry zipEntry = null;
			try {
				zipEntry = zipInputStream.getNextEntry();
			} catch (IOException e) {
				e.printStackTrace();
				errorMsg = ZIP_POINT_ERROR;
				unzipBehavior.afterUnzipThread(false, source, outputDirectory);
				return false;
			}
			byte[] buffer = new byte[1024];
			// 解压时字节计数
			int count = 0;
			// 如果进入点为空说明已经遍历完所有压缩包中文件和目录
			while (zipEntry != null) {
				// 如果是一个目录
				if (zipEntry.isDirectory()) {
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// 文件需要覆盖或者是文件不存在
					if (rewrite || !file.exists()) {
						file.mkdir();
					}
				} else {
					// 如果是文件
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// 文件需要覆盖或者文件不存在，则解压文件
					FileOutputStream fileOutputStream = null;
					if (rewrite || !file.exists()) {

						try {
							file.createNewFile();
							fileOutputStream = new FileOutputStream(file);
							while ((count = zipInputStream.read(buffer)) > 0) {
								fileOutputStream.write(buffer, 0, count);
								if (stop) {
									errorMsg = ABORT;
									return false;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							errorMsg = ZIP_OUTPUTSTREAM_ERROR;
							unzipBehavior.afterUnzipThread(false, source,
									outputDirectory);
							return false;
						}
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							errorMsg = FILESTREAM_CANNOT_CLOSE;
							unzipBehavior.afterUnzipThread(false, source,
									outputDirectory);
							return false;
						}
					}
				}
				// 定位到下一个文件入口
				try {
					zipEntry = zipInputStream.getNextEntry();
				} catch (IOException e) {
					e.printStackTrace();
					errorMsg = ZIP_NEXT_POINT_ERROR;
					unzipBehavior.afterUnzipThread(false, source,
							outputDirectory);
					return false;
				}
			}
			unzipBehavior.afterUnzipThread(true, source, outputDirectory);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result == false) {
				unzipBehavior.errorOccur(errorMsg, source, outputDirectory);
			}
			unzipBehavior.afterUnzip(result, source, outputDirectory);
		}
	}
}
