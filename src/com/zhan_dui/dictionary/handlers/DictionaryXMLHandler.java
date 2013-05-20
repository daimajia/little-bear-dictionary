package com.zhan_dui.dictionary.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.zhan_dui.dictionary.datacenter.DictionaryParseInfomation;

/**
 * 处理词典的XML解释文件
 * 
 * @author xuanqinanhai
 * 
 */

public class DictionaryXMLHandler extends DefaultHandler {

	private DictionaryParseInfomation dictionaryParseInfomation;
	private String currentFlag;

	public DictionaryParseInfomation getResults() {
		return this.dictionaryParseInfomation;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		dictionaryParseInfomation = new DictionaryParseInfomation();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String data = new String(ch, start, length);

		if (data.trim().length() == 0) {
			return;
		}
		if (currentFlag.equalsIgnoreCase("title")) {
			dictionaryParseInfomation.title = data;
		} else if (currentFlag.equalsIgnoreCase("queryword")) {
			dictionaryParseInfomation.queryWords.add(data);
		} else if (currentFlag.equalsIgnoreCase("sprintfstring")) {
			if (dictionaryParseInfomation.getLastOneEchoView().sprintfString == null)
				dictionaryParseInfomation.getLastOneEchoView().sprintfString = data;
			else {
				dictionaryParseInfomation.getLastOneEchoView().sprintfString += data;
			}
		} else if (currentFlag.equalsIgnoreCase("viewtype")) {
			dictionaryParseInfomation.getLastOneEchoView().viewType = data;
		} else if (currentFlag.equalsIgnoreCase("argcontent")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().argContent = data;
		} else if (currentFlag.equalsIgnoreCase("textsize")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().textSize = data;
		} else if (currentFlag.equalsIgnoreCase("textcolor")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().textColor = data;
		} else if (currentFlag.equalsIgnoreCase("text_padding_top")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().text_padding_top = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("text_padding_bottom")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().text_padding_bottom = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("text_padding_right")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().text_padding_right = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("text_padding_left")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().text_padding_left = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("view_padding_left")) {
			dictionaryParseInfomation.getLastOneEchoView().view_padding_left = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("view_padding_right")) {
			dictionaryParseInfomation.getLastOneEchoView().view_padding_right = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("view_padding_top")) {
			dictionaryParseInfomation.getLastOneEchoView().view_padding_top = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("view_padding_bottom")) {
			dictionaryParseInfomation.getLastOneEchoView().view_padding_bottom = Integer
					.parseInt(data);
		} else if (currentFlag.equalsIgnoreCase("textstyle")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().textStyle = data;
		} else if (currentFlag.equalsIgnoreCase("action")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().action = data;
		} else if (currentFlag.equalsIgnoreCase("dictionary_table")) {
			dictionaryParseInfomation.table = data;
		} else if (currentFlag.equalsIgnoreCase("type")) {
			dictionaryParseInfomation.getLastOneEchoView().getLastOneArg().type = data;
		}
	};

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		currentFlag = localName;
		if (localName.equalsIgnoreCase("view")) {
			dictionaryParseInfomation.addOneEchoView();
		} else if (localName.equalsIgnoreCase("args")) {
			dictionaryParseInfomation.getLastOneEchoView().addOneArg();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

	}
}
