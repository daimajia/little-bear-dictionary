package com.zhan_dui.dictionary.cursoradapters;

import org.holoeverywhere.app.Dialog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.asynctasks.QueryAsyncTask;
import com.zhan_dui.dictionary.datacenter.DictionaryDataCenter;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.listeners.IndicatorOnPageChangeListener;
import com.zhan_dui.dictionary.pageradapter.QueryManageFragmentPager;

public class SimpleWordAdapter extends CursorAdapter implements OnClickListener {

	private LayoutInflater mInflater;
	private Context mContext;
	private DictionaryDB mDictionaryDB;

	@SuppressWarnings("deprecation")
	public SimpleWordAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDictionaryDB = new DictionaryDB(mContext, DictionaryDB.DB_NAME, null,
				DictionaryDB.DB_VERSION);
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		String word = cursor.getString(cursor.getColumnIndex("word"));
		String id = cursor.getString(cursor.getColumnIndex("_id"));
		convertView.setContentDescription(word);
		viewHolder.mWordtTextView.setText(word);
		viewHolder.mDeleteButton.setContentDescription(id);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup arg2) {
		View view = mInflater.inflate(R.layout.simple_word_item, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mWordtTextView = (TextView) view.findViewById(R.id.word);
		viewHolder.mDeleteButton = (Button) view.findViewById(R.id.delete);
		viewHolder.mDeleteButton.setOnClickListener(this);
		view.setTag(viewHolder);
		view.setOnClickListener(this);
		return view;
	}

	private class ViewHolder {
		TextView mWordtTextView;
		Button mDeleteButton;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delete:
			String id = (String) v.getContentDescription();
			SQLiteDatabase sqLiteDatabase = mDictionaryDB.getWritableDatabase();
			sqLiteDatabase.execSQL("delete from `word` where `_id`='" + id
					+ "'");
			Cursor cursor = sqLiteDatabase.rawQuery(
					"select * from `word` order by `_id` desc", null);
			changeCursor(cursor);
			notifyDataSetChanged();
			sqLiteDatabase.close();
			break;
		case R.id.word_item:
			String word = (String) v.getContentDescription();
			Dialog dialog = new Dialog(mContext);
			View query_View = mInflater.inflate(R.layout.query, null);
			ViewPager mViewPager = (ViewPager) query_View
					.findViewById(R.id.viewpager);
			TitlePageIndicator mTitlePageIndicator = (TitlePageIndicator) query_View
					.findViewById(R.id.titles);
			QueryManageFragmentPager mQueryManageFragmentPager = new QueryManageFragmentPager();
			mViewPager.setAdapter(mQueryManageFragmentPager);
			mTitlePageIndicator.setViewPager(mViewPager);
			mTitlePageIndicator
					.setOnPageChangeListener(new IndicatorOnPageChangeListener());
			DictionaryDataCenter.instance(mContext).clear();
			new QueryAsyncTask(mContext, mTitlePageIndicator,
					mQueryManageFragmentPager, word).execute();
			dialog.setTitle(word);
			dialog.setContentView(query_View);
			dialog.show();
		default:
			break;
		}

	}
}
