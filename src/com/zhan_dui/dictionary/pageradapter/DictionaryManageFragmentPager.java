/**
 * @ClassName:OnlineDictionaryPagerAdapter.java
 */
package com.zhan_dui.dictionary.pageradapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.asynctasks.GetOfflineAsyncTask;
import com.zhan_dui.dictionary.asynctasks.GetOnlineDictionaryAsyncTask;

/**
 * @Description:字典管理页面的Pager
 */
public class DictionaryManageFragmentPager extends PagerAdapter {
	private static int[] titles = { R.string.online_support,
			R.string.offline_manage };
	private LayoutInflater mLayoutInflater;
	private Context mContext;

	public View mOnlineDictionaryManageView;
	public View mOfflineDictionaryManageView;
	

	@Override
	public int getCount() {
		return titles.length;
	}

	public DictionaryManageFragmentPager(Context context) {
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View childView = null;
		switch (position) {
		case 0:
			mOnlineDictionaryManageView = mLayoutInflater.inflate(
					R.layout.online_dictionary, null, false);
			new GetOnlineDictionaryAsyncTask(mOnlineDictionaryManageView)
					.execute();
			childView = mOnlineDictionaryManageView;
			break;
		case 1:
			mOfflineDictionaryManageView = mLayoutInflater.inflate(
					R.layout.offline_dictionary, null, false);
			new GetOfflineAsyncTask(mOfflineDictionaryManageView).execute();
			childView = mOfflineDictionaryManageView;
		default:
			break;
		}
		((ViewPager) container).addView(childView);
		return childView;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mContext.getString(titles[position]);
	}
}
