package com.jrmf.taxsettlement.util.file;

abstract class FileNameMatcher {

	protected String content;

	protected FileNameMatcher(String content) {
		this.content = content;
	}

	public static FileNameMatcher get(String fileNameMode) {

		FileNameMatcher matcher = null;
		if (fileNameMode.startsWith(FileRepository.ANY_PART_SIGNAL)
				&& !fileNameMode.endsWith(FileRepository.ANY_PART_SIGNAL)) {
			matcher = new SuffixMatcher(
					fileNameMode.substring(fileNameMode.indexOf(FileRepository.ANY_PART_SIGNAL) + 1));
		} else if (!fileNameMode.startsWith(FileRepository.ANY_PART_SIGNAL)
				&& fileNameMode.endsWith(FileRepository.ANY_PART_SIGNAL)) {
			matcher = new PrefixMatcher(
					fileNameMode.substring(0, fileNameMode.indexOf(FileRepository.ANY_PART_SIGNAL)));
		} else if (!fileNameMode.startsWith(FileRepository.ANY_PART_SIGNAL)
				&& !fileNameMode.endsWith(FileRepository.ANY_PART_SIGNAL)) {
			int start = fileNameMode.indexOf(FileRepository.ANY_PART_SIGNAL) + 1;
			int end = fileNameMode.lastIndexOf(FileRepository.ANY_PART_SIGNAL);
			matcher = new MiddleContentMatcher(fileNameMode.substring(start, Math.max(start, end)));
		} else {
			matcher = new FullNameMatcher(fileNameMode);
		}

		return matcher;
	}

	public abstract boolean match(String fileName);

	private static class MiddleContentMatcher extends FileNameMatcher {

		protected MiddleContentMatcher(String content) {
			super(content);
		}

		@Override
		public boolean match(String fileName) {
			return fileName.contains(content);
		}
	}

	private static class PrefixMatcher extends FileNameMatcher {

		protected PrefixMatcher(String content) {
			super(content);
		}

		@Override
		public boolean match(String fileName) {
			return fileName.startsWith(content);
		}
	}

	private static class SuffixMatcher extends FileNameMatcher {

		protected SuffixMatcher(String content) {
			super(content);
		}

		@Override
		public boolean match(String fileName) {
			return fileName.endsWith(content);
		}
	}

	private static class FullNameMatcher extends FileNameMatcher {

		protected FullNameMatcher(String content) {
			super(content);
		}

		@Override
		public boolean match(String fileName) {
			return content.equals(fileName);
		}
	}

}