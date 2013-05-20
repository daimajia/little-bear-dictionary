package com.zhan_dui.dictionary.fileimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.utils.UnzipUtils;
import com.zhan_dui.dictionary.utils.UnzipUtils.UnzipInterface;

/**
 * 负责管理所有的解压,维护在解压时启动的Notification 用法：先创建一个UnzipNotificationCenter
 * ，传入参数为点击Notification时启动的Class,而后调用PrepareNotification,返回当前存入的Notification的ID。
 * 最后startUnzip 传入一个NotificationID
 * 
 * @date 2012-11-29 上午9:23:52
 */
public class UnzipNotificationCenter {

	private static HashMap<String, Integer> unzippingMap = new HashMap<String, Integer>();
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Notification> unzippingNotificationMap = new HashMap<Integer, Notification>();

	private static int idCounter = 0;
	private Context context;
	private NotificationManager notificationManager;
	private PendingIntent pendingIntent;
	private UnzipUtils unzipUtils;

	/**
	 * ‘解压通知栏中心’构造函数
	 * 
	 * @param context
	 *            当前的上下文
	 * @param cls
	 *            点击通知栏后启动的类
	 */
	public UnzipNotificationCenter(Context context, Class<?> cls) {
		this.context = context;

		this.pendingIntent = PendingIntent.getActivity(context, 0, new Intent(
				context, cls), Intent.FLAG_ACTIVITY_CLEAR_TOP);

		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		unzipUtils = new UnzipUtils();
	}

	/**
	 * prepareUnzipNotification 准备一个Notification
	 * 
	 * @param icon
	 *            通知栏图标
	 * @param tickerText
	 *            通知栏tikerText
	 * @param contentTitle
	 *            通知栏title
	 * @param content
	 *            通知栏内容
	 * @return int 返回创建的这个notification的id 下一步通过id来启动一个解压工作
	 */
	@SuppressWarnings("deprecation")
	public int prepareUnzipNotification(int icon, String tickerText,
			String contentTitle, String content) {
		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(context, contentTitle, content,
				pendingIntent);
		unzippingNotificationMap.put(idCounter, notification);
		return idCounter++;
	}

	public int prepareUnzipNotification(int icon, int tickerTextResId,
			int contentTitleResId, int contentResId) {
		return prepareUnzipNotification(icon,
				context.getString(tickerTextResId),
				context.getString(contentTitleResId),
				context.getString(contentTitleResId));
	}

	/**
	 * startUnzip 启动一个下载工作
	 * 
	 * @param notificationId
	 *            通过prepareUnzipNotification创建的Notication的id
	 * @param source
	 *            解压文件地址
	 * @param outputDirectory
	 *            输出文件夹
	 * @param rewrite
	 *            是否覆盖
	 */
	public void startUnzip(int notificationId, String source,
			String outputDirectory, Boolean rewrite) {
		if (unzippingNotificationMap.containsKey(notificationId) == false) {
			return;
		} else {
			if (unzippingMap.containsKey(source)) {// 检测是否是重复文件下载
				Toast.makeText(context, R.string.unzip_already,
						Toast.LENGTH_SHORT).show();
				unzippingNotificationMap.remove(notificationId);
				return;
			}
			notificationManager.notify(notificationId,
					unzippingNotificationMap.get(notificationId));
			unzippingMap.put(source, notificationId);
			UnzipUtils.unzipFile(unzipBehavior, source, outputDirectory,
					rewrite);
		}
	}

	private UnzipUtils.UnzipInterface unzipBehavior = new UnzipInterface() {

		@Override
		public boolean beforeUnzip(String source, String outputDirectory) {
			int lastSep = source.lastIndexOf("/");
			String fileName = source.substring(lastSep + 1, source.length());
			// 更新notification，将通知改为 开始解压
			String title = context.getString(R.string.start_unzip) + fileName;
			String content = context.getString(R.string.unzip_tip);
			updateNotification(source, title, content);
			return true;
		}

		@Override
		public void afterUnzip(Boolean result, String source,
				String outputDirectory) {
			// 更新notification，将通知改为 解压结束
			updateNotification(source, R.string.unzip_finish,
					R.string.unzip_tip);
			// 并且从存储列表里删除
			removeFromListsByPath(source);
		}

		@Override
		public void beforeUnzipThread(String source, String outputDirectory) {
		}

		/**
		 * 解压线程即将结束时执行的内容
		 */
		@Override
		public void afterUnzipThread(Boolean result, String source,
				String outputDirectory) {
			// 解析解压后的数据，并且添加到数据库中
			updateNotification(source, R.string.unzip_finish_start_deal,
					R.string.still_wait);
			// 开始往数据库中导入该字典的信息
			File currentFile = new File(source);
			String fileName = currentFile.getName();
			String onlyName = fileName.substring(0, fileName.lastIndexOf("."));
			File importFile = new File(outputDirectory + File.separator
					+ onlyName + File.separator + "import-" + onlyName + ".dic");
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(
						new FileInputStream(importFile));
				@SuppressWarnings("resource")
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String queryString = bufferedReader.readLine();
				DictionaryDB dictionaryDB = new DictionaryDB(context,
						DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
				SQLiteDatabase sqLiteDatabase = dictionaryDB
						.getWritableDatabase();
				sqLiteDatabase.execSQL(queryString);
				sqLiteDatabase.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 出错处理
		 */
		@Override
		public void errorOccur(String errorMsg, String source,
				String outputDirectory) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
		}

	};

	/**
	 * updateNotification 更新notification
	 * 
	 * @param source
	 *            源文件地址，用来检索已经存储的notification
	 * @param title
	 *            更新notification title
	 * @param content
	 *            更新notification content
	 */
	@SuppressWarnings("deprecation")
	private void updateNotification(String source, String title, String content) {
		Notification notification = getNotificationByPath(source);
		if (notification == null)
			return;
		notification.setLatestEventInfo(context, title, content, pendingIntent);
		notificationManager.notify(unzippingMap.get(source), notification);
	}

	/**
	 * updateNotification 更新Notification
	 * 
	 * @param source
	 *            源文件地址
	 * @param strTitleRes
	 *            更新title的资源ID
	 * @param strContentRes
	 *            更新内容的资源ID
	 */
	private void updateNotification(String source, int strTitleRes,
			int strContentRes) {
		updateNotification(source, context.getString(strTitleRes),
				context.getString(strContentRes));
	}

	/**
	 * getNotificationByPath 通过要解压的文件
	 * 
	 * @param source
	 *            文件地址
	 * @return 返回notification
	 */
	private Notification getNotificationByPath(String source) {
		if (unzippingMap.containsKey(source)) {
			int id = unzippingMap.get(source);
			return unzippingNotificationMap.get(id);
		} else {
			return null;
		}
	}

	private void removeFromListsByPath(String source) {
		if (unzippingMap.containsKey(source)) {
			int id = unzippingMap.get(source);
			notificationManager.cancel(id);
			unzippingNotificationMap.remove(id);
			unzippingMap.remove(source);
		}
	}
}
