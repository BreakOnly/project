package com.jrmf.utils.pdf.replace;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * pdf渲染监听,当找到渲染的文本时，得到文本的坐标x,y,w,h
 * @author chonglulu
 * @date : 2019年2月19日10:09:51
 */
public class PositionRenderListener implements RenderListener{
	
	private List<String> findText;
	private float defaultH;
	private float fixHeight;
	public PositionRenderListener(List<String> findText, float defaultH,float fixHeight) {
		this.findText = findText;
		this.defaultH = defaultH;
		this.fixHeight = fixHeight;
	}
 
	PositionRenderListener(List<String> findText) {
		this.findText = findText;
		this.defaultH = 12;
		this.fixHeight = 2;
	}
	
	@Override
	public void beginTextBlock() {
		
	}
 
	@Override
	public void endTextBlock() {
		
	}
 
	@Override
	public void renderImage(ImageRenderInfo imageInfo) {
	}
 
	private Map<String, ReplaceRegion> result = new HashMap<>();
	@Override
	public void renderText(TextRenderInfo textInfo) {
		String text = textInfo.getText();
		for (String keyWord : findText) {
			if (null != text && text.equals(keyWord)){
				Float bound = textInfo.getBaseline().getBoundingRectange();
				ReplaceRegion region = new ReplaceRegion(keyWord);
				region.setH(bound.height == 0 ? defaultH : bound.height);
				region.setW(bound.width);
				region.setX(bound.x);
				region.setY(bound.y-this.fixHeight);
				result.put(keyWord, region);
			}
		}
	}
 
	public Map<String, ReplaceRegion> getResult() {
		for (String key : findText) {
            this.result.putIfAbsent(key, null);
		}
		return this.result;
	}
}