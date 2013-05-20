package com.zhan_dui.dictionary.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 配置类，主要处理Preference配置
 * 
 * @author xuanqinanhai
 * 
 */
public class Config {
	private static Context context;
	public static final String PREFER_NAME = "dictionary_prefer";

	public Config(Context context) {
		Config.context = context;
	}

	public void setPreference(String key, Object value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PREFER_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof String) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Boolean) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Long) {
			editor.putInt(key, (Integer) value);
		}
		editor.commit();
	}

	public int getIntPreference(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				PREFER_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getInt(key, 0);
	}

	public int addDictionary(String CN_Name, String SaveName) {
		int currentCount = getIntPreference("dictionary_count");
		setPreference("dictionary_count", currentCount + 1);
		setPreference("dic-" + currentCount + 1, CN_Name);
		setPreference("dic-confi-" + currentCount + 1, SaveName);
		return currentCount;
	}
}
