package com.zhan_dui.dictionary.handlers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.runnables.DownloadRunnable.DownloadGetEveryUpdateInterface;
import com.zhan_dui.dictionary.utils.Constants;

/**
 * 下载文件包含Notification的Handler
 * 
 * @author xuanqinanhai
 * 
 */
public class DownloadNotificationHandler extends Handler implements
		DownloadGetEveryUpdateInterface {

	private NotificationManager notificationManager;
	private Notification notification;
	private PendingIntent pendingIntent;
	private int notificationID;
	private Context context;

	@SuppressWarnings("deprecation")
	public DownloadNotificationHandler(Context context, int iconResID,
			int flag, String tickerText, PendingIntent pendingIntent,
			int notificationID) {
		this.context = context;
		this.pendingIntent = pendingIntent;
		this.notificationID = notificationID;
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(iconResID, tickerText,
				System.currentTimeMillis());
		notification.setLatestEventInfo(context, tickerText, tickerText,
				pendingIntent);
		notificationManager.notify(notificationID, notification);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == Constants.DOWNLOADING) {
			float size = msg.arg1 / 1024;
			if (size < 1) {
				notification.setLatestEventInfo(context,
						notification.tickerText, "已经下载:" + msg.arg1 + "KB",
						pendingIntent);
			} else {
				notification.setLatestEventInfo(context,
						notification.tickerText,
						"已经下载:" + String.format("%.2f", msg.arg1 / 1024f)
								+ "MB", pendingIntent);
			}
			notificationManager.notify(notificationID, notification);
		} else if (msg.what == Constants.CONNECTION_ERROR) {
			Toast.makeText(context,
					context.getString(R.string.connection_error),
					Toast.LENGTH_LONG).show();
			notificationManager.cancel(notificationID);
		} else if (msg.what == Constants.FILE_CREATE_ERROR) {
			Toast.makeText(context,
					context.getString(R.string.file_create_error),
					Toast.LENGTH_LONG).show();
			notificationManager.cancel(notificationID);

		} else if (msg.what == Constants.DOWNLOAD_SUCCESS) {
			notificationManager.cancel(notificationID);
		} else if (msg.what == Constants.DOWNLOAD_CANCEL) {
			notificationManager.cancel(notificationID);
		}
	}

}