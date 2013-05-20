package com.zhan_dui.dictionary.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mobeta.android.dslv.DragSortListView;
import com.viewpagerindicator.TabPageIndicator;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.cursoradapters.OfflineListCursorAdapter;
import com.zhan_dui.dictionary.datacenter.QueryProcessor;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.fileimport.FileImportActivity;
import com.zhan_dui.dictionary.listeners.IndicatorOnPageChangeListener;
import com.zhan_dui.dictionary.pageradapter.DictionaryManageFragmentPager;

/**
 * 字典管理页面
 * 
 * @author xuanqinanhai
 * 
 */
public class DictionaryManageFragment extends SherlockFragment {

	private ViewPager mViewPager;
	private TabPageIndicator mTabPageIndicator;
	private ActionBar mActionBar;
	private LayoutInflater mLayoutInflater;
	private DictionaryManageFragmentPager mDictionaryManageFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = ((SherlockFragmentActivity) getActivity())
				.getSupportActionBar();
		getActivity().setTitle(R.string.offline_menu);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View resultView = inflater.inflate(R.layout.dictionary_manage,
				container, false);
		return resultView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.search_import, menu);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mTabPageIndicator = (TabPageIndicator) view.findViewById(R.id.titles);
		mDictionaryManageFragment = new DictionaryManageFragmentPager(
				getActivity());
		mViewPager.setAdapter(mDictionaryManageFragment);
		mTabPageIndicator.setViewPager(mViewPager);
		mTabPageIndicator
				.setOnPageChangeListener(new IndicatorOnPageChangeListener());

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		QueryProcessor.updateCacheDictionaryList(getActivity()
				.getApplicationContext());

		if (mDictionaryManageFragment.mOfflineDictionaryManageView == null) {
			return;
		}
		DragSortListView dragSortListView = (DragSortListView) mDictionaryManageFragment.mOfflineDictionaryManageView
				.findViewById(android.R.id.list);
		OfflineListCursorAdapter dragSortCursorAdapter = (OfflineListCursorAdapter) dragSortListView
				.getInputAdapter();
		ArrayList<Integer> SortedResult = dragSortCursorAdapter
				.getCursorPositions();
		if (SortedResult == null || SortedResult.size() == 0) {
			return;
		}
		Cursor cursor = dragSortCursorAdapter.getCursor();
		cursor.moveToPosition(-1);
		int i = 0;
		DictionaryDB dictionaryDB = new DictionaryDB(getActivity(),
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();

		while (cursor.moveToNext()) {
			String[] args = { SortedResult.get(i).toString(),
					cursor.getString(cursor.getColumnIndex("_id")) };
			sqLiteDatabase
					.execSQL(
							"update `dictionary_list` set `dictionary_order`= ? where `_id`= ?",
							args);
			i++;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.import_files) {
			Intent intent = new Intent(getActivity(), FileImportActivity.class);
			getActivity().startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
