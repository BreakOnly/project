package com.jrmf.taxsettlement.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileRepository {

	String PATH_DIV = "/";

	String ANY_PART_SIGNAL = "*";

	List<String> findFile(String directory, String fileNameMode);

	void renameFile(String existFilePath, String newFilePath);

	String getPublishURLContext();

	void createFile(String filePath, boolean zip, InputStream fileDataInputStream) throws IOException;

	byte[] loadFile(String filePath, boolean zip) throws IOException;;

}
