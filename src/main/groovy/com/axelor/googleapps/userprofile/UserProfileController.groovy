package com.axelor.googleapps.userprofile

import com.axelor.auth.db.User
import com.axelor.apps.base.db.UserProfile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class UserProfileController {
	@Inject UserProfileService userProfileService
	
	void checkUserProfileExistance(ActionRequest request, ActionResponse response) {

		UserProfile userProfile = request.context as UserProfile 
		
		if (userProfileService.checkUserInUserProfile(userProfile.getDriveUser()) != null)
			userProfile = userProfileService.checkUserInUserProfile(userProfile.getDriveUser())
		
		if(request.context.get("__user__") != userProfile.getDriveUser()){
			response.values = userProfile
			return
			
		} else if(userProfile.getPermissionId() == null)
			userProfile = userProfileService.savePermissionId(userProfile)
		
		if(userProfile != null)
			response.values = userProfile		
	}
}
