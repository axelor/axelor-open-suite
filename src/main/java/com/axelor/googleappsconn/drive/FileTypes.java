package com.axelor.googleappsconn.drive;

import java.util.ArrayList;
import java.util.List;

/**
 * having the static values of all the file mime-types
 */
public class FileTypes {

	public static final String DIRECTORY = "application/vnd.google-apps.folder";
	public static final String OPEN_DOCUMENT = "application/vnd.oasis.opendocument.text";
	public static final String MS_DOCUMENT = "application/msword";
	public static final String MS_DOCUMENTX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String MS_SPREADSHEET = "application/vnd.ms-excel";
	public static final String MS_SPREADSHEETX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String OPEN_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";
	public static final String MS_POWERPOINT = "application/vnd.ms-powerpoint";
	public static final String MS_POWERPOINTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String PLAIN_TEXT = "text/plain";
	public static final String CSV = "text/csv";
	public static final String PDF_DOCUMENT = "application/pdf";
	public static final String CSV_FILE = "text/csv";
	public static final String GOOGLE_PPT_FILE = "application/vnd.google-apps.presentation";
	public static final String GOOGLE_DOC_FILE = "application/vnd.google-apps.document";
	public static final String GOOGLE_SPREADSHEET_FILE = "application/vnd.google-apps.spreadsheet";

	public static final List<String> ALL_SUPPORTED_DRIVE() {

		String ico_IMAGE = "image/x-icon";
		String svg_IMAGE = "image/svg+xml";
		String JPEG_IMAGE = "image/jpeg";
		String gif_IMAGE = "image/gif";
		String PNG_IMAGE = "image/png";
		String ZIP_FILE = "application/zip";
		String gz_ZIP = "application/x-gzip";
		String mdb_DATABASE = "application/x-msaccess";
		String mov_VIDEO = "video/quicktime";
		String movie_VIDEO = "video/x-sgi-movie";
		String wav_AUDIO = "audio/x-wav";
		String mp3_AUDIO = "audio/mpeg";
		String mpeg_AUDIO = "video/mpeg";
		String swf_FILE = "application/x-shockwave-flash";
		String TSV_FILE = "text/tab-separated-values";
		String HTML_FILE = "text/html";
		String RTF_FILE = "application/rtf";
		String SXW_FILE = "application/vnd.sun.xml.writer";
		List<String> allSupported = new ArrayList<String>();
		allSupported.add(MS_DOCUMENT);
		allSupported.add(MS_SPREADSHEET);
		allSupported.add(OPEN_DOCUMENT);
		allSupported.add(OPEN_SPREADSHEET);
		allSupported.add(ico_IMAGE);
		allSupported.add(svg_IMAGE);
		allSupported.add(JPEG_IMAGE);
		allSupported.add(gif_IMAGE);
		allSupported.add(gz_ZIP);
		allSupported.add(mdb_DATABASE);
		allSupported.add(mov_VIDEO);
		allSupported.add(movie_VIDEO);
		allSupported.add(wav_AUDIO);
		allSupported.add(mp3_AUDIO);
		allSupported.add(mpeg_AUDIO);
		allSupported.add(swf_FILE);
		allSupported.add(CSV_FILE);
		allSupported.add(TSV_FILE);
		allSupported.add(HTML_FILE);
		allSupported.add(RTF_FILE);
		allSupported.add(SXW_FILE);
		allSupported.add(PDF_DOCUMENT);
		allSupported.add(PNG_IMAGE);
		allSupported.add(ZIP_FILE);
		allSupported.add(PLAIN_TEXT);
		allSupported.add(MS_DOCUMENTX);
		allSupported.add(GOOGLE_DOC_FILE);
		allSupported.add(GOOGLE_PPT_FILE);
		allSupported.add(GOOGLE_SPREADSHEET_FILE);
		return allSupported;
	}
	public static final String getMimeTypeOfFile(String fileName) {
		
		if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
			return "application/msword";
		} else if (fileName.endsWith(".txt")) {			
			return "text/plain";
		} else if (fileName.endsWith(".odt")) {
			return "application/vnd.oasis.opendocument.text";
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (fileName.endsWith(".gif")) {
			return "image/gif";
		} else if (fileName.endsWith(".png")) {
			return "image/png";
		} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
			return "application/vnd.ms-excel";
		} else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
			return "application/vnd.ms-powerpoint";
		} else if (fileName.endsWith(".pdf")) {
			return "application/pdf";
		} else if (fileName.endsWith(".csv")) {
			return "text/csv";
		} else if (fileName.endsWith(".zip")) {
			return "application/zip";
		} else if (fileName.endsWith(".gz")) {
			return "application/x-gzip";
		} else if (fileName.endsWith(".mov")) {
			return "video/quicktime";
		} else if (fileName.endsWith(".mp3")) {
			return "audio/mpeg";
		} else if (fileName.endsWith(".swf")) {
			return "application/x-shockwave-flash";
		} else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
			return "text/html";
		}
		return "";
	}
}
