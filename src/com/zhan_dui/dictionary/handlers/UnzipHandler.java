package com.zhan_dui.dictionary.handlers;

import java.text.NumberFormat;

import org.holoeverywhere.app.ProgressDialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.utils.Constants;
/**
 * @Description:解压文件时候的显示数据handler
 * @date 2012-11-9 上午11:21:01
 */
public class UnzipHandler extends Handler {

	private Context context;
	private ProgressDialog progressDialog = null;
	private NumberFormat mNumberFormat;

	public UnzipHandler(Context context) {
		this.context = context;
		mNumberFormat = NumberFormat.getNumberInstance();
		mNumberFormat.setMaximumFractionDigits(2);
	}

	/**
	 * what represent the current state arg1 represent the current progress arg2
	 * represent the total size of the unzip
	 */

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == Constants.UNZIPPING) {
			double progress = msg.arg1 / 1024;
			String output = progress > 1024 ? mNumberFormat.format(progress / 1024.0)
					+ "MB" : progress + "KB";
			progressDialog
					.setMessage(context.getString(R.string.moving_base_data)
							+ context.getString(R.string.moving_finished_size)
							+ output);
		} else if (msg.what == Constants.UNZIP_START) {
			progressDialog = new ProgressDialog(context);
			progressDialog.show();
			progressDialog.setTitle(R.string.moveing_base_data_title);
		} else if (msg.what == Constants.UNZIP_ERROR) {
			Toast.makeText(context, R.string.moving_base_data_error,
					Toast.LENGTH_LONG).show();
			progressDialog.dismiss();
		} else if (msg.what == Constants.UNZIP_FINISH) {
			progressDialog.dismiss();
		}
	}
}
