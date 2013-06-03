package com.axelor.googleapps.document

import java.lang.annotation.Retention;
import java.util.List;

import javax.persistence.PersistenceException
import org.apache.poi.OldFileFormatException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.hibernate.ejb.criteria.expression.SizeOfCollectionExpression;

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.GoogleDirectory;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.FilePermission
import com.axelor.apps.base.db.TemplateFile;
import com.axelor.apps.base.db.UserProfile
import com.axelor.googleapps.syncDrive.SynchronizeWithGoogleDriveService;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.googleappsconn.document.DocumentOperations
import com.axelor.googleappsconn.drive.Directory
import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.sharing.SharingOperation;
import com.axelor.googleappsconn.sharing.SharingPermission;
import com.axelor.rpc.Response;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers.Returns;
import com.google.inject.persist.Transactional;


class DocumentService {
	
	@Inject GoogleDrive googleDrive
	@Inject SynchronizeWithGoogleDriveService syncService
	@Inject Utils userUtils
	/**
	 * creates a document without template
	 * @param driveuser User this is the entity of the user currently logged in to axelor.
	 * @param documentData
	 * @return
	 */
	@Transactional
	public GoogleFile createDocument(User driveuser, GoogleFile documentData) {
			
		userUtils.startApps(driveuser)
		com.axelor.googleappsconn.drive.GoogleFile googleFileInfo = new com.axelor.googleappsconn.drive.GoogleFile()
		GoogleDirectory googleDirectory = GoogleDirectory.all().filter("directoryName=?", "Root").fetchOne()
		def documentContent = documentData.getFileContent()
		def docFileName = documentData.getFileName()
		GoogleFile googleFileNewData = new GoogleFile()
		
		if(documentData.getGoogleDirectory() == null)
			documentData.setGoogleDirectory(syncService.checkAndFillRootDirectory(driveuser))		
		googleFileInfo = new  DocumentOperations(googleDrive).createDocument(docFileName, documentContent,
			 				documentData.getFileType(),documentData.getGoogleDirectory().getDirectoryId())
		SharingPermission permission = null
		
		if(documentData.getFilePermissions() != null) {
			List<SharingPermission> sharingPerList = new ArrayList<SharingPermission>();

			for(FilePermission filePermission: documentData.getFilePermissions()) {
				permission = new SharingPermission();
			
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
			sharingPerList = new SharingOperation(googleDrive).shareFile(sharingPerList,googleFileInfo.getFileId())
		}
		
		googleFileNewData.setFileId(googleFileInfo.getFileId())
		googleFileNewData.setFileName(googleFileInfo.getFileName())
		googleFileNewData.setFileType(googleFileInfo.getFileType())			
		googleFileNewData.setFileSize(googleFileInfo.getFileSize()?.longValue().toString()+" Bytes")
		googleFileNewData.setFileContent("Not Required To store")
		googleFileNewData.setGoogleDirectory(documentData.getGoogleDirectory())
		googleFileNewData.setDriveUser(driveuser)	
		googleFileNewData.setFilePermissions(documentData.getFilePermissions())
		googleFileNewData.setLastModified(googleFileInfo.getLastModified());
	
		println "Hello --------------------------------------------------------------------------------------------------"+googleFileNewData
		
		return googleFileNewData		
	
	}
	/**
	 * creates a document with template
	 * @param driveuser User this is the entity of the user currently logged in to axelor.
	 * @param documentData GoogleFile entity
	 * @return message String
	 */
	@Transactional
	public <T> GoogleFile createDocumentWithTemplate(User driveuser, T dataObject) {
					
		userUtils.startApps(driveuser)
		com.axelor.googleappsconn.drive.GoogleFile googleFileInfo = null
		Class klass = dataObject.getClass()
		List<TemplateFile> templates = TemplateFile.all().filter("dataModel.fullName=?",klass.getName()).fetch()
		TemplateFile template
		
		if (templates != null) {
			templates = templates.findAll {
				if (it.getGoogleFile().getDriveUser().equals(driveuser)) return it;
				if (it.getShare()) return it;
			}
			if (templates != null) {
				template = templates[0]
			}
		}
		// no template found for the selected domain/entity
		if(template == null || template.getGoogleFile() == null) return null;
		GoogleFile templateGoogleFile = template.getGoogleFile()
		java.io.File localTemplateFile;
		// if the the owner of the template found is different than current
		if(!templateGoogleFile.getDriveUser().equals(driveuser)) {
			// if the template file is not shared
		
			if(!template.getShare()) {
				return null;
			} else {
				// template file is shared and get - download it
				GoogleDrive tempUserGoogleDrive = userUtils.getUserGoogleDrive(templateGoogleFile.getDriveUser())
				localTemplateFile = tempUserGoogleDrive.downloadFile(templateGoogleFile.getFileId())
				tempUserGoogleDrive.finalize()
			}
		} else {
			
			try {
				localTemplateFile = googleDrive.downloadFile(templateGoogleFile.getFileId())
			} catch (Exception ex) {
				if(ex.getMessage().contains("File not found"))
					throw new Exception("The Template File does not exist on Google Drive. You can create a new document and make it template file.")
			}
		}
		
		Map<String,Object> dataMap = userUtils.getDataMapFromObject(dataObject)
		String fileName = dataMap.get("fileNameforThisDataObject")
		GoogleDirectory parentDirectory = syncService.checkAndFillRootDirectory(driveuser)
		googleFileInfo = new DocumentOperations(googleDrive)
					.createDocumentWithTemplate(fileName,parentDirectory.getDirectoryId(),dataMap,localTemplateFile)
		GoogleFile googleFileNewData = new GoogleFile()

		googleFileNewData.setFileId(googleFileInfo.getFileId())
		googleFileNewData.setFileName(googleFileInfo.getFileName())
		googleFileNewData.setFileType(googleFileInfo.getFileType())
		googleFileNewData.setFileSize(googleFileInfo.getFileSize()?.longValue().toString()+" Bytes")
		googleFileNewData.setFileContent("Not Required To store")
		googleFileNewData.setGoogleDirectory(parentDirectory)
		googleFileNewData.setDriveUser(driveuser)
		googleFileNewData.setLastModified(googleFileInfo.getLastModified());
		googleFileNewData.persist()

		localTemplateFile.delete()
		return googleFileNewData;
	}
	@Transactional
	public GoogleFile createBlankDocument(User currentUser,GoogleFile googleFile){
	
		userUtils.startApps(currentUser)
		com.axelor.googleappsconn.drive.GoogleFile googleFileInfo = new com.axelor.googleappsconn.drive.GoogleFile()
		def documentContent = ""
		def docFileName = googleFile.getFileName()
		GoogleFile googleFileNewData = new GoogleFile()
		
		if(googleFile.getGoogleDirectory() == null)
			googleFile.setGoogleDirectory(syncService.checkAndFillRootDirectory(currentUser))
		
		googleFileInfo = new  DocumentOperations(googleDrive).createDocument(docFileName, 
								documentContent, googleFile.getFileType(), googleFile.getGoogleDirectory().getDirectoryId())
		googleFile.setFileId(googleFileInfo.getFileId())
		googleFile.setDriveUser(currentUser)
		googleFile.setFileSize(googleFileInfo.getFileSize()?.longValue().toString() + " Bytes")
		googleFile.setFileType(googleFileInfo.getFileType())
		googleFile.setLastModified(googleFileInfo.getLastModified())		
		SharingPermission permission = null
		
		if(googleFile.getFilePermissions()!=null) {
			List<SharingPermission> sharingPerList=new ArrayList<SharingPermission>();
		
			for(FilePermission filePermission: googleFile.getFilePermissions()) {
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
			sharingPerList=new SharingOperation(googleDrive).shareFile(sharingPerList,googleFileInfo.getFileId())
		}
		googleFile.getFilePermissions().each {
			it.setGoogleFile(null)
		}
		googleFile.persist()
		com.axelor.apps.base.db.GoogleFile newGoogleFile = googleFile
		newGoogleFile.getFilePermissions().each {
			it.setGoogleFile(googleFile)
		}
		newGoogleFile.merge()
		return googleFile
	}
	/**
	 * creates a new google file in current directory
	 * @param googleFiles
	 * @param currentUser
	 * @return newFile GoogleFile
	 */
	@Transactional
	public GoogleFile saveDocumentInDirectory(List<GoogleFile> googleFiles, User currentUser) {
		
		GoogleFile newFile = null
		
		for(GoogleFile singleFile : googleFiles) {
			if(singleFile.getId() == null) {
				newFile = singleFile
				break
			}
		}
		
		if(newFile != null) {
			newFile = createDocument(currentUser,newFile)
			newFile.getFilePermissions().each {
				it.setGoogleFile(null)
			}
			newFile.persist()
			com.axelor.apps.base.db.GoogleFile newGoogleFile = newFile
			newGoogleFile.getFilePermissions().each {
				it.setGoogleFile(newFile)
			}
			newGoogleFile.merge()
		} else { 
			newFile = new GoogleFile()
		}
		return newFile
	}
	
	/**
	 * gives the URL to read the document in googleDrive in browser
	 * @param fileId String fileId of document to read
	 * @return the url for the file to read in google drive
	 */
	public String readDocumentInDrive(String fileId, String fileType) {

		if(fileType.equals(FileTypes.GOOGLE_DOC_FILE))
			return "https://docs.google.com/document/d/"+fileId +"/edit"
		else if(fileType.equals(FileTypes.GOOGLE_PPT_FILE))
			return "https://docs.google.com/presentation/d/"+fileId +"/edit"
		else if(fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) 
			return "https://docs.google.com/spreadsheet/ccc?key="+fileId 
		else
			return "https://docs.google.com/file/d/"+ fileId +"/edit"

	}

	/**
	 * updates the content of the document previously created by the current user.
	 * @param driveuser User this is the entity of the user currently logged in to axelor.
	 * @param documentData GoogleFile the google file to be updated (entity)
	 * @return documentData GoogleFile updated document as object of GoogleFile
	 */
	@Transactional
	public GoogleFile updateDocument(User driveuser, GoogleFile documentData)	{

		userUtils.startApps(driveuser)
		documentData.setFileContent("Not Required To store")
		GoogleFile oldGoogleFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser, documentData.getFileId()).fetchOne()
		
		if(documentData.getGoogleDirectory() != null
			&& !oldGoogleFile.getGoogleDirectory().getDirectoryId().equals(documentData.getGoogleDirectory().getDirectoryId())) {
			googleDrive.moveFile(documentData.getFileId(),
								oldGoogleFile.getGoogleDirectory().getDirectoryId(),
								documentData.getGoogleDirectory().getDirectoryId())
		}

		// Rename File
		if(!oldGoogleFile.getFileName().equals(documentData.getFileName())) {
			new DocumentOperations(googleDrive).renameDocument(documentData.getFileId(), documentData.getFileName())
		}
		
		//Sharing Google File
		SharingPermission permission = null
		if(documentData.getFilePermissions() != null) {

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
			GoogleFile oldGoogleFiletoUnshare = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser,documentData.getFileId()).fetchOne()
			List<String> oldFilePermisisonIdList = new ArrayList<String>();
			
			int j = 0;
			while(j < oldGoogleFiletoUnshare.getFilePermissions().size()) {
				oldFilePermisisonIdList.add(oldGoogleFiletoUnshare.getFilePermissions().get(j).getSharingUser()?.getPermissionId())
				j++;
			}
			List<String> newFilePermisisonIdList = new ArrayList<String>();
			
			int k = 0;
			while(k < documentData.getFilePermissions().size()) {
				newFilePermisisonIdList.add(documentData.getFilePermissions().get(k).getSharingUser()?.getPermissionId())
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
	@Transactional
	public String deleteDocumentsFromGrid(User driveUser, List<String> listIds) {

		int sizeToRemove = listIds == null ? 0 : listIds.size()
		int sizeRemoved = 0
		if(sizeToRemove == 0)
			return "Please, First select the files you want to delete."
		
		userUtils.startApps(driveUser)
		GoogleFile googleFile = null
		
		listIds.each {
			googleFile = GoogleFile.all().filter("id=?",it).fetchOne()
			String fileId = googleFile.getFileId()
			try {
				googleFile.remove() 
			} catch (PersistenceException e) {
				throw new Exception("You can not delete <FONT Color='#01DF3A'> " + googleFile.getFileName() + " </FONT> file")
			}
			googleDrive.removeFile(googleFile.getFileId())
			sizeRemoved++
		}
		
		if(sizeToRemove == 1)
			return "The file <FONT Color='#01DF3A'>" +googleFile.getFileName() + "</FONT> deleted successfully."
		else
			return "<FONT Color='#01DF3A'>"+sizeRemoved + " </FONT> files out of <FONT Color='#01DF3A'> " + sizeToRemove + " </FONT> are deleted successfully."
	}
	
	public String getFileDownloadURL(User currentUser, String fileId, String fileType) {
		
		userUtils.startApps(currentUser)
		
		if(fileType.equals(FileTypes.GOOGLE_DOC_FILE) || fileType.equals(FileTypes.GOOGLE_PPT_FILE)){
			return googleDrive.getGoogleFileDownloadURL(fileId)
		} else {
			return googleDrive.getFileDownloadURL(fileId)
		}
	}
	
	public String getFileExportInPdfURL(User currentUser, String fileId) {
		
		userUtils.startApps(currentUser)
		return googleDrive.getExportInPdfFileURL(fileId)
	}
	@Transactional
	public String trashListofGoogleFiles(User currentUser, List<String> filesToRemove) {

		int sizeToRemove = filesToRemove.size()
		int sizeRemoved = 0;
		
		if(sizeToRemove == 0)
			return "Please first select the files to be moved to trash";
			
		List<GoogleFile> filesTrashed = new ArrayList<GoogleFile>()
		userUtils.startApps(currentUser)
		GoogleFile googleFile = null

		filesToRemove.each {
			googleFile = GoogleFile.all().filter("id=?",it).fetchOne()
		
			if(!googleFile.getSharedWithMe()) {
				try {
					if(googleFile.getFileId() != null)
						googleDrive.trashFile(googleFile.getFileId())
				} catch(IOException ioEx) {
					throw new Exception(ioEx.getMessage())
				}
				googleFile.setTrashed(true)
				googleFile.merge()
				filesTrashed.add(googleFile)
				sizeRemoved++
			}
		}

		if(sizeRemoved == 1)
			return "The file <FONT Color='#01DF3A'>" + googleFile.getFileName() + " </FONT> moved to trash successfully"
		else
			return "<FONT Color='#01DF3A'>"+sizeRemoved + " </FONT> files moved to trash out of <FONT Color='#01DF3A'>" + sizeToRemove +"</FONT>"
	}
	
	@Transactional
	public String restoreListofGoogleFiles(User currentUser, List<String> filesToRestore) {
		
		boolean parentTrashProblem = false
		int sizeToRestore = filesToRestore.size()
		int sizeRestored = 0;
	
		if(sizeToRestore == 0)
			return "Please first select the files to be moved to trash";
		userUtils.startApps(currentUser)
		GoogleFile googleFile = null

		for(String id : filesToRestore) {
			googleFile = GoogleFile.all().filter("id=?", id).fetchOne()
			
			// skip the file if the file's parent directory is trashed
			if(googleFile.getGoogleDirectory().getTrashed()) {
				parentTrashProblem = true
				continue;
			}
			try {
				googleDrive.untrashFile(googleFile.getFileId())
			} catch (IOException ioEx) {
				return "the file "+ googleFile.getFileName() +
				" was not available in trash in google drive, it might have been deleted from there."
			}
			googleFile.setTrashed(false)
			googleFile.merge()
			sizeRestored++
		}
		String parentProblem = ""
		if(parentTrashProblem) {
			parentProblem = "...The parent directory of the file is trashed, please restore the trashed directory."
		}
		
		if(sizeRestored == 1 && googleFile != null)
			return "The file <FONT Color='#01DF3A'>" + googleFile.getFileName() + "</FONT> restored successfully " + parentProblem
		else
			return "<FONT Color='#01DF3A'>"+sizeRestored + "</FONT> files restored out of <FONT Color='#01DF3A'>" + sizeToRestore + " </FONT>\t" + parentProblem
			
	}
	@Transactional
	public String deleteFileFromTrash(User currentUser, List<String> filesToRemove) {
		
		int sizeToRemove = filesToRemove.size()
		int sizeRemoved = 0;
		
		if(sizeToRemove == 0)
			return "Please first select the files to be moved to trash";
			
		List<GoogleFile> filesTrashed = new ArrayList<GoogleFile>()
		userUtils.startApps(currentUser)
		GoogleFile googleFile = null

		filesToRemove.each {
			googleFile = GoogleFile.all().filter("id=?",it).fetchOne()
			
			if(!googleFile.getSharedWithMe()) {
				try {
					if(googleFile.getFileId() != null)
						googleDrive.untrashFile(googleFile.getFileId())
					googleDrive.removeFile(googleFile.getFileId())
				} catch (IOException ioEx) {
					return "The file(s) are not available on drive."
				}
				googleFile.remove()
				sizeRemoved++
			}
		}
		
		if(sizeRemoved == 1)
			return "The file <FONT Color='#01DF3A'> " + googleFile.getFileName() + " </FONT> moved to trash successfully"
		else
			return "<FONT Color='#01DF3A'>"+sizeRemoved + "</FONT> files deleted forever from to trash out of <FONT Color='#01DF3A'> " + sizeToRemove +"</FONT>"

	}
	@Transactional
	public GoogleFile checkSharingUserList(User currentUser, GoogleFile googleFile) {
		
		userUtils.startApps(currentUser)
		List<String> filePermissionList = new ArrayList<String>();
		List<SharingPermission> sharingPerList = new SharingOperation(googleDrive).getPermissionListOfFile(googleFile.getFileId())
		
		for(SharingPermission sharingPermission : sharingPerList) {
			filePermissionList.add(sharingPermission.getPermissionId());
		}
						
		List<String> dbFilePermissionList = new ArrayList<String>();
		for(FilePermission filepermission : googleFile.getFilePermissions()) {
			if(filepermission.getSharingUser() != null)
			dbFilePermissionList.add(filepermission.getSharingUser()?.getPermissionId())
		}
		
		for(String perId: dbFilePermissionList) {	// REMOVE file permissions which not found on google drive
			if(!filePermissionList.contains(perId)) {
				FilePermission deletePermission = FilePermission.all().filter("googleFile=? and sharingUser.permissionId=? ",googleFile,perId).fetchOne()
				// here used JPA instead of @Transactional bcz this method is dealing with adding and removing the permissions in which
				// we are removing from db and adding and sending in response.
				JPA.runInTransaction {
					deletePermission.remove() 
				}
			}
		}
		
		GoogleFile changedPerList = GoogleFile.all().filter("driveUser=? and fileId=?", currentUser,googleFile.getFileId()).fetchOne()
		googleFile.setFilePermissions(changedPerList.getFilePermissions())
		
		for(String perId : filePermissionList){		// ADD file permissions found from google drive to DB
			
			if(!dbFilePermissionList.contains(perId)){
				UserProfile sharingUser = UserProfile.all().filter("permissionId=?", perId).fetchOne()
				
				if(sharingUser == null) continue
				FilePermission permission = FilePermission.all().filter("googleFile=? and sharingUser=?", googleFile,sharingUser).fetchOne()
				
				if(permission == null){
					permission = new FilePermission()
					permission.setSharingUser(sharingUser)
					permission.setGoogleFile(googleFile)
					SharingPermission sharingPermission = sharingPerList.find { it.permissionId == perId	}
					
					if(sharingPermission.getRole().equals("writer"))
						permission.setEditable(true)
					else
						permission.setEditable(false)
				}
				googleFile.getFilePermissions()?.add(permission)
			}
		}
		return googleFile
	}
}

