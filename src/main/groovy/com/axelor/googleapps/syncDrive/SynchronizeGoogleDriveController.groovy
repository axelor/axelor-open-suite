package com.axelor.googleapps.syncDrive

import javax.swing.event.ListDataEvent;

import com.axelor.auth.db.User;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.googleappsconn.drive.Directory
import com.axelor.googleappsconn.drive.GoogleDrive
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class SynchronizeGoogleDriveController {
	@Inject SynchronizeWithGoogleDriveService syncWithGoogleDriveObj
	@Inject Utils userUtils
	/**
	 * synchronize the files and directories of current user in database from google drive.
	 * @param request
	 * @param response
	 */
	void sychronization(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
	
		User currentUser = request.context.get("__user__")
		try {
			response.flash = syncWithGoogleDriveObj.synchronizeDrive(currentUser)
		} catch (Exception ex) {
		
			if(ex.getMessage().contains("File not found")) {
				response.flash = "Some Files Not Found on your Google Drive, You might have been deleted from there."
				throw new Exception("Some Files Not Found on your Google Drive, You might have been deleted from there.")
			}
			else
				throw new Exception(ex.getMessage())
		
		}
	}
}
