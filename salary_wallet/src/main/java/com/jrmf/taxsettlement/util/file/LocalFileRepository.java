package com.jrmf.taxsettlement.util.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class LocalFileRepository implements FileRepository {

	private final String basePath;

	private final String publishURLContext;

	public LocalFileRepository(String basePath, String publishURLContext) {
		super();
		this.basePath = basePath;
		this.publishURLContext = publishURLContext;
	}

	@Override
	public List<String> findFile(String directoryName, String fileNameMode) {

		List<String> foundFilePathList = new ArrayList<String>();
		FileNameMatcher matcher = FileNameMatcher.get(fileNameMode);

		File directory = new File(
				new StringBuilder(basePath).append(FileRepository.PATH_DIV).append(directoryName).toString());
		if(!directory.exists()) {
			return foundFilePathList;
		}
		
		for (File subFile : directory.listFiles()) {
			if (!subFile.isFile()) {
				continue;
			}

			String fileName = subFile.getName();
			if (matcher.match(fileName)) {
				foundFilePathList
						.add(new StringBuilder(directoryName).append(FileRepository.PATH_DIV).append(fileName).toString());
			}

		}
		return foundFilePathList;
	}

	@Override
	public void renameFile(String existFilePath, String newFilePath) {
		new File(new StringBuilder(basePath).append(FileRepository.PATH_DIV).append(existFilePath).toString()).renameTo(
				new File(new StringBuilder(basePath).append(FileRepository.PATH_DIV).append(newFilePath).toString()));
	}

	@Override
	public String getPublishURLContext() {
		return publishURLContext;
	}

	@Override
	public void createFile(String filePath, boolean zip, InputStream fileDataInputStream) throws IOException {
		OutputStream fileOut = null;
		try {
			String fileFullPath = new StringBuilder(basePath).append(PATH_DIV).append(filePath).toString();
			String superDirectoryPath = fileFullPath.substring(0, fileFullPath.lastIndexOf(PATH_DIV));
			File superDirectory = new File(superDirectoryPath);
			if (!superDirectory.exists()) {
				superDirectory.mkdirs();
			}

			fileOut = new FileOutputStream(new File(fileFullPath));
			if (zip) {
				fileOut = new GZIPOutputStream(fileOut);
			}
			byte[] fileByteBuffer = new byte[1024];
			int readLen = -1;
			while ((readLen = fileDataInputStream.read(fileByteBuffer)) >= 0) {
				fileOut.write(fileByteBuffer, 0, readLen);
			}
			fileOut.flush();
		} finally {
			if (fileOut != null) {
				fileOut.close();
			}
			fileDataInputStream.close();
		}
	}

	@Override
	public byte[] loadFile(String filePath, boolean zip) throws IOException {
		String fileFullPath = new StringBuilder(basePath).append(PATH_DIV).append(filePath).toString();
		try (InputStream in = new FileInputStream(fileFullPath)) {
			byte[] fileByteBuffer = new byte[1024];
			ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
			int readLen = -1;
			while ((readLen = in.read(fileByteBuffer)) > -1) {
				fileBytes.write(fileByteBuffer, 0, readLen);
			}
			return fileBytes.toByteArray();
		}
	}

	public String getBasePath() {
		return basePath;
	}
}
