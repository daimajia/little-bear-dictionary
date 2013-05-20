package com.zhan_dui.dictionary.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.cursoradapters.WordAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;

public class WordsFragment extends SherlockListFragment implements
		OnScrollListener, OnItemClickListener, OnHeaderClickListener {
	private static final String KEY_LIST_POSITION = "KEY_LIST_POSITION";
	private int firstVisible;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.words, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		StickyListHeadersListView stickyList = (StickyListHeadersListView) view
				.findViewById(R.id.list);
		stickyList.setDivider(new ColorDrawable(0xffffffff));
		stickyList.setDividerHeight(1);
		stickyList.setOnScrollListener(this);
		stickyList.setOnItemClickListener(this);
		stickyList.setOnHeaderClickListener(this);

		if (savedInstanceState != null) {
			firstVisible = savedInstanceState.getInt(KEY_LIST_POSITION);
		}
		DictionaryDB dictionaryDB = new DictionaryDB(getActivity(),
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getReadableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery(
				"select * from word order by word", null);
		WordAdapter wordAdapter = new WordAdapter(getActivity(), cursor);
		stickyList.setAdapter(wordAdapter);
		stickyList.setSelection(firstVisible);
	}

	@Override
	public void onHeaderClick(StickyListHeadersListView l, View header,
			int itemPosition, long headerId, boolean currentlySticky) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}
}
