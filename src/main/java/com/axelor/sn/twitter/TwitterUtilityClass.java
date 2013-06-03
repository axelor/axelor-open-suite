package com.axelor.sn.twitter;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.axelor.apps.base.db.ApplicationCredentials;
import com.axelor.apps.base.db.PersonalCredential;
import com.axelor.apps.base.db.SocialNetworking;
import com.google.inject.Inject;

public class TwitterUtilityClass {
	@Inject
	public TwitterConnectionClass twtconnect;

	public boolean isTWTLivecheck(com.axelor.auth.db.User user) {
		boolean liveStatus = false;
		SocialNetworking sn = SocialNetworking.all()
				.filter("name = ?", "twitter").fetchOne();
		ApplicationCredentials application = ApplicationCredentials.all()
				.filter("snType = ?", sn).fetchOne();
		PersonalCredential credential = PersonalCredential.all()
				.filter("userId = ? and snType = ?", user, sn).fetchOne();

		if (credential != null) {
			liveStatus = true;
			if (twtconnect.isNullTWTObj(twtconnect.twitter,
					twtconnect.accessToken)) {
				twtconnect.twitter = new TwitterFactory().getInstance();
				twtconnect.accessToken = new AccessToken(
						credential.getUserToken(),
						credential.getUserTokenSecret());
				twtconnect.twitter.setOAuthConsumer(application.getApikey(),
						application.getApisecret());
				twtconnect.twitter.setOAuthAccessToken(twtconnect.accessToken);
			}
		}
		return liveStatus;
	}
}
