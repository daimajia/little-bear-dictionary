package com.zhan_dui.dictionary;

import java.io.File;

import org.holoeverywhere.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.zhan_dui.dictionary.fragments.AboutMeFragment;
import com.zhan_dui.dictionary.fragments.DictionaryManageFragment;
import com.zhan_dui.dictionary.fragments.QueryWordFragment;
import com.zhan_dui.dictionary.fragments.SimpleWordsFragment;
import com.zhan_dui.dictionary.listeners.StartMovingBaseDictionaryListener;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.ThemeUtils;

public class GeneralActivity extends SlidingFragmentActivity implements
		OnClickListener, OnClosedListener {

	public static SlidingMenu mSlidingMenu;
	private View mMenuView;
	private Button mSearch, mAbout, mOfflineManage, mSetting, mRate, mWords;
	private int mClickMenuID = 0;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		ThemeUtils.onActivityCreateSetTheme(this);

		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		setBehindContentView(R.layout.sliding_menu);
		setContentView(R.layout.activity_main);

		mSlidingMenu = getSlidingMenu();
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.setFadeDegree(0.35f);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mMenuView = mSlidingMenu.getMenu();

		// start finding ui widget
		mSearch = (Button) mMenuView.findViewById(R.id.search);
		mSetting = (Button) mMenuView.findViewById(R.id.setting);
		mOfflineManage = (Button) mMenuView.findViewById(R.id.offline_manage);
		mSetting = (Button) mMenuView.findViewById(R.id.setting);
		mAbout = (Button) mMenuView.findViewById(R.id.about);
		mRate = (Button) mMenuView.findViewById(R.id.star);
		mWords = (Button) mMenuView.findViewById(R.id.words);
		mSetting.setOnClickListener(this);
		mOfflineManage.setOnClickListener(this);
		mSearch.setOnClickListener(this);
		mAbout.setOnClickListener(this);
		mRate.setOnClickListener(this);
		mAbout.setOnClickListener(this);
		mWords.setOnClickListener(this);
		mSlidingMenu.setOnClosedListener(this);
		// SlidingMenu init;

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, new QueryWordFragment(), mTag)
				.commit();
		mCurrentShowID = R.id.search;

		if (isBaseDictionaryExist() == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.tips);
			builder.setMessage(R.string.init);
			builder.setPositiveButton(R.string.init_start,
					new StartMovingBaseDictionaryListener(mContext));
			builder.setNegativeButton(R.string.init_not_now,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							GeneralActivity.this.finish();
						}
					});
			builder.show();
		}
		UMFeedbackService.enableNewReplyNotification(this,
				NotificationType.AlertDialog);
		Boolean first = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(Constants.PREFER_FIRST, true);
		if (first) {
			Intent intent = new Intent(this, GuideActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.setting) {
			mSlidingMenu.toggle(false);
			Intent intent = new Intent(mContext, SettingActivity.class);
			mContext.startActivity(intent);
			overridePendingTransition(R.anim.anim_window_in,
					R.anim.anim_window_out_solid);
			return;
		}
		mSlidingMenu.toggle();
		mClickMenuID = v.getId();
	}

	private int mCurrentShowID;
	private final String mTag = "currentFragment";

	@Override
	public void onClosed() {
		if (mClickMenuID == mCurrentShowID)
			return;
		removeCurrentFragment();

		mCurrentShowID = mClickMenuID;
		switch (mClickMenuID) {
		case R.id.offline_manage:
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.content_frame,
							new DictionaryManageFragment(), mTag).commit();
			break;
		case R.id.search:
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, new QueryWordFragment(), mTag)
					.commit();
			break;
		case R.id.about:
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, new AboutMeFragment(), mTag)
					.commit();
			break;
		case R.id.words:
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.content_frame, new SimpleWordsFragment(),
							mTag).commit();
		default:
			break;
		}
	}

	public int getCurrentFragmentID() {
		return mCurrentShowID;
	}

	@SuppressLint("Recycle")
	private void removeCurrentFragment() {
		Fragment currentFragment = getSupportFragmentManager()
				.findFragmentByTag(mTag);
		if (currentFragment != null) {
			getSupportFragmentManager().beginTransaction().remove(
					currentFragment);
			mCurrentShowID = 0;
		}
	}

	private void removerCurrentAndReplaceFragment(int id, Fragment fragment,
			boolean force) {
		if (id == mCurrentShowID && !force) {
			return;
		}
		removeCurrentFragment();
		setFragment(id, fragment);
	}

	public void setFragment(int id, Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment, mTag).commit();
		mCurrentShowID = id;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		SearchView searchView = null;
		switch (keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			if (mSlidingMenu.isMenuShowing()) {
				mSlidingMenu.toggle();
			}
			if (mCurrentShowID != R.id.search) {
				removerCurrentAndReplaceFragment(R.id.search,
						new QueryWordFragment(), false);
			}
			searchView = (SearchView) findViewById(android.R.id.inputArea);
			if (searchView != null)
				searchView.setIconified(false);
			return true;
		case KeyEvent.KEYCODE_MENU:
			mSlidingMenu.toggle();
			break;
		case KeyEvent.KEYCODE_BACK:
			if (mSlidingMenu.isMenuShowing()) {
				mSlidingMenu.toggle();
				return true;
			}

			searchView = (SearchView) findViewById(android.R.id.inputArea);
			if (searchView != null && searchView.isIconified() == false) {
				searchView.clearFocus();
				searchView.setIconified(true);
				return true;
			}

			boolean askQuite = PreferenceManager.getDefaultSharedPreferences(
					mContext).getBoolean("quitetip", true);
			if (askQuite == true) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.tips);
				builder.setMessage(R.string.quite_tips);

				builder.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				builder.setNeutralButton(R.string.give_suggestion,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								UMFeedbackService
										.openUmengFeedbackSDK(GeneralActivity.this);
							}
						});
				builder.setNegativeButton(R.string.cancel, null);
				builder.show();
			}
			break;
		default:
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SettingActivity.sChanged) {
			finish();
			startActivity(new Intent(this, GeneralActivity.class));
			SettingActivity.sChanged = false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mSlidingMenu.toggle();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 检查基础词典是否存在
	 * 
	 * @return
	 */
	public boolean isBaseDictionaryExist() {
		String file_path = Constants.getSDBaseDictionaryPath();
		File base_file = new File(file_path);
		return base_file.exists();
	}
}
