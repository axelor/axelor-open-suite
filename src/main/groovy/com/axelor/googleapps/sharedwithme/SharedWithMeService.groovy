package com.axelor.googleapps.sharedwithme

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.FilePermission;
import com.axelor.googleapps.userutils.Utils
import com.axelor.apps.base.db.AppsCredentials
import com.axelor.googleapps.document.DocumentService
import com.axelor.googleappsconn.document.DocumentOperations
import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive
import com.axelor.googleappsconn.sharing.SharingOperation;
import com.axelor.googleappsconn.sharing.SharingPermission;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
class SharedWithMeService {

	@Inject Utils userUtils
	@Inject GoogleDrive googleDrive
	public String getfiledownloadURL(User currentUser, String fileId, String fileType) {
		
		userUtils.startApps(currentUser)
	
		if(fileType.equals(FileTypes.GOOGLE_DOC_FILE) || fileType.equals(FileTypes.GOOGLE_PPT_FILE) || fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE))
			return googleDrive.getGoogleFileDownloadURL(fileId)
		else
			return googleDrive.getFileDownloadURL(fileId)
	}
	@Transactional
	public GoogleFile updateSharedwithmeFile(User driveUser, GoogleFile documentData) {
		
		userUtils.startApps(driveUser)
		
		if(documentData.getWritable() == true && !documentData.getFileType().equals(FileTypes.GOOGLE_DOC_FILE)) {
			documentData.setFileContent("Not Required To store")
		}
		
		// Rename File
		GoogleFile oldGoogleFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveUser, documentData.getFileId()).fetchOne()
		if(!oldGoogleFile.getFileName().equals(documentData.getFileName())) {
			new DocumentOperations(googleDrive).renameDocument(documentData.getFileId(), documentData.getFileName())
		}
		
		//Sharing Google File On
		SharingPermission permission = null
		if(documentData.getFilePermissions() != null && documentData.getWritable() == true) {
			List<SharingPermission> sharingPerList = new ArrayList<SharingPermission>();
		
			for(FilePermission filePermission: documentData.getFilePermissions()) {
			
				// check filepermission already shared
				if(filePermission.getId() == null) {
					permission=new SharingPermission();
				
					if(filePermission.getEditable().booleanValue())
						permission.setRole("writer")
					else
						permission.setRole("reader")
					
				if(filePermission.getSharingUser() != null){
					permission.setEmailId(filePermission.getSharingUser().getEmailId())
					permission.setName(filePermission.getSharingUser().getName())
				} else {
					permission.setEmailId(filePermission.getShareToEmail())
					permission.setName(filePermission.getShareToEmail())
				}
					permission.setType("user")
					permission.setNotifyEmail(filePermission.getNotifyEmail())
					sharingPerList.add(permission)
				}
			}
			sharingPerList = new SharingOperation(googleDrive).shareFile(sharingPerList,documentData.getFileId())
			
			//unshare file
			GoogleFile oldGoogleFiletoUnshare = GoogleFile.all().filter("driveUser=? and fileId=?", driveUser,documentData.getFileId()).fetchOne()
			List<String> oldFilePermisisonIdList = new ArrayList<String>();
			
			int j = 0;
			while(j < oldGoogleFiletoUnshare.getFilePermissions().size()) {
				oldFilePermisisonIdList.add(oldGoogleFiletoUnshare.getFilePermissions().get(j).getSharingUser().getPermissionId())
				j++;
			}
			List<String> newFilePermisisonIdList = new ArrayList<String>();
			
			int k = 0;
			while(k < documentData.getFilePermissions().size()) {
				newFilePermisisonIdList.add(documentData.getFilePermissions().get(k).getSharingUser().getPermissionId())
				k++;
			}
			List<String> listOfUnshareFile = new ArrayList<String>()
			
			for(String perId:oldFilePermisisonIdList) {
				if(!newFilePermisisonIdList.contains(perId)) {
					listOfUnshareFile.add(perId)
				}
			}
			new SharingOperation(googleDrive).unShareFile(listOfUnshareFile, documentData.getFileId())
		}
		return documentData
	}
}

