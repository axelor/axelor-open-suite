package com.axelor.googleapps.authorize

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.FilePermission;
import com.axelor.apps.base.db.GoogleDirectory;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.TemplateFile;
import com.axelor.apps.base.db.UserProfile;
import com.axelor.googleappsconn.drive.FileTypes

import com.axelor.auth.db.User
//import com.axelor.db.JPA
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.apps.base.db.GoogleAppsConfig

import com.axelor.rpc.ActionRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.inject.persist.Transactional;
import com.itextpdf.text.pdf.AcroFields.Item;

import java.net.HttpURLConnection

import javax.persistence.EntityManager;

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

class AuthorizationService {

	/**
	 * builds the basic objects for authorization and returns the url for authorization
	 */
	public String getAuthURL( ) {
		
		HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		JsonFactory JSON_FACTORY = new JacksonFactory();
		GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		if (googleAppsConfig == null) return null
		List<String> dataScopes = new ArrayList<String>();
		dataScopes.add("https://docs.google.com/feeds/");
		dataScopes.add("https://docs.googleusercontent.com/");
		dataScopes.add("https://spreadsheets.google.com/feeds/");
		dataScopes.addAll(Arrays.asList(DriveScopes.DRIVE));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, googleAppsConfig.getClientId(), googleAppsConfig.getClientSecret(), dataScopes)
				.setAccessType("offline")
				.setApprovalPrompt("force")
				.build();
		String authURL = flow.newAuthorizationUrl().setRedirectUri(googleAppsConfig.getRedirectURI()).build();
		return authURL;
	}
	/**
	 * authorize the current user with the authcode to google server using the apps credentials, config and then 
	 * gets tokens and call the saveTokens
	 * @param currentUser User this is the entity of the user currently logged in.
	 */
	public boolean authorize(User currentUser, String authCode) {

		GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		JsonFactory JSON_FACTORY = new JacksonFactory();
		List<String> dataScopes = new ArrayList<String>();
		dataScopes.add("https://docs.google.com/feeds/");
		dataScopes.add("https://docs.googleusercontent.com/");
		dataScopes.add("https://spreadsheets.google.com/feeds/");
		dataScopes.addAll(Arrays.asList(DriveScopes.DRIVE));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, googleAppsConfig.getClientId(), googleAppsConfig.getClientSecret(), dataScopes)
				.setAccessType("offline")
				.setApprovalPrompt("force")
				.build();
		TokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(googleAppsConfig.getRedirectURI()).execute();
		Credential credential = flow.createAndStoreCredential(response,googleAppsConfig.getClientEmail());
		if (credential == null) return false
		if (credential.getAccessToken() == null || credential.getRefreshToken() == null)
			return false
		saveTokens(currentUser,credential.getAccessToken(),credential.getRefreshToken())
		return true
	}
	/**
	 * saves the access token and refresh token for current user in database. 
	 * @param currentUser String this is the entity of the user currently logged in to axelor.
	 * @param accessToken String
	 * @param refreshToken String
	 */
	@Transactional
	public void saveTokens(User currentUser, String accessToken, String refreshToken) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
		Date date = new Date();
		String creationTime = dateFormat.format(date);
		AppsCredentials currentUserCredentials = new AppsCredentials()
		currentUserCredentials.setLastAccessTime(creationTime)
		currentUserCredentials.setAccessToken(accessToken)
		currentUserCredentials.setRefreshToken(refreshToken)
		currentUserCredentials.setDriveUser(currentUser)
		currentUserCredentials.persist()
	}
	/**
	* checks if the user's entry exist in apps credential and if not then
	* do one entry for that user.
	* @param currentUser User this is the entity of the user currently logged in to axelor.
	*/
   public AppsCredentials checkUserInCredentials(User currentUser) {
	   
	   GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
	   
	   if (googleAppsConfig == null || googleAppsConfig.getRedirectURI() == null || googleAppsConfig.getClientId() == null
	   || googleAppsConfig.getClientSecret() == null || googleAppsConfig.getClientEmail() == null) {
		   return null
	   }
	   AppsCredentials returnAppsCredentials = AppsCredentials.all().filter("driveUser=?", currentUser).fetchOne()
	   return returnAppsCredentials
   }
   @Transactional
	public String unauthorizeUserURL(User currentUser) {
		AppsCredentials appsCredentials = AppsCredentials.all().filter("driveUser=?", currentUser).fetchOne()
		
		if(appsCredentials != null) {
			String token=appsCredentials.getAccessToken()
			appsCredentials?.remove()
			List<TemplateFile> listTelmplateFile=TemplateFile.all().filter("googleFile.driveUser=?",currentUser).fetch()
			listTelmplateFile.each { it.remove() }
			List<GoogleFile> googleFileList = GoogleFile.all().filter("driveUser=?", currentUser).fetch()
			
			for (GoogleFile googleFile : googleFileList) {
				if (googleFile.getFilePermissions() != null) {
					List<FilePermission> filePermisisonList = FilePermission.all().filter("googleFile=?",googleFile).fetch()
					for (FilePermission filePermission :filePermisisonList ) {
						filePermission.remove()
					}
				}
				googleFile.remove()
			}
			List<GoogleDirectory> googleDirectories = GoogleDirectory.all().filter("driveUser=?", currentUser).fetch()
			
			for(GoogleDirectory dirToMerge : googleDirectories){
					dirToMerge.setParent(null)
					dirToMerge.merge()
			}
			List<GoogleDirectory> listgoogleDirectories = GoogleDirectory.all().filter("driveUser=?", currentUser).fetch()
			listgoogleDirectories.each {
					it.remove()
			}
			UserProfile thisUserProfile = UserProfile.all().filter("driveUser=?", currentUser).fetchOne()
			
			if(thisUserProfile != null){
				FilePermission.all().filter("sharingUser=?", thisUserProfile).fetch().each {
					it.remove()
				}
				thisUserProfile.remove()
			}
			return "https://accounts.google.com/o/oauth2/revoke?token=" + token
		} else {
			return "User is not stil Authorized"
		}
	}

	public String checkUserAuthStatus(User currentUser) {
		
		AppsCredentials appsCredentials = AppsCredentials.all().filter("driveUser=?", currentUser).fetchOne()
		if(appsCredentials == null)
			return "You need to authorize your account with Google-Apps , Please Follow the instruction Steps "
		else if(appsCredentials.getAccessToken() == null || appsCredentials.getRefreshToken() == null)
			return "You need to authorize your account with Google-Apps , Please Follow the instruction Steps "
		else
			return "You Are Already Authorized in Google-Apps"
	}
}

