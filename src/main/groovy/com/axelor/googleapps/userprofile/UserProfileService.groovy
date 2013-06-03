package com.axelor.googleapps.userprofile

import java.lang.annotation.Retention;

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.UserProfile;
import com.axelor.googleapps.userutils.Utils
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.googleappsconn.drive.Directory;
import com.axelor.googleappsconn.drive.GoogleDrive
import com.axelor.googleappsconn.drive.GoogleFile;
import com.axelor.googleappsconn.sharing.SharingOperation;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


class UserProfileService {

	@Inject Utils userUtils
	@Inject GoogleDrive googleDrive
	public UserProfile checkUserInUserProfile(User currentUser){
		
		UserProfile userProfile = UserProfile.all().filter("driveUser=?", currentUser).fetchOne()
		return userProfile		
	}
	@Transactional
	public UserProfile savePermissionId(UserProfile userProfile) {
		
		String fileId
		String permissionId
		AppsCredentials appsCredentials = AppsCredentials.all().filter("driveUser=?",userProfile.getDriveUser()).fetchOne()
		
		if(appsCredentials == null) 
			return userProfile
		userUtils.startApps(userProfile.getDriveUser())
		
		if(!googleDrive.isAuthorised()) 
			return userProfile
		
		Directory rootDirectory = new Directory();
		fileId = rootDirectory.getRootDirectoryId(googleDrive)
		SharingOperation sharingOperation = new SharingOperation(googleDrive);
		permissionId = sharingOperation.getPermissionId(fileId);
		userProfile.setPermissionId(permissionId)
		
		if(userProfile.getId() != null){
			userProfile.save() 
		}
		
		return userProfile
	}
}
