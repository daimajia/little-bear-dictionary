/**
 * @ClassName:QueryProcessCenter.java
 */
package com.zhan_dui.dictionary.datacenter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.xml.sax.SAXException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhan_dui.dictionary.datacenter.DictionaryParseInfomation.EchoViews;
import com.zhan_dui.dictionary.datacenter.DictionaryParseInfomation.TextArg;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.handlers.DictionaryXMLHandler;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.DisplayUtils;

/**
 * @Description:负责整个词典单词的查询和排版工作
 * 
 */
public class QueryProcessor {

	private static QueryProcessor mQueryProcessor;
	/**
	 * 缓存所有的XML转换信息
	 */
	private static HashMap<String, DictionaryParseInfomation> mCacheXMLInformation = new HashMap<String, DictionaryParseInfomation>();
	/**
	 * 缓存所有正在启用的字典
	 */
	private static ArrayList<DictionaryInfo> mCacheUsingDictionaries = new ArrayList<QueryProcessor.DictionaryInfo>();
	/**
	 * 缓存所有的查询过的单词ID，这样，二次查询会极速无比
	 */
	private static HashMap<String, Integer> mCacheWordID = new HashMap<String, Integer>();
	private static final int mCacheCountForWordsID = 60;

	private final static String DB_PATH = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ Constants.SAVE_DIRECTORY + File.separator;

	public final static String DB_BASE_DIC = "dictionary_word.sqlite";

	private static Boolean mIsGlobal = false;
	private static Boolean mIsIndividual = false;

	private static Boolean mIsIndividualTermNum = false;
	private static Boolean mIsIndividualProperty = false;
	private static Boolean mIsIndividualChinese = false;
	private static Boolean mIsIndividualEnglish = false;
	private static Boolean mIsIndividualExample = false;

	private static class DictionaryInfo {
		public DictionaryInfo(String DicName, String DicFileName,
				String DicConfigFileName, String DicSaveDir) {
			mDicDir = DicSaveDir;
			mDicFileName = DicFileName;
			mDicConfigFileName = DicConfigFileName;
			mDicName = DicName;
		}

		/***
		 * 词典所在子目录，所有词典都会被解压在sdcard/dictionary下，mDicDir只是该字典的目录
		 */
		public String mDicDir;
		/**
		 * 字典的名称 如 collins.dic 实际是一个sqlite文件
		 */
		public String mDicFileName;
		/**
		 * 字典的配置文件 config-[字典名称]
		 */
		public String mDicConfigFileName;
		/**
		 * 字典的中文名
		 */
		public String mDicName;
	}

	public class QueryResult {
		private String DictionaryName;
		private ScrollView DictionaryView;

		public QueryResult(String dictionaryName, ScrollView dictionaryView) {
			DictionaryName = dictionaryName;
			DictionaryView = dictionaryView;
		}

		public String getDictionaryName() {
			return DictionaryName;
		}

		public ScrollView getDictionaryView() {
			return DictionaryView;
		}

	}

	private static int mGlobalSize, mGlobalColor, mTermNumSize, mTermNumColor,
			mPropertySize, mPropertyColor, mChineseSize, mChineseColor,
			mEnglishSize, mEnglishColor, mExampleSize, mExampleColor;

	// 更新排版的偏好设置
	public static void updateFromPreference(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		mIsGlobal = sharedPreferences.getBoolean("global_effect", false);
		mIsIndividual = sharedPreferences.getBoolean("individual", false);
		mIsIndividualTermNum = sharedPreferences.getBoolean("term_number",
				false);
		mIsIndividualProperty = sharedPreferences.getBoolean(
				"individual_property", false);
		mIsIndividualChinese = sharedPreferences.getBoolean(
				"chinese_explanation", false);
		mIsIndividualEnglish = sharedPreferences.getBoolean(
				"english_explanation", false);
		mIsIndividualExample = sharedPreferences.getBoolean("example", false);

		mGlobalSize = sharedPreferences.getInt("global_effect_size", 14);
		mGlobalColor = sharedPreferences.getInt("global_effect_color",
				Color.WHITE);

		mPropertySize = sharedPreferences
				.getInt("individual_property_size", 14);
		mPropertyColor = sharedPreferences.getInt("individual_property_color",
				Color.WHITE);
		mChineseSize = sharedPreferences.getInt("chinese_explanation_size", 14);
		mChineseColor = sharedPreferences.getInt("chinese_explanation_color",
				Color.WHITE);
		mEnglishSize = sharedPreferences.getInt("english_explanation_size", 14);
		mEnglishColor = sharedPreferences.getInt("english_explanation_color",
				Color.WHITE);
		mExampleSize = sharedPreferences.getInt("example_size", 12);
		mExampleColor = sharedPreferences.getInt("example_color", Color.WHITE);
	}

	private QueryProcessor(Context context) {
		updateFromPreference(context);
		updateCacheDictionaryList(context);
	}

