package com.jrmf.taxsettlement.api.task;

import com.jrmf.utils.FtpTool;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.jrmf.taxsettlement.api.gateway.restful.APIDefinitionConstants;
import com.jrmf.taxsettlement.util.file.FileRepository;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractMerchantDataFileGenerator implements MerchantDataFileGenerator {

	private static final char DEFAULT_FIELD_DIV = '|';

	private static final String DEFAULT_ROW_END = "\r\n";
	
	private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
	
	@Autowired
	private FileRepository fileRepository;

	@Value("${fileRepositoryRootPath}")
	private String fileRepositoryRootPath;

	private boolean zip = true;

	public void generateDataFile(Map<String, Object> params) throws Exception {
		String merchantId = (String) params.get(APIDefinitionConstants.CFN_MERCHANT_ID);
		List<String[]> dataRows = getDataRows(params);

		if (dataRows.size() == 0){
			return;
		}

		String fileName = getNewFileName(params);
		if (zip) {
			fileName = new StringBuilder(fileName).append(".gzip").toString();
		}

		FtpTool.uploadFile(
				new StringBuilder(fileRepositoryRootPath).append(FileRepository.PATH_DIV).append(merchantId)
						.append(FileRepository.PATH_DIV).toString(),
				fileName,
				getFileDataInputStream(dataRows));
	}

	private InputStream getFileDataInputStream(List<String[]> dataRows) throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for(String[] dataRow : dataRows) {
			for(int i = 0; i < dataRow.length; i++) {
				if(i > 0)
					out.write(DEFAULT_FIELD_DIV);
				if(dataRow[i] != null)
					out.write(dataRow[i].getBytes(DEFAULT_CHARSET));
			}
			out.write(DEFAULT_ROW_END.getBytes(DEFAULT_CHARSET));
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	protected abstract String getNewFileName(Map<String, Object> params);

	protected abstract List<String[]> getDataRows(Map<String, Object> params);
}
