package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextDistiller extends AbstractBatchFormDistiller<TextRowUnit> {

	private static final Logger logger = LoggerFactory.getLogger(TextDistiller.class);

	private String splitStr;
	
	public TextDistiller(DataFormTemplate<TextRowUnit> dataFormTemplate, String charset, boolean unzip, String splitStr) {
		super(dataFormTemplate, charset, unzip);
		this.splitStr = splitStr;
	}

	@Override
	protected List<TextRowUnit> distillDataFormUnit(String charset, byte[] dataSource) {

		List<TextRowUnit> rowUnitList = new ArrayList<TextRowUnit>();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(dataSource), Charset.forName(charset)));
		String readLine = null;
		try {
			while ((readLine = reader.readLine()) != null) {
				if ("".equals(readLine))
					continue;
				rowUnitList.add(new TextRowUnit(splitStr, readLine));
			}
		} catch (IOException e) {
			logger.error("error occured in reading line of data source", e);
		}

		return rowUnitList;
	}

}
