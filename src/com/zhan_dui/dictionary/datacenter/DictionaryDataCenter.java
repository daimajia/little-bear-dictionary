package com.zhan_dui.dictionary.datacenter;

import java.util.HashMap;

import com.zhan_dui.dictionary.db.DictionaryDB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

/**
 * 
 * @Description:Dictionary data pool,when a query finished,it add a dictionary
 *                         name and dictionary show view into this center. The
 *                         dictionary name ArrayList used to ViewPagerIndicator,
 *                         The dictionary view map will be used to
 *                         DictionaryView Fragment.
 * 
 *                         DictionaryDataCenter
 *                         是一个数据池，当一个单词查询由QueryProcessor查询完成后，他将所有的查询结添加到数据池中，
 *                         而后由其他需要数据的地方去获取。
 */
@SuppressLint("UseSparseArrays")
public class DictionaryDataCenter {

	private static DictionaryDataCenter mDataCenter;
	private static HashMap<Integer, DictionaryDetail> mDictionaryViewMap = new HashMap<Integer, DictionaryDetail>();
	private static int mLastPosition = 0;

	private static DictionaryDB mDictionaryDB;
	private static Context mContext;

	private class DictionaryDetail {
		public String mDictionaryName;
		public View mShowView;

		public DictionaryDetail(String dictionaryName, View showView) {
			mDictionaryName = dictionaryName;
			mShowView = showView;
		}
	}

	private DictionaryDataCenter(Context context) {
		mContext = context;
		mDictionaryDB = new DictionaryDB(mContext, DictionaryDB.DB_NAME, null,
				DictionaryDB.DB_VERSION);
	}

	public static DictionaryDataCenter instance(Context context) {
		if (mDataCenter == null) {
			mDataCenter = new DictionaryDataCenter(context);
		}
		return mDataCenter;
	}

	public void addWord(String word) {
		SQLiteDatabase sqLiteDatabase = mDictionaryDB.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("word", word);
		sqLiteDatabase.insert("word", null, contentValues);
		sqLiteDatabase.close();
	}

	public String getPagerTitle(int position) {
		return mDictionaryViewMap.get(position).mDictionaryName;
	}

	public String getDictionaryNameByPosition(int position) {
		return mDictionaryViewMap.get(position).mDictionaryName;
	}

	public void clear() {
		mDictionaryViewMap.clear();
		mLastPosition = 0;
	}

	public View getDictionaryViewByPosition(int position) {
		return mDictionaryViewMap.get(position).mShowView;
	}

	public void addDictionaryView(String dictionaryName, View dictionaryView) {
		mDictionaryViewMap.put(mLastPosition, new DictionaryDetail(
				dictionaryName, dictionaryView));
		mLastPosition++;
	}

	public int getDictionaryViewCount() {
		return mDictionaryViewMap.size();
	}

	public void queryWord(String word) {

	}
}
