package com.axelor.googleappsconn.spreadsheet;

import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.drive.GoogleFile;

/**
 * the class to perform operations relevant to spreadsheets like create and read
 * */
public class SpreadsheetOperations {

	GoogleDrive driveService;

	public SpreadsheetOperations(GoogleDrive passedService) {
		driveService = passedService;
	}
	/**
	 * This method is called by other methods and should not be used by user
	 * directly Creates a new SpreadSheet on local machine and upload
	 * @param fileName String file name to be created on google drive
	 * @param directoryId String parent Directory ID
	 * @param fileToUpload java.io.File
	 * @return GoogleFile uploaded file and configured attributes.
	 * @throws Exception
	 */
	public GoogleFile createSpreadSheet(String fileName, String directoryId,
			java.io.File file) throws Exception {
		boolean convertToGoogleFormat = true;
		long fileSize = 0L;
		String mimeType = FileTypes.MS_SPREADSHEET;
		GoogleFile uploadedFile = driveService.createFile(fileName, mimeType,
				file, directoryId, convertToGoogleFormat);
		if (convertToGoogleFormat) {
			uploadedFile.setFileType(FileTypes.GOOGLE_SPREADSHEET_FILE);
			uploadedFile.setFileSize(fileSize);
		}
		return uploadedFile;
	}
}
