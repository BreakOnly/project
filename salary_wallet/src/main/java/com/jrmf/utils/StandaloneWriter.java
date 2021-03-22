package com.jrmf.utils;

import java.io.IOException;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class StandaloneWriter extends XMLWriter{
	
	protected void writeDeclaration() throws IOException {
		  OutputFormat format = getOutputFormat();
          format.setNewLineAfterDeclaration(false); // xml声明与内容是否添加空行
          format.setSuppressDeclaration(false); // 是否设置xml声明头部 false：添加
          format.setNewlines(true); // 设置分行stringWriter, format
		  String encoding = format.getEncoding();
		 
		  if (!format.isSuppressDeclaration()) {
		    if (encoding.equals("UTF8")) {
		      writer.write("<?xml version=\"1.0\"");
		 
		      if (!format.isOmitEncoding()) {
		        writer.write(" encoding=\"UTF-8\"");
		      }
		 
		      writer.write(" standalone=\"no\"");
		      writer.write("?>");
		    } else {
		      writer.write("<?xml version=\"1.0\"");
		 
		      if (!format.isOmitEncoding()) {
		        writer.write(" encoding=\"" + encoding + "\"");
		      }
		 
		      writer.write(" standalone=\"no\"");
		      writer.write("?>");
		    }
		 
		    if (format.isNewLineAfterDeclaration()) {
		      println();
		    }
		  }
		}
}
