package com.zhan_dui.dictionary.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.view.View;

import com.mobeta.android.dslv.DragSortListView;
import com.zhan_dui.dictionary.cursoradapters.OfflineListCursorAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;

/**
 * 离线字典的管理功能
 * 
 * @author xuanqinanhai
 * 
 */
public class GetOfflineAsyncTask extends AsyncTask<Void, Void, Cursor> {

	private DragSortListView mOfflineDragSortListView;
	private View mWaittingView;
	private View mContainerView;
	private SQLiteDatabase mSqLiteDatabase;
	private Context mContext;
	private Cursor mCursor;
	private CursorAdapter mCursorAdapter;

	public GetOfflineAsyncTask(View container) {
		mContainerView = container;
		mContext = container.getContext();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaittingView = mContainerView.findViewById(android.R.id.empty);
		mWaittingView.setVisibility(View.VISIBLE);
	}

	@Override
	protected Cursor doInBackground(Void... params) {
		mOfflineDragSortListView = (DragSortListView) mContainerView
				.findViewById(android.R.id.list);
		mSqLiteDatabase = new DictionaryDB(mContext, DictionaryDB.DB_NAME,
				null, DictionaryDB.DB_VERSION).getReadableDatabase();
		mCursor = mSqLiteDatabase
				.rawQuery(
						"select * from dictionary_list where dictionary_downloaded = '1' order by `dictionary_order` asc",
						null);
		return mCursor;
	}

	@Override
	protected void onPostExecute(Cursor result) {
		super.onPostExecute(result);
		mWaittingView.setVisibility(View.GONE);
		mCursorAdapter = new OfflineListCursorAdapter(mContext, result, true);
		mOfflineDragSortListView.setAdapter(mCursorAdapter);
		mSqLiteDatabase.close();
	}
}
