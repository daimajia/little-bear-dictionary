package com.zhan_dui.dictionary.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zhan_dui.dictionary.R;

public class ThemeUtils {
	private static int mTheme;

	public final static int THEME_DARK = 1;
	public final static int THEME_WHITE = 2;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme) {
		mTheme = theme;
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity) {

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(activity);
		String select_theme = sharedPreferences.getString("select_theme",
				"light_theme");

		if (select_theme.equals("light_theme")) {
			mTheme = 2;
		} else if (select_theme.equals("dark_theme")) {
			mTheme = 1;
		}

		switch (mTheme) {
		default:
		case THEME_WHITE:
			activity.setTheme(R.style.Dic_Theme_Light);
			break;
		case THEME_DARK:
			activity.setTheme(R.style.Dic_Theme_Dark);
			break;
		}
	}
}
