package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrmf.taxsettlement.api.service.ActionParams;

public abstract class AbstractBatchFormDistiller<D extends DataFormUnit> implements BatchFormDistiller {

	private static final Logger logger = LoggerFactory.getLogger(AbstractBatchFormDistiller.class);

	private String charset;

	private boolean unzip;

	private DataFormTemplate<D> dataFormTemplate;

	protected AbstractBatchFormDistiller(DataFormTemplate<D> dataFormTemplate, String charset, boolean unzip) {
		super();
		this.dataFormTemplate = dataFormTemplate;
		this.charset = charset;
		this.unzip = unzip;
	}

	@Override
	public BatchFormDistillResult distill(byte[] fileBytes, Class<? extends ActionParams> exactalParamClass)
			throws IOException {

		byte[] dataSource = fileBytes;
		if (unzip) {
			dataSource = unZip(dataSource);
		}

		List<D> dataFormUnits = distillDataFormUnit(charset, dataSource);
		BatchFormDistillResult result = new BatchFormDistillResult();

		for (D dataFormUnit : dataFormUnits) {
			try {
				result.addDistill(dataFormTemplate.parse(dataFormUnit, exactalParamClass));
			} catch (Exception e) {
				String briefInfo = dataFormUnit.getBriefInfo();
				logger.error("fail to transform data form unit[{}] to action params", briefInfo);
				logger.error("error occured in data form unit parsing", e);
				result.addUndistill(briefInfo);
			}
		}
		return result;
	}

	private byte[] unZip(byte[] dataSource) throws IOException {
		GZIPInputStream zipInputStream = new GZIPInputStream(new ByteArrayInputStream(dataSource));
		ByteArrayOutputStream unzipOutStream = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[1024];
		int readLen = -1;
		while ((readLen = zipInputStream.read(byteBuffer)) > -1) {
			unzipOutStream.write(byteBuffer, 0, readLen);
		}
		return unzipOutStream.toByteArray();
	}

	protected abstract List<D> distillDataFormUnit(String charset, byte[] dataSource);

}
