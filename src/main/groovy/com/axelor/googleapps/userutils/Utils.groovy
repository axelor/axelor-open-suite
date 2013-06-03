package com.axelor.googleapps.userutils

import java.awt.print.Printable;
import java.util.Date;
import java.util.Map;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property
import com.axelor.db.mapper.PropertyType;
import com.axelor.apps.base.db.GoogleDirectory;
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.apps.base.db.GoogleAppsConfig;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.drive.GoogleFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers.Returns;

class Utils {
	
	@Inject GoogleDrive googleDrive
	
	// method to be called on user login on click of main menu
	// currently it is called on each operation in services
	// on this method call to build obj : 697106749 nanosec and if directly return : 14630 nanosec
	// the GoogleDrive object requires 5 bytes in session

	public void startApps(User currentUser){
		
		if(googleDrive.isAuthorised()){ 
			return
		}
		
		GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		
		if(googleAppsConfig == null) throw new Exception("The admin has not configured the google-apps, You cant' start App.")
		AppsCredentials appsCredentials = AppsCredentials.all().filter("driveUser=?", currentUser).fetchOne()
		
		if(appsCredentials == null) throw new Exception("You are not authorized yet. Go to Settings->Authorize menu.")
		boolean refreshed
		
		try {
			if(googleDrive != null && !googleDrive.isAuthorised())
				refreshed = googleDrive.refresh(googleAppsConfig.getClientId(),
					googleAppsConfig.getClientSecret(), appsCredentials.getRefreshToken())
		} catch(UnknownHostException ukHostEx) {
			
			if(!refreshed)
				return null
				
		} catch(Exception ex){
		
			if(ex.getMessage().contains("invalid_grant"))
				throw new Exception("Your Google Credentials are obsolete, Please follow the authorization process again. Settings->Authorize.")
				
		}
		
	}
	
	/**
	 * gives the GoogleDrive object by buiding it from credentials for current user and 
	 * refresh token.
	 * @param appsCredentials AppsCredential
	 * @return
	 */
	// this method is used at single place - document service, don't remove it...
	public GoogleDrive getUserGoogleDrive(User currentUser) {
		
		AppsCredentials appsCredentials = AppsCredentials.all().filter("driveUser=?",currentUser).fetchOne()
		GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		GoogleDrive googleDrive = new GoogleDrive()
		boolean refreshed
		
		try {
			refreshed = googleDrive.refresh(googleAppsConfig.getClientId(),
					googleAppsConfig.getClientSecret(), appsCredentials.getRefreshToken())
		} catch(UnknownHostException ukHostEx) {
			
			if(!refreshed)
				return null
				
		} catch(Exception ex){
			
			if(ex.getMessage().contains("invalid_grant"))
				throw new Exception("Your Google Credentials are obsolete, Please follow the authorization process again. Settings->Authorize.")
		}
		
		if(refreshed)
			return googleDrive
		else return null
	}

	public boolean validAppsConfig(ActionRequest request,ActionResponse response) {
		
		User currentUser = request.context.get("__user__")
		AppsCredentials appCredentials = AppsCredentials.all().filter("driveUser=?", currentUser).fetchOne()
		
		if(appCredentials == null) {
			String err = "You have not authorized with GoogleApps, Please do it first."
			throw new Exception(err)
			return false
		}
		return true
	}

	public <T> Map<String,Object> getDataMapFromObject(T objectData) {
		
		Map<String,Object> dataMap = new HashMap<String,Object>()
		Mapper objectMapper = Mapper.of(objectData.getClass())
		String fileName = objectMapper.getNameField().get(objectData)
		
		if(fileName==null)
			fileName = "Untitled Document"
		dataMap.put("fileNameforThisDataObject", fileName)
		String key,value;
	
		for (Property property : objectMapper.getProperties() ) {
			// for setting value of property
			key=""
			value=""
			if(property.getType() == PropertyType.ONE_TO_MANY || property.getType() == PropertyType.MANY_TO_MANY) {

				def listDataObject = property.get(objectData)
				Mapper inMapper = Mapper.of(property.getTarget())
				key = property.getName()
				if(inMapper.getNameField() == null) {
					dataMap.put(key, listDataObject.size())
				} else { 
					ArrayList<String> listData = new ArrayList<String>()
					Iterator<Object> objIterator = listDataObject.iterator()
					objIterator.each {
						
						if(inMapper.getNameField().get(it) != null)
							listData.add(inMapper.getNameField().get(it).toString())
					}
					dataMap.put(key, listData)
				}
			} else if(property.getType() == PropertyType.MANY_TO_ONE) {
				
				Mapper inMapper = Mapper.of(property.get(objectData).getClass())
				String nameFieldVal = inMapper.getNameField()?.get(property.get(objectData))
				key = property.getName()
				value = nameFieldVal
				dataMap.put(key, value)
			} else if(property.getType() == PropertyType.ONE_TO_ONE) {
				// do nothing because there is no such relationship
			} else {
				key = property.getName()
				value = property.get(objectData)
				dataMap.put(key, value)
			}
		}
		return dataMap;
	}
}
