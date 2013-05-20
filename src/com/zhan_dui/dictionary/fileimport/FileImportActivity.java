/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zhan_dui.dictionary.fileimport;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.ThemeUtils;

/**
 * This is the main activity. The activity that is presented to the user as the
 * application launches. This class is, and expected not to be, instantiated. <br>
 * <p>
 * This class handles creating the buttons and text views. This class relies on
 * the class EventHandler to handle all button press logic and to control the
 * data displayed on its ListView. This class also relies on the FileManager
 * class to handle all file operations such as copy/paste zip/unzip etc. However
 * most interaction with the FileManager class is done via the EventHandler
 * class. Also the SettingsMangager class to load and save user settings. <br>
 * <p>
 * The design objective with this class is to control only the look of the GUI
 * (option menu, context menu, ListView, buttons and so on) and rely on other
 * supporting classes to do the heavy lifting.
 * 
 * @author Joe Berria
 * 
 */
public final class FileImportActivity extends SherlockListActivity {

	private static final String PREFS_NAME = "ManagerPrefsFile";
	private static final String PREFS_SORT = "sort";

	private FileManager mFileMag;
	private EventHandler mHandler;
	private EventHandler.TableRow mTable;

	private SharedPreferences mSettings;
	private boolean mUseBackKey = true;
	private TextView mPathLabel;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		ThemeUtils.onActivityCreateSetTheme(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.import_zip_main);
		mContext = this;
		/* read settings */
		mSettings = getSharedPreferences(PREFS_NAME, 0);

		int sort = mSettings.getInt(PREFS_SORT, 2);

		mFileMag = new FileManager();
		mFileMag.setSortType(sort);

		if (savedInstanceState != null)
			mHandler = new EventHandler(FileImportActivity.this, mFileMag,
					savedInstanceState.getString("location"));
		else
			mHandler = new EventHandler(FileImportActivity.this, mFileMag);

		mTable = mHandler.new TableRow();

		/*
		 * sets the ListAdapter for our ListActivity andgives our EventHandler
		 * class the same adapter
		 */
		mHandler.setListAdapter(mTable);
		setListAdapter(mTable);

		/* register context menu for our list view */
		registerForContextMenu(getListView());

		mPathLabel = (TextView) findViewById(R.id.path_label);
		mPathLabel.setText("path: /sdcard");
		setTitle(R.string.import_title);

		getSupportActionBar().setDisplayOptions(
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
						| ActionBar.DISPLAY_SHOW_HOME);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("location", mFileMag.getCurrentDir());
	}

	@Override
	public void onListItemClick(ListView parent, View view, int position,
			long id) {
		final String item = mHandler.getData(position);
		final File file = new File(mFileMag.getCurrentDir() + "/" + item);
		String item_ext = null;

		try {
			item_ext = item.substring(item.lastIndexOf(".") + 1, item.length());
		} catch (IndexOutOfBoundsException e) {
			item_ext = "";
		}

		if (file.isDirectory()) {
			if (file.canRead()) {
				mHandler.updateDirectory(mFileMag.getNextDir(item, false));
				mPathLabel.setText(mFileMag.getCurrentDir());

				if (!mUseBackKey)
					mUseBackKey = true;

			} else {
				Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT)
						.show();
			}
		}

		else if (item_ext.equalsIgnoreCase("zip")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.alert);
			builder.setMessage(R.string.sure_this_directory);
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(mContext, file.getPath(),
									Toast.LENGTH_SHORT).show();
							UnzipNotificationCenter unzipNotificationCenter = new UnzipNotificationCenter(
									mContext, FileImportActivity.class);

							int id = unzipNotificationCenter
									.prepareUnzipNotification(R.drawable.unzip,
											R.string.start_unzip,
											R.string.start_unzip,
											R.string.unzip_tip);

							unzipNotificationCenter.startUnzip(id,
									file.getPath(),
									Environment.getExternalStorageDirectory()
											+ "/" + Constants.SAVE_DIRECTORY,
									true);
						}
					});
			builder.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.show();
		}
	}

	private final int ORDER_DEFAULT = 1;
	private final int ORDER_ABC = 2;
	private final int ORDER_TYPE = 3;
	private final int ORDER_SIZE = 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenu = menu.addSubMenu(R.string.setting_import);
		subMenu.add(0, ORDER_DEFAULT, 0, getString(R.string.sort_default));
		subMenu.add(0, ORDER_ABC, 0, getString(R.string.sort_abc));
		subMenu.add(0, ORDER_TYPE, 0, getString(R.string.sort_type));
		subMenu.add(0, ORDER_SIZE, 0, getString(R.string.sort_size));
		subMenu.getItem().setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
			return false;
		}
		mSettings.edit().putInt(PREFS_SORT, item.getItemId()).commit();
		mFileMag.setSortType(item.getItemId());
		mHandler.updateDirectory(mFileMag.getNextDir(mFileMag.getCurrentDir(),
				true));
		return true;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		String current = mFileMag.getCurrentDir();

		if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey
				&& !current.equals("/")) {

			mHandler.updateDirectory(mFileMag.getPreviousDir());
			mPathLabel.setText(mFileMag.getCurrentDir());
			return true;

		} else if (keycode == KeyEvent.KEYCODE_BACK && mUseBackKey
				&& current.equals("/")) {

			Toast.makeText(FileImportActivity.this,
					R.string.root_directory_tip, Toast.LENGTH_SHORT).show();

			mUseBackKey = false;
			mPathLabel.setText(mFileMag.getCurrentDir());

			return false;

		} else if (keycode == KeyEvent.KEYCODE_BACK && !mUseBackKey
				&& current.equals("/")) {
			finish();

			return false;
		}
		return false;
	}
}