	public static QueryProcessor instance(Context context) {
		if (mQueryProcessor == null) {
			mQueryProcessor = new QueryProcessor(context);
		}
		return mQueryProcessor;
	}

	public static void updateCacheDictionaryList(Context context) {
		mCacheUsingDictionaries.clear();
		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getReadableDatabase();
		Cursor cursor = sqLiteDatabase
				.rawQuery(
						"select * from `dictionary_list` where `dictionary_show`='1' and `dictionary_downloaded`='1' order by `dictionary_order` asc",
						null);

		while (cursor.moveToNext()) {
			String dicname = cursor.getString(cursor
					.getColumnIndex("dictionary_name"));
			String filename = cursor.getString(
					cursor.getColumnIndex("dictionary_save_name")).replace(
					"zip", "dic");
			String dirname = filename.substring(0, filename.length() - 4);
			String configFileName = "config-" + filename;
			DictionaryInfo dictionaryInfo = new DictionaryInfo(dicname,
					filename, configFileName, dirname);
			mCacheUsingDictionaries.add(dictionaryInfo);
		}
		sqLiteDatabase.close();
	}

	public int getDictionaryUsingCount() {
		return mCacheUsingDictionaries.size();
	}

	public QueryResult query(Context context, String word, int position)
			throws ParserConfigurationException, SAXException, IOException {
		DictionaryInfo dictionaryInfo = mCacheUsingDictionaries.get(position);
		ScrollView dictionaryView = query(context, word,
				dictionaryInfo.mDicDir, dictionaryInfo.mDicFileName,
				dictionaryInfo.mDicConfigFileName);
		String dictionaryName = mCacheUsingDictionaries.get(position).mDicName;
		return new QueryResult(dictionaryName, dictionaryView);
	}

