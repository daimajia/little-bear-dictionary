package com.zhan_dui.dictionary.utils;

import java.io.File;

import android.os.Environment;

/**
 * 程序常量
 * 
 * @author xuanqinanhai
 * 
 */
public class Constants {
	private Constants() {

	}

	/**
	 * 字典在线列表地址
	 */
	public static final String ONLINE_DICTIONARY_LIST_URL = "http://dic.zhan-dui.com/json.php";

	public static final String WEIBO_APP_KEY = "3376967044";
	public static final long WEIBO_BEAR_ID = 3195393535L;
	public static final String WEIBO_BEAR_NAME = "小熊词典";
	public static final String WEIBO_REDIRECT_URI = "http://dic.zhan-dui.com/auth.php";

	public static final int DOWNLOADING = 1;
	public static final int DOWNLOAD_ERROR = -1;
	public static final int DOWNLOAD_SUCCESS = 0;
	public static final int DOWNLOAD_FINISH = 2;
	public static final int DOWNLOAD_CANCEL = 3;
	public static final int DOWNLOAD_START = 4;

	public static final int MOVE_START = 0;
	public static final int MOVING = 1;
	public static final int MOVE_END = 2;
	public static final int MOVE_ERROR = 3;

	public static final int CONNECTION_ERROR = 6;

	public static final int FILE_CREATE_ERROR = 4;
	public static final int MALFORM_URL = 5;

	public static final String PREFER_NAME = "LITTLE_BEAR";

	public static final String PREFER_FIRST = "FIRST_START";

	public static final String SAVE_DIRECTORY = "dictionary";
	public static final String BASE_DICTIONARY = "dictionary_word.sqlite";
	public static final String BASE_DICTIONARY_ASSET = "dictionary_word.sqlite.zip";

	public static final int UNZIPPING = 0;
	public static final int UNZIP_ERROR = -1;
	public static final int UNZIP_START = 1;
	public static final int UNZIP_FINISH = 2;

	public static final int DEFAULT_SMALL_SIZE = 14;
	public static final int DEFAULT_MEDIUM_SIZE = 19;
	public static final int DEFAULT_LARGE_SIZE = 25;

	public static final int JSON_STRING_GET_ERROR = 0;

	/**
	 * 获取SD卡上基础词库的地址
	 * 
	 * @return
	 */
	public static String getSDBaseDictionaryPath() {
		String base_file_path = getSaveDirectory() + File.separator
				+ Constants.BASE_DICTIONARY;
		return base_file_path;
	}

	public static String getSaveDirectory() {
		String save_dir = Environment.getExternalStorageDirectory()
				+ File.separator + Constants.SAVE_DIRECTORY;
		return save_dir;
	}

}
