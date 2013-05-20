package com.zhan_dui.dictionary.datacenter;

import java.util.ArrayList;

/**
 * about Dictionary information...parse from configuration xml
 * 
 * @author xuanqinanhai
 * 
 */
public class DictionaryParseInfomation {

	public String title;// Dictionary Name
	public String table;// Query table
	public ArrayList<String> queryWords = new ArrayList<String>();// query what cols?
	public ArrayList<EchoViews> echoViews = new ArrayList<DictionaryParseInfomation.EchoViews>();// echo views

	public class EchoViews {//EchoView class represent enough print information
		public String sprintfString;// Stirng.Format("",...);
		public ArrayList<TextArg> sprintfArgs = new ArrayList<TextArg>();// args
		public String viewType;// what kind of view to new...

		public int view_padding_left = 0, view_padding_right = 0,
				view_padding_top = 0, view_padding_bottom = 0;

		@Override
		public String toString() {
			return sprintfString + " " + sprintfArgs.toString() + " "
					+ viewType;
		}

		public void addOneArg() {
			sprintfArgs.add(new TextArg());
		}

		public TextArg getLastOneArg() {
			return sprintfArgs.get(sprintfArgs.size() - 1);
		}
	}

	public class TextArg {
		public String textSize = "normal";
		public String textColor = "#000000";
		public String action = null;
		public String argContent;
		public String textStyle = "normal";
		public int text_padding_left = 0, text_padding_right = 0,
				text_padding_top = 0, text_padding_bottom = 0;
		public String type = "english";
	}

	public void addOneEchoView() {
		echoViews.add(new EchoViews());
	}

	public EchoViews getLastOneEchoView() {
		return echoViews.get(echoViews.size() - 1);
	}

	@Override
	public String toString() { //use to debug
		System.out.println("title:" + title);
		System.out.println("querywords:" + queryWords.toString());
		System.out.println("echoviews:");
		for (EchoViews echoView : echoViews) {
			System.out.println(echoView.toString());
		}
		return super.toString();
	}
}
