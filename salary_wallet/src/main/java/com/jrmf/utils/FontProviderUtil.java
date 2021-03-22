package com.jrmf.utils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FontProviderUtil extends XMLWorkerFontProvider {
  @Override
  public Font getFont(final String fontname, final String encoding,
      final boolean embedded, final float size, final int style,
      final BaseColor color) {
    BaseFont bf = null;
    try {
      bf = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Font font = new Font(bf, size, style, color);
    font.setColor(color);
    return font;
  }
}

