package com.axelor.googleapps.directory

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.auth.db.User
import com.axelor.apps.base.db.AppsCredentials
import com.axelor.googleappsconn.drive.GoogleDrive
import com.axelor.apps.base.db.GoogleDirectory
import com.axelor.googleapps.userutils.Utils
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class DirectoryController {
	
	@Inject DirectoryService directoryServiceObj
	@Inject Utils userUtils 
	
	void saveDirectory(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response)) 
			return
		
		GoogleDirectory googleDirectory = request.context as GoogleDirectory
		User currentUser = request.context.get("__user__")
		
		try {
			if(googleDirectory.getDirectoryId() == null) {
				
				GoogleDirectory newGoogleDirectory = new GoogleDirectory()
				newGoogleDirectory = directoryServiceObj.createDirectoryInDrive(currentUser, googleDirectory)
				response.values = newGoogleDirectory
			} else {
				response.values = directoryServiceObj.updateDirectory(currentUser, googleDirectory);
			}
			
		} catch (Exception ex) {		
			if(ex.getMessage().contains("File not found")){
				response.flash = googleDirectory.getDirectoryName() + " Directory Not Found on your Google Drive, It might have been deleted from there."
				throw new Exception(googleDirectory.getDirectoryName() + " Directory Not Found on your Google Drive, It might have been deleted from there.")
			}
			else
				throw new Exception(ex.getMessage())
		}
	}

	public void removeDirectories(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
			
		try{
			User currentUser = request.context.get("__user__")
			def listIds = request.context.get("_ids")
			response.flash = directoryServiceObj.removeDirectories(listIds, currentUser)
			
		} catch(Exception googleExceptionFileNotFound) {		
			if(googleExceptionFileNotFound.getMessage().contains("File not found")) {
				response.flash = "Directory Not Found ,it might have been deleted from drive"				
				throw new Exception("Directory Not Found ,it might have been deleted from drive")
			}
			else
				throw new Exception(googleExceptionFileNotFound.getMessage())
		}
	}
	
	public void trashDirectoriesFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		def listIds = request.context.get("_ids")
		User currentUser = request.context.get("__user__")
		
		try {
			response.flash = directoryServiceObj.trashDirectories(currentUser, listIds)
			
		} catch (Exception ex) {		
			if(ex.getMessage().contains("File not found")) {
				response.flash = "These Directories Not Found on your Google Drive, You might have been deleted from there."
				throw new Exception("These Directories Not Found on your Google Drive, You might have been deleted from there.")
			}
			else
				throw ex
		}
	}

	public void restoreDirectoriesFromGrid(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		def listIds = request.context.get("_ids")
		User currentUser = request.context.get("__user__")
		
		try {
			response.flash = directoryServiceObj.untrashDirectories(currentUser, listIds)
			
		} catch(Exception ex) {				
			if(ex.getMessage().contains("File not found")) {
				response.flash = "These Directories Not Found on your Google Drive, You might have been deleted from there."								
				throw new Exception("These Directories Not Found on your Google Drive, You might have been deleted from there.")
			}
			else
				throw new Exception(ex.getMessage())
		}
	}

	public void deleteFromTrash(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		def listIds = request.context.get("_ids")
		User currentUser = request.context.get("__user__")
		
		try {
			response.flash = directoryServiceObj.deleteDirectoriesFromTrash(currentUser, listIds)
			
		} catch (Exception ex) {		
			if(ex.getMessage().contains("File not found")) {
				response.flash = "These Directories Not Found on your Google Drive, You might have been deleted from there."
				throw new Exception("These Directories Not Found on your Google Drive, You might have been deleted from there.")
			}
			else
				throw new Exception(ex.getMessage())
		}
	}
}