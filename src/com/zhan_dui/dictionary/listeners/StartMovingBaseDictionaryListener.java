package com.zhan_dui.dictionary.listeners;

import java.io.File;

import org.holoeverywhere.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.utils.AssetUtils;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.UnzipUtils;
import com.zhan_dui.dictionary.utils.AssetUtils.AssetCopyInterface;
import com.zhan_dui.dictionary.utils.UnzipUtils.UnzipInterface;

/**
 * 第一次启动的时候用户要移动基础词典文件
 * 
 * @author xuanqinanhai
 * 
 */
public class StartMovingBaseDictionaryListener implements OnClickListener {

	private Context mContext;
	private ProgressDialog mMovingProgressDialog;

	public StartMovingBaseDictionaryListener(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mMovingProgressDialog = new ProgressDialog(mContext);
		mMovingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mMovingProgressDialog.setTitle(R.string.move_dialog_title);
		mMovingProgressDialog.setMessage(mContext
				.getString(R.string.start_init));
		mMovingProgressDialog.setCancelable(true);
		mMovingProgressDialog.show();
		AssetUtils.copyFromAsset(mContext, Constants.BASE_DICTIONARY_ASSET,
				Constants.getSaveDirectory(), Constants.BASE_DICTIONARY_ASSET,
				MovingHandler);
	}

	private AssetCopyInterface MovingHandler = new AssetCopyInterface() {

		@Override
		public void onCopyStart(String assetFilePath, String copyDirecotry,
				String newFileName) {
			mMovingProgressDialog.setTitle(R.string.first_step_copy);
			mMovingProgressDialog.setMessage("可能要花费一些时间...");
		}

		@Override
		public void onUpdate(int current, int total) {
			mMovingProgressDialog.setMessage("可能还需要些时间...");
			mMovingProgressDialog.setMax(total);
			mMovingProgressDialog.setProgress(current);
		}

		@Override
		public void onFinish(Boolean result, String assetFilePath,
				String copyDirecotry, String newFileName) {

			if (result == false) {
				File file = new File(copyDirecotry + File.separator
						+ newFileName);
				if (file.exists()) {
					file.delete();
				}
			} else {
				mMovingProgressDialog.setMessage("拷贝结束，准备开始处理，请稍后..");
				// 开始解压文件
				UnzipUtils.unzipFile(UnzipHandler, copyDirecotry
						+ File.separator + newFileName, copyDirecotry, true);
			}
		}

		@Override
		public void onError(String assetFilePath, String copyDirecotry,
				String newFileName, String errorMsg) {
			Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
		}

	};

	private UnzipInterface UnzipHandler = new UnzipInterface() {

		@Override
		public boolean beforeUnzip(String source, String outputDirectory) {
			mMovingProgressDialog.setTitle(R.string.second_step_unzip);
			mMovingProgressDialog.setMessage("正在处理，请稍后");
			return true;
		}

		@Override
		public void afterUnzip(Boolean result, String source,
				String outputDirectory) {
			if (result) {
				Toast.makeText(mContext, "处理结束，快去字典管理中下载自己喜欢的词典吧~",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "处理失败", Toast.LENGTH_SHORT).show();
			}
			mMovingProgressDialog.dismiss();
		}

		@Override
		public void beforeUnzipThread(String source, String outputDirectory) {

		}

		@Override
		public void afterUnzipThread(Boolean result, String source,
				String outputDirectory) {

		}

		@Override
		public void errorOccur(String errorMsg, String source,
				String outputDirectory) {
			Toast.makeText(mContext, "处理出现错误了", Toast.LENGTH_SHORT).show();
		}

	};
}