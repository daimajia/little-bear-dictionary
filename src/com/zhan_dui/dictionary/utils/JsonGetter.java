package com.zhan_dui.dictionary.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * 获取Json文本
 * 
 * @author xuanqinanhai
 * 
 */
public class JsonGetter {
	private JsonGetter() {

	}

	/**
	 * get json string from url
	 * 
	 * @param JsonUrl
	 * @return
	 * @throws Exception
	 */
	public static String get(String JsonUrl) throws Exception {
		URL url = new URL(JsonUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setConnectTimeout(10000);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(urlConnection.getInputStream()));
		StringBuilder jsonResultBuilder = new StringBuilder();
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			jsonResultBuilder.append(line);
		}
		return jsonResultBuilder.toString();
	}
}
