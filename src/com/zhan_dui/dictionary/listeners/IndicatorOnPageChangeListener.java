package com.zhan_dui.dictionary.listeners;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.slidingmenu.lib.SlidingMenu;
import com.zhan_dui.dictionary.GeneralActivity;

/**
 * 这个监听器是用来让SlidingBar和Indicator工作起来的
 * 
 * @author xuanqinanhai
 * 
 */
public class IndicatorOnPageChangeListener implements OnPageChangeListener {

	public IndicatorOnPageChangeListener() {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {

		switch (position) {
		case 0:
			Log.i("Position", position + "");
			GeneralActivity.mSlidingMenu
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			Log.i("info:", GeneralActivity.mSlidingMenu.getTouchModeAbove()
					+ "");
			break;
		default:
			Log.i("Position", position + "");
			GeneralActivity.mSlidingMenu
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			Log.i("info:", GeneralActivity.mSlidingMenu.getTouchModeAbove()
					+ "");
			break;
		}
	}
}
