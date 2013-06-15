package com.zhan_dui.dictionary.fragments;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.umeng.fb.UMFeedbackService;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.FriendshipsAPI;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.utils.Constants;

public class AboutMeFragment extends SherlockFragment implements
		OnClickListener {

	private ActionBar mActionBar;
	private Weibo mWeibo;
	private Boolean mSSOAuth;
	private SsoHandler mSsoHandler;
	private Oauth2AccessToken mAccessToken;
	private static Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWeibo = Weibo.getInstance(Constants.WEIBO_APP_KEY,
				Constants.WEIBO_REDIRECT_URI);
		SherlockFragmentActivity sherlockFragmentActivity = (SherlockFragmentActivity) getActivity();
		mActionBar = sherlockFragmentActivity.getSupportActionBar();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mContext = view.getContext();
		mActionBar.setTitle(R.string.about);
		Button weiboButton = (Button) view.findViewById(R.id.weibo);
		Button feedbackButton = (Button) view.findViewById(R.id.feedback);
		weiboButton.setOnClickListener(this);
		feedbackButton.setOnClickListener(this);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		Boolean followed = sharedPreferences.getBoolean("follow", false);
		if (followed) {
			weiboButton.setText("感谢您的关注");
			weiboButton.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.weibo:
			try {
				Class.forName("com.weibo.sdk.android.sso.SsoHandler");
				mSSOAuth = true;
			} catch (ClassNotFoundException e) {
				mSSOAuth = false;
			}

			if (mSSOAuth == true) {
				mSsoHandler = new SsoHandler(getActivity(), mWeibo);
				mSsoHandler.authorize(new AuthDialogListener());
			} else {
				mWeibo.authorize(getActivity(), new AuthDialogListener());
			}

			break;
		case R.id.feedback:
			UMFeedbackService.openUmengFeedbackSDK(getActivity());
			break;
		default:
			break;
		}
	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {

		}

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			mAccessToken = new Oauth2AccessToken(token, expires_in);
			if (mAccessToken.isSessionValid()) {
				AccessTokenKeeper.keepAccessToken(getActivity(), mAccessToken);
				FriendshipsAPI friendshipsAPI = new FriendshipsAPI(mAccessToken);
				friendshipsAPI.create(Constants.WEIBO_BEAR_ID,
						Constants.WEIBO_BEAR_NAME, new FollowListener());
			}
		}

		@Override
		public void onError(WeiboDialogError e) {
			try {
				JSONObject jsonObject = new JSONObject(e.getMessage());
				if (jsonObject.has("error_code")
						&& jsonObject.getInt("error_code") == 20506) {
					Message msg = Message.obtain(FollowHandler, 0);
					msg.sendToTarget();
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
				Toast.makeText(getActivity(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	private static Handler FollowHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
				Editor editor = PreferenceManager.getDefaultSharedPreferences(
						mContext).edit();
				editor.putBoolean("follow", true);
				editor.commit();
				break;
			case -1:
				Toast.makeText(mContext, "关注失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	};

	class FollowListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			Message msg = Message.obtain(FollowHandler, 0, response);
			msg.sendToTarget();
		}

		@Override
		public void onError(WeiboException e) {
			Message msg = Message.obtain(FollowHandler, -1, e);
			msg.sendToTarget();
		}

		@Override
		public void onIOException(IOException e) {
			Message msg = Message.obtain(FollowHandler, -1, e);
			msg.sendToTarget();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
}
