package com.axelor.googleapps.authorize

import com.axelor.apps.base.db.AppsCredentials
import com.axelor.apps.base.db.GoogleAppsConfig
import com.axelor.auth.db.User;
import com.axelor.googleapps.authorize.AuthorizationService
import com.axelor.googleappsconn.document.DocumentOperations;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.spreadsheet.SpreadsheetOperations;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Request;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


class AuthorizationController {
	/**
	 * initiates the user authorization to google server and instruct the user
	 * for copy paste the URL for authourization.
	 * @param request ActionRequest
	 * @param response ActionResponse
	 */
	@Inject AuthorizationService authService
	void openGoogleApps(ActionRequest request, ActionResponse response){
		
		String authURL
		try{
			authURL = authService.getAuthURL()
		}
		catch(Exception ex) {
			throw new Exception(ex.getMessage())
		}
		
		if (authURL == null) {
			response.flash = "Please First Configure GoogleApps by going in 'Configuration' menu - Inform the Administrator for that."
			return
		}
		response.flash = "<a href='" + authURL + "' target='_new'> Click Here to be Authorized </a>"
	}
	/**
	* checks the existence for AppsCredentials for current user in database
	* @param request
	* @param response
	*/
   void checkUserExistance(ActionRequest request, ActionResponse response) {

	   AppsCredentials appsCredentials=request.context as AppsCredentials
	   User currentUser = request.context.get("__user__")
	   try {
		   appsCredentials = authService.checkUserInCredentials(currentUser)
	   }catch(Exception ex) {
		   throw new Exception(ex.getMessage())
	   }
	   response.values=appsCredentials
   }
	/**
	 * load the google apps configuration in view from database.
	 * @param request
	 * @param response
	 */
	void onLoadAuthorizeView(ActionRequest request, ActionResponse response) {

		GoogleAppsConfig googleAppsConfig = GoogleAppsConfig.all().fetchOne()
		GoogleAppsConfig googleAppsConfigRequestObj =  request.context as GoogleAppsConfig
		googleAppsConfigRequestObj = googleAppsConfig
		response.values = googleAppsConfigRequestObj
	}
	
	void unauthorizeUser(ActionRequest request, ActionResponse response) {
		
		User currentUser = request.context.get("__user__")
		response.flash = " <FONT Color='#0000FF'> <a href=" + authService.unauthorizeUserURL(currentUser)+" target='_new' >Click Here to Complete Unauthorize Process</a><?FONT>";
	}
	void checkUserStatus(ActionRequest request, ActionResponse response) {
		
		User currentUser = request.context.get("__user__")
		String message = authService.checkUserAuthStatus(currentUser)
		response.flash = "<FONT Color='#009900'>"+ message +"</FONT>"
	}
}
