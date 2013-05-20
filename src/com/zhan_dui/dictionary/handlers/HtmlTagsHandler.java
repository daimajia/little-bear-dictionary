package com.zhan_dui.dictionary.handlers;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;
import android.util.Log;

public class HtmlTagsHandler implements TagHandler {

	private String parent;
	private int ol_count = 1;

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
			XMLReader xmlReader) {
		Log.e("tags", tag);
		if (opening == false) {
			return;
		}
		if (tag.equalsIgnoreCase("ol")) {
			parent = "ol";
			ol_count = 1;
		} else if (tag.equalsIgnoreCase("li")) {
			if (parent.equalsIgnoreCase("ol")) {
				Log.i("output", output.toString());
				output.append("\n   " + ol_count++ + ".");
			}
		}
	}

}
