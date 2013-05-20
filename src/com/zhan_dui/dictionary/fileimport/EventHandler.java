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
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhan_dui.dictionary.R;

/**
 * This class sits between the Main activity and the FileManager class. To keep
 * the FileManager class modular, this class exists to handle UI events and
 * communicate that information to the FileManger class
 * 
 * This class is responsible for the buttons onClick method. If one needs to
 * change the functionality of the buttons found from the Main activity or add
 * button logic, this is the class that will need to be edited.
 * 
 * This class is responsible for handling the information that is displayed from
 * the list view (the files and folder) with a a nested class TableRow. The
 * TableRow class is responsible for displaying which icon is shown for each
 * entry. For example a folder will display the folder icon, a Word doc will
 * display a word icon and so on. If more icons are to be added, the TableRow
 * class must be updated to display those changes.
 * 
 * @author Joe Berria
 */
public class EventHandler {
	/*
	 * Unique types to control which file operation gets performed in the
	 * background
	 */

	private final Context mContext;
	private final FileManager mFileMang;
	private TableRow mDelegate;

	private boolean multi_select_flag = false;

	// the list used to feed info into the array adapter and when multi-select
	// is on
	private ArrayList<String> mDataSource, mMultiSelectData;
	private TextView mPathLabel;
	private TextView mInfoLabel;

	/**
	 * Creates an EventHandler object. This object is used to communicate most
	 * work from the Main activity to the FileManager class.
	 * 
	 * @param context
	 *            The context of the main activity e.g Main
	 * @param manager
	 *            The FileManager object that was instantiated from Main
	 */
	public EventHandler(Context context, final FileManager manager) {
		mContext = context;
		mFileMang = manager;

		mDataSource = new ArrayList<String>(mFileMang.setHomeDir(Environment
				.getExternalStorageDirectory().getPath()));
	}

	/**
	 * This constructor is called if the user has changed the screen orientation
	 * and does not want the directory to be reset to home.
	 * 
	 * @param context
	 *            The context of the main activity e.g Main
	 * @param manager
	 *            The FileManager object that was instantiated from Main
	 * @param location
	 *            The first directory to display to the user
	 */
	public EventHandler(Context context, final FileManager manager,
			String location) {
		mContext = context;
		mFileMang = manager;

		mDataSource = new ArrayList<String>(
				mFileMang.getNextDir(location, true));
	}

	/**
	 * This method is called from the Main activity and this has the same
	 * reference to the same object so when changes are made here or there they
	 * will display in the same way.
	 * 
	 * @param adapter
	 *            The TableRow object
	 */
	public void setListAdapter(TableRow adapter) {
		mDelegate = adapter;
	}

	/**
	 * This method is called from the Main activity and is passed the TextView
	 * that should be updated as the directory changes so the user knows which
	 * folder they are in.
	 * 
	 * @param path
	 *            The label to update as the directory changes
	 * @param label
	 *            the label to update information
	 */
	public void setUpdateLabels(TextView path, TextView label) {
		mPathLabel = path;
		mInfoLabel = label;
	}

	/**
	 * Indicates whether the user wants to select multiple files or folders at a
	 * time. <br>
	 * <br>
	 * false by default
	 * 
	 * @return true if the user has turned on multi selection
	 */
	public boolean isMultiSelected() {
		return multi_select_flag;
	}

	/**
	 * Use this method to determine if the user has selected multiple
	 * files/folders
	 * 
	 * @return returns true if the user is holding multiple objects
	 *         (multi-select)
	 */
	public boolean hasMultiSelectData() {
		return (mMultiSelectData != null && mMultiSelectData.size() > 0);
	}

	/**
	 * will return the data in the ArrayList that holds the dir contents.
	 * 
	 * @param position
	 *            the indext of the arraylist holding the dir content
	 * @return the data in the arraylist at position (position)
	 */
	public String getData(int position) {

		if (position > mDataSource.size() - 1 || position < 0)
			return null;

		return mDataSource.get(position);
	}

	/**
	 * called to update the file contents as the user navigates there phones
	 * file system.
	 * 
	 * @param content
	 *            an ArrayList of the file/folders in the current directory.
	 */
	public void updateDirectory(ArrayList<String> content) {
		if (!mDataSource.isEmpty())
			mDataSource.clear();

		for (String data : content) {
			mDataSource.add(data);
		}
		mDelegate.notifyDataSetChanged();
	}

	private static class ViewHolder {
		TextView topView;
		TextView bottomView;
		ImageView icon;
	}

	/**
	 * A nested class to handle displaying a custom view in the ListView that is
	 * used in the Main activity. If any icons are to be added, they must be
	 * implemented in the getView method. This class is instantiated once in
	 * Main and has no reason to be instantiated again.
	 * 
	 * @author Joe Berria
	 */
	public class TableRow extends ArrayAdapter<String> {
		private final int KB = 1024;
		private final int MG = KB * KB;
		private final int GB = MG * KB;
		private String display_size;

		public TableRow() {
			super(mContext, R.layout.tablerow, mDataSource);
		}

		public String getFilePermissions(File file) {
			String per = "-";

			if (file.isDirectory())
				per += "d";
			if (file.canRead())
				per += "r";
			if (file.canWrite())
				per += "w";

			return per;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder mViewHolder;
			int num_items = 0;
			String temp = mFileMang.getCurrentDir();
			File file = new File(temp + "/" + mDataSource.get(position));
			String[] list = file.list();

			if (list != null)
				num_items = list.length;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater
						.inflate(R.layout.tablerow, parent, false);

				mViewHolder = new ViewHolder();
				mViewHolder.topView = (TextView) convertView
						.findViewById(R.id.top_view);
				mViewHolder.bottomView = (TextView) convertView
						.findViewById(R.id.bottom_view);
				mViewHolder.icon = (ImageView) convertView
						.findViewById(R.id.row_image);
				convertView.setTag(mViewHolder);

			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}

			if (file != null && file.isFile()) {
				String ext = file.toString();
				String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);
				if (sub_ext.equalsIgnoreCase("zip")) {
					mViewHolder.icon.setImageResource(R.drawable.zip);
				}
			} else if (file != null && file.isDirectory()) {
				if (file.canRead() && file.list() != null
						&& file.list().length > 0)
					mViewHolder.icon.setImageResource(R.drawable.folder);
				else
					mViewHolder.icon.setImageResource(R.drawable.folder);
			}

			String permission = getFilePermissions(file);

			if (file.isFile()) {
				double size = file.length();
				if (size > GB)
					display_size = String
							.format("%.2f Gb ", (double) size / GB);
				else if (size < GB && size > MG)
					display_size = String
							.format("%.2f Mb ", (double) size / MG);
				else if (size < MG && size > KB)
					display_size = String
							.format("%.2f Kb ", (double) size / KB);
				else
					display_size = String.format("%.2f bytes ", (double) size);

				if (file.isHidden())
					mViewHolder.bottomView
							.setText("(hidden) | " + display_size);
				else
					mViewHolder.bottomView.setText(display_size + " | "
							+ permission);

			} else {
				if (file.isHidden())
					mViewHolder.bottomView.setText("(hidden) | " + num_items
							+ " items | " + permission);
				else {
					if (num_items != 0)
						mViewHolder.bottomView.setText(num_items + " items | "
								+ permission);
				}
			}

			mViewHolder.topView.setText(file.getName());

			return convertView;
		}
	}

}
