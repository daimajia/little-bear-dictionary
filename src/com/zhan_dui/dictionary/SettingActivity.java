package com.zhan_dui.dictionary;

import org.holoeverywhere.preference.PreferenceActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;
import com.zhan_dui.dictionary.datacenter.QueryProcessor;
import com.zhan_dui.dictionary.utils.ThemeUtils;

public class SettingActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static boolean sChanged = false;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ThemeUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting_preference);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.setting_smallbear);
	}

	protected void onDestroy() {
		super.onDestroy();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// 做下面的只是为了流畅，之所以要存储起来，是为了更快的查询
				QueryProcessor.updateFromPreference(getApplicationContext());
			}
		};
		new Thread(runnable).start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("select_theme")) {
			String select_theme = sharedPreferences.getString(key,
					"light_theme");
			if (select_theme.equals("light_theme"))
				ThemeUtils.changeToTheme(this, ThemeUtils.THEME_WHITE);
			else if (select_theme.equals("dark_theme")) {
				ThemeUtils.changeToTheme(this, ThemeUtils.THEME_DARK);
			}
			sChanged = true;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
}
