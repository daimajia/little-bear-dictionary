package com.zhan_dui.dictionary.listeners;

import com.zhan_dui.dictionary.cursoradapters.OnlineListCursorAdapter;

import android.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 下载文件时候的取消监听器
 */
public class DownloadDictionaryCancelListener implements OnClickListener {

	private String toCancelUrl;
	private CursorAdapter cursorAdapter;

	public DownloadDictionaryCancelListener(String url,
			CursorAdapter cursorAdapter) {
		this.toCancelUrl = url;
		this.cursorAdapter = cursorAdapter;
	}

	@Override
	public void onClick(View v) {
		OnlineListCursorAdapter.downloadingNotificationUrls.remove(v
				.getContentDescription());
		cursorAdapter.notifyDataSetChanged();
		OnlineListCursorAdapter.downloadingNotificationCancels
				.add(this.toCancelUrl);
	}

}
