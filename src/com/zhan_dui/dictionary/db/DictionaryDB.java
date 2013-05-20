package com.zhan_dui.dictionary.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

@SuppressLint("DefaultLocale")
public class DictionaryDB extends SQLiteOpenHelper {

	public final static String DB_NAME = "dictionary";
	public final static int DB_VERSION = 3;
	public final static String DB_DICTIONARY_LIST_NAME = "dictionary_list";

	public DictionaryDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String createSql = "create table if not exists dictionary_list (_id INTEGER PRIMARY KEY AUTOINCREMENT,dictionary_name text,dictionary_size text,dictionary_url text,dictionary_save_name text,dictionary_downloaded INTEGER default 0,dictionary_show INTEGER default 0,dictionary_order INTEGER default 0);";
		String createSql_words = "create table if not exists  word(_id INTEGER PRIMARY KEY AUTOINCREMENT,word text,addtime DATETIME);";
		sqLiteDatabase.execSQL(createSql);
		sqLiteDatabase.execSQL(createSql_words);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
