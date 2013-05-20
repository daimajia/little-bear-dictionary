package com.zhan_dui.dictionary.cursoradapters;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.listeners.DownloadDictionaryCancelListener;
import com.zhan_dui.dictionary.listeners.DownloadDictionaryListener;

@SuppressLint("NewApi")
public class OnlineListCursorAdapter extends CursorAdapter {

	LayoutInflater layoutInflater;
	Context context;
	ArrayList<String> dictionarysInfos = new ArrayList<String>();
	public static ArrayList<String> downloadingNotificationUrls = new ArrayList<String>();
	public static ArrayList<String> downloadingNotificationCancels = new ArrayList<String>();
	private CursorAdapter thisCursorAdapter;

	public OnlineListCursorAdapter(Context context, Cursor c) {
		super(context, c, true);
		this.context = context;
		thisCursorAdapter = this;
		if (c == null) {
			DictionaryDB dictionaryDB = new DictionaryDB(context,
					DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
			SQLiteDatabase sqLiteDatabase = dictionaryDB.getReadableDatabase();
			c = sqLiteDatabase
					.rawQuery(
							"select * from dictionary_list where dictionary_downloaded <> '1'",
							null);
			sqLiteDatabase.close();
		}
		layoutInflater = LayoutInflater.from(context);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = layoutInflater.inflate(
				R.layout.online_dictionary_list_item, null);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.DictionaryName = (TextView) view
				.findViewById(R.id.item_dictionary_name);
		viewHolder.DictionarySize = (TextView) view
				.findViewById(R.id.item_dictionary_size);
		viewHolder.DictionaryDownloadButton = (Button) view
				.findViewById(R.id.item_download);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();

		String dictionary_name = cursor.getString(cursor
				.getColumnIndex("dictionary_name"));
		String dictionary_size = cursor.getString(cursor
				.getColumnIndex("dictionary_size"));
		String dictionary_save_name = cursor.getString(cursor
				.getColumnIndex("dictionary_save_name"));
		String dictionary_url = cursor.getString(cursor
				.getColumnIndex("dictionary_url"));
		String dictionary_downloaded = cursor.getString(cursor
				.getColumnIndex("dictionary_downloaded"));
		int id = cursor.getInt(cursor.getColumnIndex("_id"));

		viewHolder.DictionaryName.setText(dictionary_name);
		viewHolder.DictionarySize.setText(dictionary_size);
		viewHolder.DictionaryName.setContentDescription(id + "");
		viewHolder.DictionaryDownloadButton
				.setContentDescription(dictionary_url);
		
		viewHolder.DictionaryDownloadButton
				.setOnClickListener(new DownloadDictionaryListener(context,
						thisCursorAdapter, id, dictionary_name,
						dictionary_save_name, dictionary_url, dictionary_size));

		if (dictionary_downloaded.equals("1")) {
			viewHolder.DictionaryDownloadButton.setText(context
					.getString(R.string.download_finished));
			viewHolder.DictionaryDownloadButton.setEnabled(false);
		} else if (downloadingNotificationUrls.contains(dictionary_url)) {
			viewHolder.DictionaryDownloadButton.setEnabled(true);
			viewHolder.DictionaryDownloadButton.setText(context
					.getString(R.string.download_cancel));
			viewHolder.DictionaryDownloadButton
					.setOnClickListener(new DownloadDictionaryCancelListener(
							dictionary_url, thisCursorAdapter));
		} else {
			viewHolder.DictionaryDownloadButton.setText(context
					.getString(R.string.dictionary_download));
			if (viewHolder.DictionaryDownloadButton.isEnabled() == false) {
				viewHolder.DictionaryDownloadButton.setEnabled(true);
			}
		}

	}

	static class ViewHolder {
		TextView DictionaryName, DictionarySize;
		Button DictionaryDownloadButton;
	}

}
