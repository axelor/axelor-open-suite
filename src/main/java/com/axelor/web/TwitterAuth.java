package com.axelor.web;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import com.axelor.auth.db.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import com.axelor.apps.base.db.PersonalCredential;
import com.axelor.apps.base.db.SocialNetworking;
import com.axelor.sn.service.SNTWTService;
import com.axelor.db.*;
import java.io.*;
//http://localhost:8080/axelor-demo/ws/twitterauth/100
@Path("/twitterauth")
public class TwitterAuth {
	@com.google.inject.Inject
	SNTWTService service;

	String scriptMsg = "";

	@GET
	@Path("{id}")
	public String get(@PathParam("id") Long id,
			@QueryParam("oauth_verifier") String code) {

		EntityManager em = JPA.em();
		EntityTransaction tx = em.getTransaction();

		try {

			System.out.println(code);
			Subject subject = SecurityUtils.getSubject();
			User currentUser = User.all()
					.filter("self.code = ?1", subject.getPrincipal())
					.fetchOne();
			File file = new File(currentUser.getCode());

			ObjectInputStream inStream = new ObjectInputStream(
					new FileInputStream(file));
			Twitter twitter = (Twitter) inStream.readObject();
			RequestToken requestToken = (RequestToken) inStream.readObject();
			System.out.println(twitter);
			System.out.println(requestToken);
			file.delete();
			inStream.close();
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
					code);
			System.out.println(accessToken.getToken());
			System.out.println(accessToken.getTokenSecret());
			SocialNetworking sn = SocialNetworking.all()
					.filter("name=?", "twitter").fetchOne();

			if (!accessToken.getToken().isEmpty()) {
				PersonalCredential personalCredential = new PersonalCredential();
				personalCredential.setSnUserName("@"
						+ accessToken.getScreenName());
				personalCredential.setUserId(currentUser);
				personalCredential.setSnType(sn);
				personalCredential.setUserToken(accessToken.getToken());
				personalCredential.setUserTokenSecret(accessToken
						.getTokenSecret());
				tx.begin();
				personalCredential.merge();
				tx.commit();
				//service.importFollowers(currentUser);
				code = "Login Successfull!!";
			}

			else {
				code = "There is Some Technical Issues!! Please Try Later";
			}
			scriptMsg = " <html ><head><script type=\"text/javascript\">function onLoadPage(){var r=confirm(\""
					+ code
					+ "\");if (r==true)  { 	 window.close();  }else  {  	window.close();  }    }</script></head><body onLoad=\"onLoadPage()\"><center><img src=\"http://i47.tinypic.com/jqp34p.png\" width=\"10%\"> <br><b><h2>Twitter Connector</h2></b></center></body> </HTML>";
		} catch (Exception e) {
			code = e.getMessage();
			e.printStackTrace();
		}
		return scriptMsg;
	}
}
