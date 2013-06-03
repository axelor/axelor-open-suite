package com.axelor.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.axelor.auth.db.User;
import com.axelor.googleapps.authorize.AuthorizationService;
import com.google.inject.Inject;

/**
 * class designed to accept the authorisation code sent by Goolge server
 * when new users is authorising with google-apps. it'll work as a web-servlet,
 * catch the auth-code for the current user, and call the authService to authorise
 * the current user.
 */

@Path("/gapps")
public class GAppsAuthorization {

	@Inject
	AuthorizationService authService;
	@GET
	@Path("{id}")
	public String get(@PathParam("id") Long id, @QueryParam("code") String authCode) throws Exception {

		Subject subject = SecurityUtils.getSubject();
		User currentUser = User.all().filter("self.code = ?1", subject.getPrincipal()).fetchOne();
		String msg = "";
		if (authService.authorize(currentUser, authCode))
			msg = "YOU Have Successfully Authorized, Now GO TO Axelor Demo.";
		else
			msg = "User Authorization Failed, Try after some time or Inform technical person.";
		String scriptMsg = " <html ><head><script type=\"text/javascript\">function onLoadPage(){var r=confirm(\""
							+ msg
							+ "\");if (r==true)  { 	 window.close();  }else  {  	window.close();  }    }</script></head>" 
							+ " <body onLoad=\"onLoadPage()\"><center><img src=\"http://i47.tinypic.com/jqp34p.png\" width=\"10%\"> <br>Google Apps Connector</center></body> </HTML>";
		return scriptMsg;
	}
}
