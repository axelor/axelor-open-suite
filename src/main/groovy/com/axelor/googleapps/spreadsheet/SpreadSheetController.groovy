package com.axelor.googleapps.spreadsheet

import com.axelor.auth.db.User
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.TemplateSpreadSheet;
import com.axelor.googleapps.document.DocumentService;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.googleapps.syncDrive.SynchronizeWithGoogleDriveService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Response;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.inject.Inject;
import com.axelor.meta.db.MetaModel;
class SpreadSheetController {
	
	@Inject SpreadSheetService spreadSheetService
	@Inject DocumentService documentService
	@Inject SynchronizeWithGoogleDriveService syncService
	@Inject Utils userUtils
	void saveSpreadSheet(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		TemplateSpreadSheet templateSpreadsheet = request.context.get("templateSpreadSheet") as TemplateSpreadSheet
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		GoogleFile updatedGoogleFile;
		
		if(googleFile.getId() != null) {
			updatedGoogleFile = spreadSheetService.updateSpreadSheet(currentUser,googleFile)
		} else {
			
			try {
			
				if(templateSpreadsheet == null) {
					throw new Exception("You should select a template spreadsheet to take data from.")
				}
				updatedGoogleFile = spreadSheetService.createSpreadSheetWithTemplate(currentUser,googleFile,templateSpreadsheet)
				response.values = updatedGoogleFile
				response.flash = " File Saved Successfully "
			}
			catch (javax.persistence.NoResultException e) {
				throw new Exception("There is not any matched data for specified Criteria")
			
			}catch (Exception ex) {
			
				if(ex.getMessage().contains("File not found"))
					throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
				else
					throw new Exception(ex.getMessage())
			}
		}
	}
	
	void createSpreadSheetInDrive(ActionRequest request,ActionResponse response){
	
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile=request.context as GoogleFile
		googleFile=spreadSheetService.createBlankSpreadSheet(currentUser,googleFile)
		String driveUrl=spreadSheetService.readSpreadsheetInDrive(googleFile.getFileId(),googleFile.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFile.getFileName(), resource : url, viewType : "html"]
	}
	
	void downloadSpreadSheetFile(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		try {
			User currentUser = request.context.get("__user__")
			GoogleFile googleFile = request.context as GoogleFile
			String downloadURL = spreadSheetService.getFileDownloadURL(currentUser, googleFile.getFileId(),googleFile.getFileType())
			response.flash = "   <FONT Color='#0000FF'><a href=" + downloadURL +" target='_new' >Click Here To Download File </a></FONT>"
		} catch (GoogleJsonResponseException googleExceptionFileNotFound) {
			response.flash = " <FONT Color='#FF0000'> File Not Found ,it might be deleted from drive </FONT>"
		} catch(Exception ex) {
			
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
	}

	void onNewCheckRootExistance(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		User currentUser = request.context.get("__user__")
		syncService.checkAndFillRootDirectory(currentUser)
	}
	
	void downloadSpreadSheetFileFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		try {
			User currentUser = request.context.get("__user__")
			def listIds = request.context.get("_ids")
			GoogleFile googleFileFromList = GoogleFile.all().filter("id=?",listIds[0]).fetchOne()
			String downloadURL = spreadSheetService.getFileDownloadURL(currentUser, googleFileFromList.getFileId(),googleFileFromList.getFileType())
			response.flash = "<FONT Color='#0000FF'> <a href=" + downloadURL +" target='_new' > Click Here Download File </a></FONT>"
		} catch (GoogleJsonResponseException googleExceptionFileNotFound) {
			response.flash = " <FONT Color='#FF0000'> File Not Found ,it might be deleted from drive </FONT>"
		} catch(Exception ex) {
		
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}

	}

	void readSpreadSheetInDrive(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		GoogleFile googleFile = request.context as GoogleFile
		String fileId = googleFile.getFileId()
		String driveUrl = spreadSheetService.readSpreadsheetInDrive(fileId,googleFile.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFile.getFileName(), resource : url, viewType : "html"]
	}
	
	void readSpreadSheetInDriveFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		def listIds = request.context.get("_ids")
		GoogleFile googleFileFromList = GoogleFile.all().filter("id=?",listIds[0]).fetchOne()
		String driveUrl = spreadSheetService.readSpreadsheetInDrive(googleFileFromList.getFileId(),googleFileFromList.getFileType())
		URL url = new URL(driveUrl);
		response.view = [title : googleFileFromList.getFileName(), resource : url, viewType : "html"]
	}

	
	void checkSharingList(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		
		try {
			googleFile = documentService.checkSharingUserList(currentUser, googleFile)
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		
		}
		response.values=googleFile
	}
	
	void setFieldOfSelectedModel(ActionRequest request, ActionResponse response) {
			TemplateSpreadSheet ts = request.context.get("_parent") as TemplateSpreadSheet
			MetaModel metamodel=MetaModel.all().filter("id=?", ts.getTemplateModel().getId()).fetchOne()
			response.values=["selectedModel_dummy":metamodel]
		}
}

