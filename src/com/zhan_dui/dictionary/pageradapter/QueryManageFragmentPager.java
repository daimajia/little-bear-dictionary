package com.zhan_dui.dictionary.pageradapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.zhan_dui.dictionary.datacenter.DictionaryDataCenter;

public class QueryManageFragmentPager extends PagerAdapter {

	private DictionaryDataCenter mDataCenter;
	private Context mContext;

	public QueryManageFragmentPager(Context context) {
		mContext = context;
	}

	public QueryManageFragmentPager() {
		mDataCenter = DictionaryDataCenter.instance(mContext);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = mDataCenter.getDictionaryViewByPosition(position);
		container.addView(view);
		return view;
	}

	@Override
	public int getCount() {
		return mDataCenter.getDictionaryViewCount();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mDataCenter.getPagerTitle(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}

}
