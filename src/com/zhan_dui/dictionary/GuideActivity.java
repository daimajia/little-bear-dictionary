package com.zhan_dui.dictionary;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class GuideActivity extends Activity {
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.guide);
		Animation leftToRight = new TranslateAnimation(0.0f, 150.0f, 0.0f, 0.0f);
		leftToRight.setDuration(1000);
		leftToRight.setRepeatCount(3);
		findViewById(R.id.hand).setAnimation(leftToRight);
		TextView tips = (TextView) findViewById(R.id.tips);
		tips.setText(R.string.swipe_show_menu);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(Constants.PREFER_FIRST, false);
		mEditor.commit();
		finish();
		return super.onTouchEvent(event);
	}
}
