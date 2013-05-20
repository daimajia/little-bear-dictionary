package com.zhan_dui.dictionary.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.viewpagerindicator.TitlePageIndicator;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.asynctasks.AddWordAsyncTask;
import com.zhan_dui.dictionary.asynctasks.QueryAsyncTask;
import com.zhan_dui.dictionary.datacenter.DictionaryDataCenter;
import com.zhan_dui.dictionary.listeners.IndicatorOnPageChangeListener;
import com.zhan_dui.dictionary.pageradapter.QueryManageFragmentPager;

public class QueryWordFragment extends SherlockFragment implements
		OnQueryTextListener {

	private QueryManageFragmentPager mQueryManageFragmentPager;
	private ViewPager mViewPager;
	private TitlePageIndicator mTitlePageIndicator;
	private String mQueryWord = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		DictionaryDataCenter.instance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(com.zhan_dui.dictionary.R.layout.query,
				container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mTitlePageIndicator = (TitlePageIndicator) view
				.findViewById(R.id.titles);
		mQueryManageFragmentPager = new QueryManageFragmentPager();
		mViewPager.setAdapter(mQueryManageFragmentPager);
		mTitlePageIndicator.setViewPager(mViewPager);
		mTitlePageIndicator
				.setOnPageChangeListener(new IndicatorOnPageChangeListener());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			mSearchView.setQuery("", false);
			break;
		case R.id.search:
			mSearchView.setIconified(false);
			mSearchView.requestFocusFromTouch();
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			break;
		case R.id.add:
			// DictionaryDataCenter.instance(getActivity()).addWord(mQueryWord);
			if (mQueryWord.length() != 0)
				new AddWordAsyncTask(mQueryWord, getActivity()).execute();
			break;
		default:
			break;
		}
		return true;
	}

	private SearchView mSearchView;
	private ActionBar mActionBar;

	@SuppressLint("NewApi")
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mActionBar = ((SherlockFragmentActivity) getActivity())
				.getSupportActionBar();
		mSearchView = new SearchView(mActionBar.getThemedContext());
		mSearchView.setQueryHint(getActivity().getString(R.string.query_hint));

		Theme theme = getActivity().getTheme();
		AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) mSearchView
				.findViewById(R.id.abs__search_src_text);
		TypedValue typedValue = new TypedValue();
		if (theme.resolveAttribute(R.attr.search_view_text_color, typedValue,
				true)) {
			autoCompleteTextView.setTextColor(typedValue.data);
		} else {
			autoCompleteTextView.setTextColor(Color.BLACK);
		}

		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_HOME_AS_UP);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
				Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		layoutParams.setMargins(0, 0, 15, 0);
		mActionBar.setCustomView(mSearchView, layoutParams);
		inflater.inflate(R.menu.search_split_menu, menu);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setId(android.R.id.inputArea);
		mSearchView.setIconifiedByDefault(true);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		DictionaryDataCenter.instance(getActivity()).clear();
		new QueryAsyncTask(getActivity(), mTitlePageIndicator,
				mQueryManageFragmentPager, query).execute();
		if (query != null && query.length() != 0) {
			mQueryWord = query;
		}
		mSearchView.clearFocus();
		return true;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mActionBar != null)
			mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE
					| ActionBar.DISPLAY_HOME_AS_UP);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}

}
