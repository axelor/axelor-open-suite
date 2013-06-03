package com.axelor.googleapps.document

import java.awt.Desktop;

import javax.security.auth.login.AppConfigurationEntry;

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.googleapps.syncDrive.SynchronizeWithGoogleDriveService
import com.axelor.apps.base.db.GoogleDirectory
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.TemplateFile;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.apps.base.db.GoogleAppsConfig;
import com.axelor.googleappsconn.document.DocumentOperations;
import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;

class DocumentController {
	
	@Inject DocumentService documentSeriveObj
	@Inject SynchronizeWithGoogleDriveService syncService
	@Inject Utils userUtils
	/**
	 * saves the document created by user with or without template
	 * @param request
	 * @param response
	 */
	void saveDocumentFile(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
		User currentUser = request.context.get("__user__")
		syncService.checkAndFillRootDirectory(currentUser)
		String message
		GoogleFile documentData = request.context as GoogleFile
		
		try {
			if(documentData.getFileId() == null) {
				documentData = documentSeriveObj.createDocument(currentUser, documentData)
				String dirName = documentData.getGoogleDirectory().getDirectoryName().toString()
				message = "Successfully <FONT Color='#01DF3A'> "+ documentData.getFileName()  + " </FONT> file stored in <FONT Color='#01DF3A'>" + dirName + " </FONT>Directory"
				response.values = documentData
				
			} else {
				message = "Successfully <FONT Color='#01DF3A'> " + documentData.getFileName().toString() + " </FONT>updated"
				response.values = documentSeriveObj.updateDocument(currentUser, documentData)
			}
			
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.flash=message
	}
	
	void createDocumentInDrive(ActionRequest request,ActionResponse response){
	
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile=request.context as GoogleFile
		if(googleFile.getFileName() == null || googleFile.getFileType() == null)
			throw new Exception("You should provide required details, then click Create in Drive.")
		googleFile=documentSeriveObj.createBlankDocument(currentUser,googleFile)
		String driveUrl=documentSeriveObj.readDocumentInDrive(googleFile.getFileId(),googleFile.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFile.getFileName(), resource : url, viewType : "html"]
		response.values = googleFile
	}
	
	void saveDocumentInDirectory(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
			
		User currentUser = request.context.get("__user__")
		GoogleDirectory directory = request.context as GoogleDirectory
		List<GoogleFile> filesList = directory.getGoogleFiles()
		
		// if all files have not null value in id then there is no file added
		if(filesList.find {	it.getId() == null;	}==null)  {
			return
		}		
		GoogleFile newFile
						
		try {
			newFile = documentSeriveObj.saveDocumentInDirectory(filesList,currentUser)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		
		}
		if(newFile == null) {
			throw new Exception(" <FONT Color='#FF0000'> There was some problem saving file in this directory, try other directory. Don't Save Now. </FONT>")
			directory.setGoogleFiles(filesList)
			response.values = directory
			return
		}
		
		if(newFile.getId() == null) {
			response.flash = "Sharing Permission Updated.."
			response.values = directory
			return
		}		
		filesList.remove(filesList.size()-1)
		newFile.setGoogleDirectory(directory)
		filesList.add(newFile)
		// set the list with edited entry
		directory.setGoogleFiles(filesList)
		response.flash = " The File <FONT Color='#01DF3A'> " + newFile.getFileName() + " </FONT> successfully added in <FONT Color='#01DF3A'> " + directory.getDirectoryName() + " Directory </FONT>"
		response.values = directory
	}

	void onNewCheckRootExistance(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
			
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = new GoogleFile()
		GoogleDirectory parentDirectory = null
	
		try {
			parentDirectory = syncService.checkAndFillRootDirectory(currentUser)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
	}

	void deleteFilesFromGrid(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
		
		User currentUser = request.context.get("__user__")
		def listIds = request.context.get("_ids")
		String message
		
		try {
			message = " <FONT Color='#01DF3A'> "+documentSeriveObj.deleteDocumentsFromGrid(currentUser, listIds)+"</FONT>"
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("These Files Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.flash = message
	}

	void downloadFileFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		try {
				User currentUser = request.context.get("__user__")
				def listIds = request.context.get("_ids")
				GoogleFile googleFileFromList = GoogleFile.all().filter("id=?",listIds[0]).fetchOne()
				String downloadURL = documentSeriveObj.getFileDownloadURL(currentUser, googleFileFromList.getFileId(),googleFileFromList.getFileType())
				response.flash = "  <FONT Color='#0000FF'> <a href=" + downloadURL +" target='_new'>Click Here to Download File </a></FONT>"
			
			} catch (Exception ex) {
			
				if(ex.getMessage().contains("File not found"))
					throw new Exception("These Files Not Found on your Google Drive, You might have been deleted from there.")
				else
					throw new Exception(ex.getMessage())
				
			}
	}

	void downloadFile(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
		
		try {
			User currentUser = request.context.get("__user__")
			GoogleFile googleFile = request.context as GoogleFile
			if(googleFile.getFileId() == null)
				throw new Exception("You can not download file before saving it.")
			String downloadURL = documentSeriveObj.getFileDownloadURL(currentUser, googleFile.getFileId(),googleFile.getFileType())
			response.flash = "   <FONT Color='#0000FF'> <a href=" + downloadURL +" target='_new' >Click Here To DownloadFile </a></FONT>"
		} catch (Exception ex) {
		
			if(ex.getMessage().contains("File not found"))
				throw new Exception("These Files Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
	}

	void readDocumentInDrive(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		GoogleFile googleFile = request.context as GoogleFile		
		String fileId = googleFile.getFileId()
		if(fileId == null)
			throw new Exception("You can not read document before saving it.")
		String driveUrl = documentSeriveObj.readDocumentInDrive(fileId,googleFile.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFile.getFileName(), resource : url, viewType : "html"]	
	}

	void readDocumentInDriveFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		def listIds = request.context.get("_ids")
		GoogleFile googleFileFromList = GoogleFile.all().filter("id=?",listIds[0]).fetchOne()
		String driveUrl = documentSeriveObj.readDocumentInDrive(googleFileFromList.getFileId(),googleFileFromList.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFileFromList.getFileName(), resource : url, viewType : "html"]
	}

	
	void exportFileInPdf(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
			
		try {
				User currentUser = request.context.get("__user__")
				GoogleFile googleFile = request.context as GoogleFile
				if(googleFile.getFileId() == null)
					throw new Exception("You can not export before saving file.")
				String downloadURL = documentSeriveObj.getFileExportInPdfURL(currentUser, googleFile.getFileId())
				if(downloadURL != null)
					response.flash = "   <a href='" + downloadURL +"' target='_new'> Click Here To Export File In PDF </a>"
				else
					response.flash = "You can Export the Google Document/Spreadsheet Type file only as PDF"
		
			} catch (Exception ex) {
				if(ex.getMessage().contains("File not found"))
					throw new Exception("These Files Not Found on your Google Drive, You might have been deleted from there.")
				else
					throw new Exception(ex.getMessage())
       		}
	}

	void trashDocumentsFromGrid(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
		
		String message;
		User currentUser = request.context.get("__user__")
		def listIds = request.context.get("_ids")

		try {
			message = documentSeriveObj.trashListofGoogleFiles(currentUser, listIds)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.flash = message
	}

	void restoreDocumentsFromGrid(ActionRequest request, ActionResponse response) {

		if(!userUtils.validAppsConfig(request, response))
			return
		
		String message
		User currentUser = request.context.get("__user__")
		def listIds = request.context.get("_ids")
		try {
			message = documentSeriveObj.restoreListofGoogleFiles(currentUser, listIds)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.flash = message
	}
	void deleteTrashedFile(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		String message;
		User currentUser = request.context.get("__user__")
		def listIds = request.context.get("_ids")
		try {
			message = documentSeriveObj.deleteFileFromTrash(currentUser, listIds)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.flash = message
	}

	void checkSharingList(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		try {
			googleFile = documentSeriveObj.checkSharingUserList(currentUser, googleFile)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.values = googleFile
	}

	void getCurrentUser(ActionRequest request, ActionResponse response) {

		User currentUser = request.context.get("__user__")
		response.values = ["dummy3":currentUser]
	}
}

