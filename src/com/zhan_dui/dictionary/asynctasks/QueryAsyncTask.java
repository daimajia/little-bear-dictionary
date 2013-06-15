package com.zhan_dui.dictionary.asynctasks;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.viewpagerindicator.PageIndicator;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.datacenter.DictionaryDataCenter;
import com.zhan_dui.dictionary.datacenter.QueryProcessor;
import com.zhan_dui.dictionary.datacenter.QueryProcessor.QueryResult;
import com.zhan_dui.dictionary.fragments.QueryWordFragment;

/**
 * 负责查询的AsyncTask
 * 
 * @author xuanqinanhai
 * 
 */
public class QueryAsyncTask extends AsyncTask<Void, Object, Boolean> {
	private PageIndicator mPageIndicator;
	private PagerAdapter mPagerAdapter;
	private DictionaryDataCenter mDataCenter;
	private String mQueryWord;
	private static Context mContext;

	public final static int MSG_NO_USEFUL_DICTIONARY = 100;
	public final static int MSG_HAS_USEFUL_DICTIONARY = 200;
	public final static int MSG_NOT_FOUND_WORD = 300;

	private static QueryProcessor mQueryProcessor;

	public QueryAsyncTask(Context context, PageIndicator pageIndicator,
			PagerAdapter adapter, String queryWord) {
		mContext = context;
		mPagerAdapter = adapter;
		mQueryWord = queryWord;
		mPageIndicator = pageIndicator;
		mDataCenter = DictionaryDataCenter.instance(context);
		mQueryProcessor = QueryProcessor.instance(mContext);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mQueryProcessor.getDictionaryUsingCount() == 0) {
			// 如果开启了的字典个数为零
			Message.obtain(QueryWordFragment.QueryMessageHandler,
					QueryAsyncTask.MSG_NO_USEFUL_DICTIONARY).sendToTarget();
			cancel(false);
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean result = true;
		if (isCancelled()) {
			return true;
		}
		for (int i = 0; i < mQueryProcessor.getDictionaryUsingCount(); i++) {
			try {
				QueryResult queryResult = mQueryProcessor.query(mContext,
						mQueryWord, i, mQueryEventHandle);
				if (queryResult != null) {
					publishProgress(queryResult.getDictionaryName(),
							queryResult.getDictionaryView());
				}

			} catch (ParserConfigurationException e) {
				result = false;
				e.printStackTrace();
			} catch (SAXException e) {
				result = false;
				e.printStackTrace();
			} catch (IOException e) {
				result = false;
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
		Log.i("Lin", "get " + (String) values[0]);
		mDataCenter.addDictionaryView((String) values[0], (View) values[1]);
		mPagerAdapter.notifyDataSetChanged();
		mPageIndicator.notifyDataSetChanged();
	}

	private static Handler mQueryEventHandle = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == QueryProcessor.MSG_WORD_NOT_EXISIT) {
				Toast.makeText(mContext, R.string.no_this_word,
						Toast.LENGTH_LONG).show();
			}
		};
	};

}
