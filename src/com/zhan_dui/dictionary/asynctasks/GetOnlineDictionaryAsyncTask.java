package com.zhan_dui.dictionary.asynctasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.cursoradapters.OnlineListCursorAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.db.DictionaryManager;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.JsonGetter;

public class GetOnlineDictionaryAsyncTask extends
		AsyncTask<Void, Void, Boolean> {

	private ListView mDictionaryList;
	private View mWaittingView;
	private Context mContext;
	private Boolean mResult;
	private SQLiteDatabase mSqliteDatabase;

	public GetOnlineDictionaryAsyncTask(View view) {
		mContext = view.getContext();
		mDictionaryList = (ListView) view.findViewById(android.R.id.list);
		mWaittingView = view.findViewById(android.R.id.empty);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mWaittingView.setVisibility(View.VISIBLE);
		mSqliteDatabase = new DictionaryDB(mContext, DictionaryDB.DB_NAME,
				null, DictionaryDB.DB_VERSION).getReadableDatabase();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		mWaittingView.setVisibility(View.GONE);
		if (mResult) {
			AppMsg.makeText((Activity) mContext,
					mContext.getString(R.string.get_update_success),
					AppMsg.STYLE_INFO).show();
		} else {
			AppMsg.makeText((Activity) mContext,
					mContext.getString(R.string.get_update_failed),
					AppMsg.STYLE_ALERT).show();
		}
		Cursor cursor = mSqliteDatabase
				.rawQuery(
						"select * from dictionary_list where dictionary_downloaded <> '1'",
						null);
		OnlineListCursorAdapter onlineListCursorAdapter = new OnlineListCursorAdapter(
				mContext, cursor);
		mDictionaryList.setAdapter(onlineListCursorAdapter);
		mSqliteDatabase.close();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			String jsonString = JsonGetter
					.get(Constants.ONLINE_DICTIONARY_LIST_URL);
			ParseJson(jsonString);
			mResult = true;
		} catch (Exception e) {
			e.printStackTrace();
			mResult = false;
		} finally {
			
		}
		return mResult;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		mResult = false;
		mWaittingView.setVisibility(View.GONE);
	}

	public void ParseJson(String json) throws JSONException {
		JSONArray jsonArray = new JSONArray(json);
		String name, size, url, save_name;
		JSONObject jsonObject;
		DictionaryDB dictionaryDB = new DictionaryDB(mContext,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery(
				"select count(*) from dictionary_list", null);
		cursor.moveToFirst();
		int dictionaryCount = cursor.getInt(cursor.getColumnIndex("count(*)"));

		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObject = jsonArray.getJSONObject(i);
			name = jsonObject.getString("dictionary_name");
			size = jsonObject.getString("dictionary_size");
			url = jsonObject.getString("dictionary_url");
			save_name = jsonObject.getString("dictionary_save_name");

			DictionaryManager.getInstance(mContext).addAnDictionaryToDB(name,
					save_name, url, size, true, ++dictionaryCount);
		}
		sqLiteDatabase.close();

	}
}
