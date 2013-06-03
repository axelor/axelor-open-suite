package com.axelor.googleapps.uploadFile

import com.axelor.auth.db.User;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class UploadFileController {
	
	@Inject UploadFileService uploadfileServiceOBJ
	@Inject Utils userUtils
	void uploadFile(ActionRequest request, ActionResponse response) {
		
		if(!userUtils.validAppsConfig(request, response))
			return
		
		User currentUser = request.context.get("__user__")
		GoogleFile googleFile = request.context as GoogleFile
		
		try {
			googleFile = uploadfileServiceOBJ.uploadFile(currentUser, googleFile)
		} catch (Exception ex) {
			response.flash = "There was some problem on Uploading File, Check that You are authorized well."
			throw new Exception("There was some problem on Uploading File, Check that You are authorized well.")
		}
		response.flash = "File   <FONT Color='#01DF3A'>  " + googleFile.getFileName() +"</FONT> uploaded  successfully  "
	}
}
