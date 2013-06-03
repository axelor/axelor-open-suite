package com.axelor.googleapps.uploadFile

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.googleapps.userutils.Utils
import com.axelor.googleappsconn.drive.GoogleDrive
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class UploadFileService {

	@Inject Utils userUtils
	@Inject GoogleDrive googleDrive
	@Transactional
	public GoogleFile uploadFile(User driveUser, GoogleFile googleFile) {
		
		userUtils.startApps(driveUser)
		File file = new File(GoogleDrive.USER_HOME_DOCUMENTS,googleFile.getFileName())
		com.axelor.googleappsconn.drive.GoogleFile uploadedFileInfo = googleDrive.uploadFile(file, googleFile.getGoogleDirectory().getDirectoryId())
		googleFile.setFileId(uploadedFileInfo.getFileId())

		GoogleFile uploadedFile = new GoogleFile()
		uploadedFile.setFileId(uploadedFileInfo.getFileId())
		uploadedFile.setFileName(uploadedFileInfo.getFileName())
		uploadedFile.setFileSize(uploadedFileInfo.getFileSize().toString())
		uploadedFile.setFileType(uploadedFileInfo.getFileType())
		uploadedFile.setGoogleDirectory(googleFile.getGoogleDirectory())
		uploadedFile.setDriveUser(driveUser)
		uploadedFile.setLastModified(uploadedFile.getLastModified());
		uploadedFile.setFileContent("uploaded File")
		uploadedFile.persist()

		return googleFile
	}
}
