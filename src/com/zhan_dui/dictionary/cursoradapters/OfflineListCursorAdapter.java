package com.zhan_dui.dictionary.cursoradapters;

import java.io.File;

import org.holoeverywhere.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mobeta.android.dslv.DragSortCursorAdapter;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.utils.Constants;

@SuppressLint("HandlerLeak")
public class OfflineListCursorAdapter extends DragSortCursorAdapter implements
		OnCheckedChangeListener {

	private Context mContext;
	private LayoutInflater mLayoutInflater;

	public OfflineListCursorAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		this.mContext = context;
		this.mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		TextView dictionary_name_textView = (TextView) convertView
				.findViewById(R.id.item_dictionary_name);
		TextView dictionary_size = (TextView) convertView
				.findViewById(R.id.item_dictionary_size);
		ToggleButton toggleButton = (ToggleButton) convertView
				.findViewById(R.id.item_hide);
		Boolean isShow = cursor
				.getInt(cursor.getColumnIndex("dictionary_show")) == 1 ? true
				: false;
		toggleButton.setChecked(isShow);
		toggleButton.setContentDescription(cursor.getString(cursor
				.getColumnIndex("_id")));
		toggleButton.setOnCheckedChangeListener(this);
		dictionary_name_textView.setText(cursor.getString(cursor
				.getColumnIndex("dictionary_name")));
		dictionary_size.setText(cursor.getString(cursor
				.getColumnIndex("dictionary_size")));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return mLayoutInflater.inflate(R.layout.offline_dictionary_list_item,
				null);
	}

	/**
	 * 删除字典线程
	 */
	private class DeleteDictionaryThread extends Thread {
		private Cursor mCursor;
		private Handler mHandler;

		public DeleteDictionaryThread(Cursor cursor, Handler handler) {
			this.mCursor = cursor;
			this.mHandler = handler;
		}

		@Override
		public void run() {
			super.run();

			DictionaryDB dictionaryDB = new DictionaryDB(mContext,
					DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
			SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
			String delete = "delete from `dictionary_list` where `_id`=?";
			String[] arg = { mCursor.getString(mCursor.getColumnIndex("_id")) };
			sqLiteDatabase.execSQL(delete, arg);
			String path_prefix = Constants.getSaveDirectory() + File.separator;
			String dictionary_save_name = mCursor.getString(mCursor
					.getColumnIndex("dictionary_save_name"));
			String nameString = dictionary_save_name.substring(0,
					dictionary_save_name.lastIndexOf("."));

			File file = new File(path_prefix + dictionary_save_name);

			file.delete();

			file = new File(path_prefix + "config-" + nameString + ".dic");

			file.delete();

			file = new File(path_prefix + "import-" + nameString + ".dic");

			file.delete();

			file = new File(path_prefix + nameString + ".dic");

			file.delete();

			sqLiteDatabase.close();

			Message msg = Message.obtain(mHandler);
			msg.sendToTarget();

		}
	}

	private Handler mDeleteHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DictionaryDB dictionaryDB = new DictionaryDB(mContext,
					DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
			SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
			Cursor cursor = sqLiteDatabase
					.rawQuery(
							"select * from dictionary_list where dictionary_downloaded = '1' order by `dictionary_order` asc",
							null);
			changeCursor(cursor);
		}

	};

	@Override
	public void remove(int which) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.tips);
		String to_delete_tips = mContext.getString(R.string.sure_to_delete);
		final Cursor cursor = getCursor();
		if (cursor.moveToPosition(which)) {
			final String name = cursor.getString(cursor
					.getColumnIndex("dictionary_name"));
			to_delete_tips = String.format(to_delete_tips, name);
			builder.setMessage(to_delete_tips);
			builder.setNegativeButton(R.string.ok, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new DeleteDictionaryThread(cursor, mDeleteHandler).start();
					Toast.makeText(mContext, "开始删除" + name, Toast.LENGTH_SHORT)
							.show();
				}
			});

			builder.setPositiveButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					notifyDataSetChanged();
				}
			});
		} else {
			builder.setMessage("error");

		}
		builder.show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String id = buttonView.getContentDescription().toString();
		String status = isChecked == true ? "1" : "0";
		DictionaryDB dictionaryDB = new DictionaryDB(mContext,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		String[] argsStrings = { status, id };
		sqLiteDatabase
				.execSQL(
						"update `dictionary_list` set `dictionary_show`=? where `_id`=?",
						argsStrings);
		sqLiteDatabase.close();
	}
}
