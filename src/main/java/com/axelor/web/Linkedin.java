package com.axelor.web;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.axelor.auth.db.User;
import com.axelor.sn.service.LnService;

@Path("/linkedin")
public class Linkedin {
	
	@Inject
	LnService LinkedinService;

	@GET
	@Path("{id}")
	public String get(@PathParam("id") Long id, @QueryParam("oauth_verifier") String verifier, @QueryParam("oauth_token") String token) throws Exception 
	{
		String str=null;
		Subject subject = SecurityUtils.getSubject();
		User currentUser = User.all().filter("self.code = ?1", subject.getPrincipal()).fetchOne();
		boolean status=LinkedinService.getUserToken( verifier, currentUser, token);		
		if(status)
		{
			str="<html><head><script type=\"text/javascript\">  function onLoadPage(){  alert(\"Successfully Logged In\");  window.close(); }   </script></head><body onLoad=\"onLoadPage()\"></body> </HTML>";
		}
		return str;
	}
}
