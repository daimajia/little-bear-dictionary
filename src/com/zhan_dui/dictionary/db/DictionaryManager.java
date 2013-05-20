package com.zhan_dui.dictionary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 管理所有字典数据
 * 
 * @author xuanqinanhai
 * 
 */
public class DictionaryManager {

	private static DictionaryManager _dictionaryManager;

	private static DictionaryDB mDictionaryDB;

	private SQLiteDatabase mSqLiteDatabase;

	private DictionaryManager(Context context) {
		mDictionaryDB = new DictionaryDB(context, DictionaryDB.DB_NAME, null,
				DictionaryDB.DB_VERSION);
	}

	public static DictionaryManager getInstance(Context context) {
		if (_dictionaryManager == null) {
			return new DictionaryManager(context);
		} else {
			return _dictionaryManager;
		}
	}

	public boolean addAnDictionaryToDB(String dictionaryName,
			String dictionarySaveName, String dictionaryUrl,
			String dictionarySize, boolean dictionaryShow, int dictionaryOrder) {
		mSqLiteDatabase = mDictionaryDB.getWritableDatabase();

		String checkIfExsit = "select * from dictionary_list where `dictionary_name`='"
				+ dictionaryName + "'";

		Cursor cursor = mSqLiteDatabase.rawQuery(checkIfExsit, null);

		int count = cursor.getCount();

		if (count == 0) {
			// 如果字典不存在则添加
			ContentValues contentValues = new ContentValues();
			contentValues.put("dictionary_name", dictionaryName);
			contentValues.put("dictionary_size", dictionarySize);
			contentValues.put("dictionary_url", dictionaryUrl);
			contentValues.put("dictionary_save_name", dictionarySaveName);
			contentValues.put("dictionary_show", dictionaryShow == true ? "1"
					: "0");
			contentValues.put("dictionary_order",
					String.valueOf(dictionaryOrder));
			mSqLiteDatabase.insert("dictionary_list", null, contentValues);
			mSqLiteDatabase.close();
			return true;
		} else {
			mSqLiteDatabase.close();
			return false;
		}
	}

	public boolean removeAnDictionaryFromDB(String dictionaryName,
			String dictionarySaveName) {
		mSqLiteDatabase = mDictionaryDB.getWritableDatabase();
		String checkIfExsit = "select * from dictionary_list where `dictionary_name`='%s' and `dictionary_save_name`='%s'";
		checkIfExsit = String.format(checkIfExsit, dictionaryName,
				dictionarySaveName);
		Cursor cursor = mSqLiteDatabase.rawQuery(checkIfExsit, null);
		int count = cursor.getCount();
		if (count == 0) {
			mSqLiteDatabase.close();
			return false;
		} else {
			String deleteDicItem = "delete from dictionary_list where `dictionary_name`='%s' and `dictionary_save_name`='%s'";
			deleteDicItem = String.format(deleteDicItem, dictionaryName,
					dictionarySaveName);
			mSqLiteDatabase.execSQL(deleteDicItem);
			mSqLiteDatabase.close();
			// TO:删除的时候，还得删除对应的文件
		}
		return true;
	}
}
