package com.zhan_dui.dictionary.cursoradapters;

import org.holoeverywhere.app.Dialog;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.viewpagerindicator.TitlePageIndicator;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.asynctasks.QueryAsyncTask;
import com.zhan_dui.dictionary.datacenter.DictionaryDataCenter;
import com.zhan_dui.dictionary.listeners.IndicatorOnPageChangeListener;
import com.zhan_dui.dictionary.pageradapter.QueryManageFragmentPager;

public class WordAdapter extends CursorAdapter implements
		StickyListHeadersAdapter {
	private LayoutInflater mLayoutInflater;
	private Context mContext;
	private Cursor mCursor;

	@SuppressWarnings("deprecation")
	public WordAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCursor = c;
	}

	private HeaderViewHolder headerViewHolder;

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.header, parent,
					false);
			headerViewHolder = new HeaderViewHolder();
			headerViewHolder.text = (TextView) convertView
					.findViewById(R.id.text);
			convertView.setTag(headerViewHolder);
		} else {
			headerViewHolder = (HeaderViewHolder) convertView.getTag();
		}
		mCursor.moveToPosition(position);
		char headerChar = mCursor.getString(mCursor.getColumnIndex("word"))
				.subSequence(0, 1).charAt(0);
		headerViewHolder.text.setText(headerChar + "");
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		mCursor.moveToPosition(position);
		return mCursor.getString(mCursor.getColumnIndex("word"))
				.subSequence(0, 1).charAt(0);
	}

	private ViewHolder viewHolder;

	@Override
	public void bindView(View v, Context context, Cursor c) {
		viewHolder = (ViewHolder) v.getTag();
		viewHolder.text.setText(c.getString(c.getColumnIndex("word")));
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup viewGroup) {
		View view = mLayoutInflater.inflate(R.layout.word_item, viewGroup,
				false);
		TextView textView = (TextView) view.findViewById(R.id.word);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.text = textView;
		view.setTag(viewHolder);
		view.setOnClickListener(new OnClickExpanded());
		return view;
	}

	class OnClickExpanded implements OnClickListener {

		@Override
		public void onClick(View v) {
			Dialog dialog = new Dialog(mContext);
			View query_View = mLayoutInflater.inflate(R.layout.query, null);
			ViewPager mViewPager = (ViewPager) query_View
					.findViewById(R.id.viewpager);
			TitlePageIndicator mTitlePageIndicator = (TitlePageIndicator) query_View
					.findViewById(R.id.titles);
			QueryManageFragmentPager mQueryManageFragmentPager = new QueryManageFragmentPager();
			mViewPager.setAdapter(mQueryManageFragmentPager);
			mTitlePageIndicator.setViewPager(mViewPager);
			mTitlePageIndicator
					.setOnPageChangeListener(new IndicatorOnPageChangeListener());
			ViewHolder holder = (ViewHolder) v.getTag();
			String textString = (String) holder.text.getText();
			DictionaryDataCenter.instance(mContext).clear();
			mQueryManageFragmentPager.notifyDataSetChanged();
			mTitlePageIndicator.notifyDataSetChanged();
			new QueryAsyncTask(mContext, mTitlePageIndicator,
					mQueryManageFragmentPager, textString).execute();
			dialog.setTitle(textString);
			dialog.setContentView(query_View);
			dialog.show();
		}
	}

	class HeaderViewHolder {
		TextView text;
	}

	class ViewHolder {
		TextView text;
	}
}
