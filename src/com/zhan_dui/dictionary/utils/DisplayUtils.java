package com.zhan_dui.dictionary.utils;

import android.content.Context;

/**
 * 像素和dpi转换工具
 * 
 * @author xuanqinanhai
 * 
 */
public class DisplayUtils {
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
