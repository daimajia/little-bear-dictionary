package com.zhan_dui.dictionary.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.cursoradapters.SimpleWordAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;

/**
 * 简化的生词本，只存储单词
 * 
 * @author xuanqinanhai
 * 
 */
public class SimpleWordsFragment extends SherlockListFragment {

	DictionaryDB mDictionaryDB;

	public SimpleWordsFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.simple_words, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mDictionaryDB = new DictionaryDB(getActivity(), DictionaryDB.DB_NAME,
				null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = mDictionaryDB.getReadableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery(
				"select * from `word` order by `_id` desc", null);
		getListView().setAdapter(new SimpleWordAdapter(getActivity(), cursor));
		sqLiteDatabase.close();
	}

}