	/**
	 * 查询并且生成scrollView排版
	 * 
	 * @param context
	 *            不解释
	 * @param word
	 *            要查询的单词
	 * @param filePath
	 *            sqlite文件地址
	 * @param xmlPath
	 *            对应该sqlite的配置文件地址
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private ScrollView query(Context context, String word, String saveDir,
			String filePath, String xmlPath)
			throws ParserConfigurationException, SAXException, IOException {

		// start parse the dictionary config-xml
		DictionaryParseInfomation dictionaryParseInfomation = parseConfigureXML(
				saveDir, xmlPath);
		// end parse

		// Prepare some query variables
		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(
				Constants.getSaveDirectory() + File.separator + saveDir
						+ File.separator + filePath, null,
				SQLiteDatabase.OPEN_READWRITE);
		int word_id = getWordID(word);
		String table = dictionaryParseInfomation.table;
		String[] columns = (String[]) (dictionaryParseInfomation.queryWords
				.toArray(new String[0]));
		String[] selectionArgs = { word_id + "" };
		Cursor cursor = sqLiteDatabase.query(table, columns, "word_id=?",
				selectionArgs, null, null, null);
		// end prepare some query variables
		int index = 1;
		LinearLayout wrapperLinearLayout = new LinearLayout(context);
		wrapperLinearLayout.setOrientation(LinearLayout.VERTICAL);
		ViewGroup.LayoutParams layoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		wrapperLinearLayout.setLayoutParams(layoutParams);
		TextView textView = null;
		while (cursor.moveToNext()) {
			textView = processOneItem(context, cursor,
					dictionaryParseInfomation, index++);
			wrapperLinearLayout.addView(textView);
		}
		cursor.close();
		sqLiteDatabase.close();
		ScrollView scrollView = new ScrollView(context);
		scrollView.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		scrollView.addView(wrapperLinearLayout);
		return scrollView;
	}

	/**
	 * 对每一个单词条目生成textview
	 * 
	 * @param context
	 * @param cursor
	 * @param dictionaryParseInfomation
	 * @param index
	 * @return
	 */
	private TextView processOneItem(Context context, Cursor cursor,
			DictionaryParseInfomation dictionaryParseInfomation, int index) {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		TextView textView = new TextView(context);
		for (EchoViews echoView : dictionaryParseInfomation.echoViews) {

			textView.setLayoutParams(layoutParams);
			textView.setPadding(
					DisplayUtils.dip2px(context, echoView.view_padding_left),
					DisplayUtils.dip2px(context, echoView.view_padding_top),
					DisplayUtils.dip2px(context, echoView.view_padding_right),
					DisplayUtils.dip2px(context, echoView.view_padding_bottom));
			ArrayList<SpannableString> contents = new ArrayList<SpannableString>();
			CharacterStyle colorCharacterStyle = null, sizeCharacterStyle = null;
			for (TextArg textArg : echoView.sprintfArgs) {

				String content = cursor.getString(cursor
						.getColumnIndex(textArg.argContent)) + "  ";
				SpannableString partString = dealAction(textArg, content);
				colorCharacterStyle = null;
				sizeCharacterStyle = null;

				if (mIsGlobal) {
					colorCharacterStyle = new ForegroundColorSpan(mGlobalColor);
					sizeCharacterStyle = new AbsoluteSizeSpan(mGlobalSize);
				}

				if (mIsIndividual) {
					if (mIsIndividualProperty
							&& textArg.type.equalsIgnoreCase("property")) {
						colorCharacterStyle = new ForegroundColorSpan(
								mPropertyColor);
						sizeCharacterStyle = new AbsoluteSizeSpan(mPropertySize);
					} else if (mIsIndividualEnglish
							&& textArg.type.equalsIgnoreCase("english")) {
						colorCharacterStyle = new ForegroundColorSpan(
								mEnglishColor);
						sizeCharacterStyle = new AbsoluteSizeSpan(mEnglishSize);
					} else if (mIsIndividualChinese
							&& textArg.type.equalsIgnoreCase("chinese")) {
						colorCharacterStyle = new ForegroundColorSpan(
								mChineseColor);
						sizeCharacterStyle = new AbsoluteSizeSpan(mChineseSize);
					} else if (mIsIndividualExample
							&& textArg.type.equalsIgnoreCase("example")) {
						colorCharacterStyle = new ForegroundColorSpan(
								mExampleColor);
						sizeCharacterStyle = new AbsoluteSizeSpan(mExampleSize);
					}
				}

				if (colorCharacterStyle == null || sizeCharacterStyle == null) {
					if (mIsGlobal) {
						colorCharacterStyle = new ForegroundColorSpan(
								mGlobalColor);
						sizeCharacterStyle = new AbsoluteSizeSpan(mGlobalSize);
					} else {
						// theme style
					}
				}

				partString.setSpan(colorCharacterStyle, 0, partString.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				partString.setSpan(sizeCharacterStyle, 0, partString.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				contents.add(partString);
			}
			CharSequence resultContent = TextUtils.concat(contents
					.toArray(new SpannableString[0]));
			SpannableString IndexSpannableString = new SpannableString(index
					+ ".");

			if (mIsIndividual && mIsIndividualTermNum) {
				colorCharacterStyle = new ForegroundColorSpan(mTermNumColor);
				sizeCharacterStyle = new AbsoluteSizeSpan(mTermNumSize);
			} else if (mIsGlobal) {
				colorCharacterStyle = new ForegroundColorSpan(mGlobalColor);
				sizeCharacterStyle = new AbsoluteSizeSpan(mGlobalSize);
			} else {
				// Theme style
			}
			IndexSpannableString.setSpan(colorCharacterStyle, 0,
					IndexSpannableString.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			IndexSpannableString.setSpan(sizeCharacterStyle, 0,
					IndexSpannableString.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			resultContent = TextUtils.concat(IndexSpannableString,
					resultContent);

			textView.setText(resultContent);
		}
		return textView;
	}

	// 解决XML中的行为属性
	private SpannableString dealAction(TextArg arg, String content) {
		if (arg.action != null) {
			if (arg.action.equals("split")) {
				// |||
				String[] examples = content.split("\\|\\|\\|");
				content = "\n";
				for (String example : examples) {
					content += example + "\n";
				}
				content = "\n" + content;
			}
		}
		return new SpannableString(content);
	}

	private DictionaryParseInfomation parseConfigureXML(String childDir,
			String xmlPath) throws ParserConfigurationException, SAXException,
			IOException {
		DictionaryParseInfomation dictionaryParseInfomation;
		if (mCacheXMLInformation.containsKey(xmlPath)) {
			dictionaryParseInfomation = mCacheXMLInformation.get(xmlPath);
		} else {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			DictionaryXMLHandler dictionaryXMLHandler = new DictionaryXMLHandler();
			String xmlFilePath = Constants.getSaveDirectory() + File.separator
					+ childDir + File.separator + xmlPath;
			saxParser.parse(new File(xmlFilePath), dictionaryXMLHandler);
			dictionaryParseInfomation = dictionaryXMLHandler.getResults();
			mCacheXMLInformation.put(xmlPath, dictionaryParseInfomation);
		}
		return dictionaryParseInfomation;
	}

	public static final int WORD_NOT_EXSIST = -1;

	// 获取单词的ID
	public int getWordID(String word) {

		if (mCacheWordID.containsKey(word)) {
			return mCacheWordID.get(word);
		}

		SQLiteDatabase iddatabase = SQLiteDatabase.openDatabase(DB_PATH
				+ DB_BASE_DIC, null, SQLiteDatabase.OPEN_READWRITE);
		String[] tableStrings = { "id" };
		Cursor cursor = iddatabase.query("word", tableStrings, "word='" + word
				+ "'", null, null, null, null);
		int id = WORD_NOT_EXSIST;
		if (cursor.moveToNext()) {
			id = cursor.getInt(0);
		} else {
			// TODO:处理ID不存在时候的状态
		}
		iddatabase.close();

		if (mCacheWordID.size() == mCacheCountForWordsID) {
			mCacheWordID.clear();
		} else {
			mCacheWordID.put(word, id);
		}
		return id;
	}

}
