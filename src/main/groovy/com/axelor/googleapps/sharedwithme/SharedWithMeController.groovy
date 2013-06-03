package com.axelor.googleapps.sharedwithme

import com.axelor.auth.db.User
import com.axelor.apps.base.db.GoogleFile
import com.axelor.googleapps.userutils.Utils
import com.axelor.googleapps.document.DocumentService
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class SharedWithMeController {

	@Inject DocumentService documentService
	@Inject SharedWithMeService sharedWithMeServiceObj
	@Inject Utils userUtils
	void downloadFileFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		try {
			User currentUser = request.context.get("__user__")
			def listIds = request.context.get("_ids")
			GoogleFile googleFile = GoogleFile.all().filter("id=?",listIds[0]).fetchOne()
			String downloadURL = sharedWithMeServiceObj.getfiledownloadURL(currentUser,googleFile.getFileId(),googleFile.getFileType())
			response.flash = "  <FONT Color='#0000FF'><a href=" + downloadURL +" target='_new' >Click Here To Download File</FONT>"
			
		} catch (Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
	}
	
	void checkSharingList(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		
		try {
			googleFile = documentService.checkSharingUserList(currentUser, googleFile)
			
		} catch(Exception ex) {
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw ex
		}
		response.values = googleFile
	}

	void updateSharedWithmeFile(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		
		try {
			googleFile = sharedWithMeServiceObj.updateSharedwithmeFile(currentUser, googleFile)
			
		} catch (Exception ex) {		
			if(ex.getMessage().contains("File not found"))
				throw new Exception("This File Not Found on your Google Drive, You might have been deleted from there.")
			else
				throw new Exception(ex.getMessage())
		}
		response.values = googleFile
	}
}

