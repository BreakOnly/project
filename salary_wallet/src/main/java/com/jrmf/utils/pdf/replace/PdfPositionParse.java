package com.jrmf.utils.pdf.replace;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 解析PDF中文本的x,y位置
 * @author chonglulu
 * @date : 2019年2月19日09:59:14
 */
public class PdfPositionParse {
 
	private PdfReader reader;
    /**
     * 需要查找的文本
     */
	private List<String> findText = new ArrayList<>();
	private PdfReaderContentParser parser;
 
	private boolean needClose = true;


    PdfPositionParse(PdfReader reader){
		this.reader = reader;
		parser = new PdfReaderContentParser(reader);
		needClose = false;
	}
 
	void addFindText(String text){
		this.findText.add(text);
	}
	
	/**
	 * 解析文本
	 */
	public Map<String, ReplaceRegion> parse() throws IOException{
		try{
			if(this.findText.size() == 0){
				throw new NullPointerException("没有需要查找的文本");
			}
			PositionRenderListener listener = new PositionRenderListener(this.findText);
			parser.processContent(1, listener);
			return listener.getResult();
		}finally{
			if(reader != null && needClose){
				reader.close();
			}
		}
	}
}