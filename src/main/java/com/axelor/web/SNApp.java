package com.axelor.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import com.axelor.auth.db.User;
import com.axelor.sn.service.SNFBService;
import com.google.inject.Inject;

//http://localhost:8080/axelor-demo/ws/snapps/100
@Path("/snapps")
public class SNApp {
	@Inject
	SNFBService service;

	public boolean contactValue;
	
	@GET
	@Path("{id}")
	public String get(@PathParam("id") Long id, @QueryParam("code") String code) {
		String stat = "";
		String scriptMsg = "";
		try {

			Subject subject = SecurityUtils.getSubject();
			User currentUser = User.all()
					.filter("self.code = ?1", subject.getPrincipal())
					.fetchOne();

			stat = service.storingToken(currentUser, code);
			
			if (stat.equals("1"))
				code = "Login Successfull!!";

			else
				code = "There is Some Technical Issues!! Please Try Later";
			
			System.out.println(contactValue);
			scriptMsg = " <html ><head><script type=\"text/javascript\">function onLoadPage(){var r=confirm(\""
					+ code
					+ "\");if (r==true)  { 	 window.close();  }else  {  	window.close();  }    }</script></head><body onLoad=\"onLoadPage()\"><center><img src=\"http://i47.tinypic.com/jqp34p.png\" width=\"10%\"> <br><b><h2>Facebook Connector</h2></b></center></body> </HTML>";

		} catch (Exception e) {
			code = e.getMessage();
			e.printStackTrace();
		}
		return scriptMsg;
	}
}
