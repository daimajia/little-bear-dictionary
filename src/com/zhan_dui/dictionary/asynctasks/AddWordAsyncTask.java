package com.zhan_dui.dictionary.asynctasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.datacenter.QueryProcessor;
import com.zhan_dui.dictionary.db.DictionaryDB;

public class AddWordAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private String mToAddWord;
	private DictionaryDB mDictionaryDB;
	private Context mContext;
	private int mErrorCode = SUCCESS;

	private static ArrayList<String> mAddingList = new ArrayList<String>();

	private static int ERROR_NOT_EXSIST = 10;
	private static int ERROR_ALREADY_ADDED = 20;
	private static int SUCCESS = 30;

	private Boolean mAdding = false;
	
	public AddWordAsyncTask(String word, Context context) {
		mToAddWord = word;
		mContext = context;
		if (mAddingList.contains(word) == false) {
			mAddingList.add(word);
		} else {
			mAdding = true;
		}
	}

	@Override
	protected void onPreExecute() {
		if (mToAddWord.length() == 0) {
			cancel(true);
		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected Boolean doInBackground(Void... params) {
		if (mAdding == true) {
			return false;
		}
		mDictionaryDB = new DictionaryDB(mContext, DictionaryDB.DB_NAME, null,
				DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = mDictionaryDB.getWritableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery(
				// 检查是否已经在生词表中存在
				"select * from word where word='" + mToAddWord + "' limit 1",
				null);
		Boolean returnResult = false;

		if (cursor.getCount() == 0) {
			// 检查是否在word中存在
			if (QueryProcessor.instance(mContext).getWordID(mToAddWord) == QueryProcessor.MSG_WORD_NOT_EXISIT) {
				returnResult = false;
				mErrorCode = ERROR_NOT_EXSIST;
			} else {
				ContentValues contentValues = new ContentValues();
				contentValues.put("word", mToAddWord);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				String addTime = simpleDateFormat.format(new Date());
				contentValues.put("addtime", addTime);
				sqLiteDatabase.insert("word", null, contentValues);
				returnResult = true;
			}
		} else {
			returnResult = false;
			mErrorCode = ERROR_ALREADY_ADDED;
		}
		sqLiteDatabase.close();
		mAddingList.remove(mToAddWord);
		return returnResult;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Toast.makeText(mContext, mContext.getString(R.string.add_success),
					Toast.LENGTH_SHORT).show();
		} else {
			if (mErrorCode == ERROR_ALREADY_ADDED)
				Toast.makeText(mContext,
						mContext.getString(R.string.add_repeat),
						Toast.LENGTH_LONG).show();
			else if (mErrorCode == ERROR_NOT_EXSIST) {
				Toast.makeText(
						mContext,
						mToAddWord
								+ mContext.getString(R.string.add_not_exsist),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
