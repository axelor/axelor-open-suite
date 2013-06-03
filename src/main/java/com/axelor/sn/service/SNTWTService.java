package com.axelor.sn.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.ApplicationCredentials;
import com.axelor.apps.base.db.MsgInbox;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TwitterDirectMessage;

import com.axelor.apps.base.db.PersonalCredential;
import com.axelor.apps.base.db.TwitterPostTweet;
import com.axelor.apps.base.db.SocialNetworking;
import com.axelor.apps.base.db.TwitterComment;
import com.axelor.apps.base.db.TwitterConfig;
import com.axelor.apps.base.db.TwitterFollowerRequest;
import com.axelor.apps.base.db.TwitterHomeTimeline;
import com.axelor.sn.twitter.TwitterConnectionClass;
import com.axelor.sn.twitter.TwitterUtilityClass;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.joda.time.DateTime;

/**
 * 
 * @author axelor-APAR
 * 
 */
public class SNTWTService {
	@Inject
	TwitterUtilityClass twtUtil;

	@Inject
	public TwitterConnectionClass twtconnect;
	String acknowledgment, apiKey, apiSecret, userToken, userTokenSecret,
			redirectUrl;

	@SuppressWarnings("rawtypes")
	ArrayList<HashMap> lstReturnResponse = new ArrayList<HashMap>();
	@SuppressWarnings("rawtypes")
	HashMap mapRetriveValues = new HashMap();

	/**
	 * @author axelor-APAR
	 * @param user
	 * @param sn
	 * @return authorizationUrl
	 */
	@Transactional
	public String obtainToken(User user, SocialNetworking sn) {
		try {

			List<ApplicationCredentials> query = ApplicationCredentials.all()
					.filter("snType=? ", sn).fetch();
			List<PersonalCredential> credential = PersonalCredential.all()
					.filter("userId=? and snType=?", user, sn).fetch();
			if (query.isEmpty()) {
				acknowledgment = "Sorry You can't Do anyting Admin Doesnt set Application Credentials ";
				throw new javax.validation.ValidationException(
						"No Application Credentials Available");
			} else {
				if (!credential.isEmpty()) {
					acknowledgment = "You Already Have One Account Associated...";
					throw new javax.validation.ValidationException(
							"You Already Have One Account Associated...");
				} else {
					for (int i = 0; i < query.size(); i++) {
						apiKey = query.get(i).getApikey();
						apiSecret = query.get(i).getApisecret();
						redirectUrl = query.get(i).getRedirectUrl();
					}
					acknowledgment = twtconnect.getAuthUrl4j(apiKey, apiSecret,
							redirectUrl);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;

	}

	public SocialNetworking saySnType() {
		SocialNetworking snType = SocialNetworking.all()
				.filter("name=?", "twitter").fetchOne();
		return snType;
	}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @param contentToTweet
	 * @return acknowledgement
	 */
	@Transactional
	public String postTweet(User user, String content) {
		try {
			if (twtUtil.isTWTLivecheck(user)) {
				mapRetriveValues = twtconnect.postTweet(content);
				if (mapRetriveValues.size() > 0)
					acknowledgment = mapRetriveValues.get("acknowledgment")
							.toString();
			} else
				acknowledgment = "0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @param contentId
	 *            - it will validate user and make Call to custom-API for
	 *            Deletion
	 * @return acknowledgement
	 */
	@SuppressWarnings("rawtypes")
	@Transactional
	public String getDeleteMessage(User user, List contentId) {
		TwitterPostTweet postMsg;
		try {
			if (twtUtil.isTWTLivecheck(user)) {
				for (int i = 0; i < contentId.size(); i++) {
					postMsg = new TwitterPostTweet();
					postMsg = TwitterPostTweet.all().filter("id=?", contentId.get(i))
							.fetchOne();
					if (!postMsg.getCommentsTweet().isEmpty()) {
						List<TwitterComment> cmntList = TwitterComment.all()
								.filter("id=?", contentId.get(i)).fetch();
						for (int j = 0; j < cmntList.size(); j++)
							cmntList.get(j).remove();
					}

					mapRetriveValues = twtconnect.deleteContent(postMsg
							.getAcknowledgment());

					if (mapRetriveValues.containsKey("acknowledgment"))
						postMsg.remove();
					else
						acknowledgment = "There is Some Problem, Please Try late";
				}
			} else
				acknowledgment = "Please Authorize the Application First";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @return String acknowledgement
	 */
/*
	@Transactional
	public String importFollowers(User user) {

		try {
			SocialNetworking sn = SocialNetworking.all()
					.filter("name=?", "twitter").fetchOne();
			if (twtUtil.isTWTLivecheck(user)) {
				lstReturnResponse = twtconnect.importContact();
				if (!lstReturnResponse.isEmpty()) {
					List<ImportContact> query1 = ImportContact.all()
							.filter("curUser=? and snType=?", user, sn).fetch();
					List<String> str = new ArrayList<String>();
					for (int i = 0; i < query1.size(); i++)
						str.add(query1.get(i).getSnUserId());

					// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
					// ROW OF D/B AND PERSIST INTO IT
					for (int p = 0; p < lstReturnResponse.size(); p++) {
						mapRetriveValues = lstReturnResponse.get(p);
						if (!str.contains(mapRetriveValues.get("twitterId")
								.toString())) {
							ImportContact twtcntc = new ImportContact();
							twtcntc.setSnUserId(mapRetriveValues.get(
									"twitterId").toString());
							twtcntc.setName(mapRetriveValues.get("twitterName")
									.toString());
							twtcntc.setLink(mapRetriveValues.get("twitterLink")
									.toString());
							twtcntc.setSnType(sn);
							twtcntc.setCurUser(user);
							twtcntc.persist();
						}
					}

				} else
					acknowledgment = "1";
			} else
				acknowledgment = "0";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}
*/
	@Transactional
	public String importContactsTWTERP(User user) {

		try {
			SocialNetworking sn = SocialNetworking.all()
					.filter("name=?", "twitter").fetchOne();
			if (twtUtil.isTWTLivecheck(user)) {
				lstReturnResponse = twtconnect.importContact();
				if (!lstReturnResponse.isEmpty()) {
					List<Partner> query1 = Partner.all()
							.filter("curUser=? and snType=?", user, sn).fetch();
					List<String> str = new ArrayList<String>();
					for (int i = 0; i < query1.size(); i++)
						str.add(query1.get(i).getSnUserId());

					// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
					// ROW OF D/B AND PERSIST INTO IT
					for (int p = 0; p < lstReturnResponse.size(); p++) {
						mapRetriveValues = lstReturnResponse.get(p);
						if (!str.contains(mapRetriveValues.get("twitterId")
								.toString())) {
							Partner twtcntc = new Partner();
							twtcntc.setSnUserId(mapRetriveValues.get(
									"twitterId").toString());
							twtcntc.setName(mapRetriveValues.get("twitterName")
									.toString());
							twtcntc.setWebSite(mapRetriveValues.get("twitterLink")
									.toString());
							twtcntc.setIsContact(true);
							twtcntc.setSnType(sn);
							twtcntc.setCurUser(user);
							twtcntc.persist();
						}
					}

				} else
					acknowledgment = "No data found!!!,Try again after sometime";
			} else
				acknowledgment = "Please Authorise The Applicaton First";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;	
	}
	
	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @param twitterUserProfileId
	 * @param msgToSend
	 * @return acknowledgement
	 */
	@Transactional
	public String sentMessage(User user, String id, String msg) {
		try {
			if (twtUtil.isTWTLivecheck(user)) {
				mapRetriveValues = twtconnect.sendMessage(id, msg);
				if (mapRetriveValues.containsKey("acknowledgment"))
					acknowledgment = mapRetriveValues.get("acknowledgment")
							.toString();
			} else
				acknowledgment = "0";
		} catch (Exception e) {
			acknowledgment = e.getMessage();
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @return acknowledgement
	 */
	@Transactional
	public String getInbox(User user) {

		SocialNetworking snType = SocialNetworking.all().filter("name=?","twitter").fetchOne();
		if (twtUtil.isTWTLivecheck(user)) {

			lstReturnResponse = twtconnect.getDirectMessages();
			if (!lstReturnResponse.isEmpty()) {
				List<MsgInbox> inboxRecords = MsgInbox.all().filter("curUser = ? and snType=?", user, snType).fetch();
				for (int i = 0; i < inboxRecords.size(); i++)
					inboxRecords.get(i).remove();

				// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
				// ROW OF D/B AND PERSIST INTO IT
				for (int p = 0; p < lstReturnResponse.size(); p++) {
					mapRetriveValues = lstReturnResponse.get(p);
					MsgInbox msgInbox = new MsgInbox();
					msgInbox.setMessageId(mapRetriveValues.get("msgId").toString());
					msgInbox.setMessageContent(mapRetriveValues.get("msgContent").toString());
					msgInbox.setMessageFrom(mapRetriveValues.get("senderName").toString());
					msgInbox.setMessageFromID(mapRetriveValues.get("senderId").toString());
					msgInbox.setMessageDate(mapRetriveValues.get("receiveDate").toString());
					msgInbox.setCurUser(user);
					msgInbox.setSnType(snType);
					msgInbox.persist();
				}
				return "Success";
			} else
				acknowledgment = "1";
		} else
			acknowledgment = "0";
	
	return acknowledgment;
}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * 
	 * @param contentId
	 *            - it will validate user and make Call to custom-API for
	 *            Deletion
	 * @return acknowledgement
	 */
	/*@Transactional
	public String deleteDirectMessage(User user,
			@SuppressWarnings("rawtypes") List contentId) {
		try {
			if (twtUtil.isTWTLivecheck(user)) {

				for (int i = 0; i < contentId.size(); i++) {
					TwitterInbox delMsg = TwitterInbox.all()
							.filter("id=?", contentId.get(i)).fetchOne();

					mapRetriveValues = twtconnect
							.deleteInbox(delMsg.getMsgId());

					if (mapRetriveValues.containsKey("acknowledgment"))
						delMsg.remove();
				}
			} else
				acknowledgment = "Please Authorize The Application First";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}*/

	/**
	 * @author axelor-APAR It will used to retrieve twitter's HomeTimeLine
	 * @param currentUser
	 * @return String acknowledgement
	 */
	@Transactional
	public String getHomeTimeLine(User user) {

		try {
			if (twtUtil.isTWTLivecheck(user)) {
				try {
					TwitterConfig param = TwitterConfig.all()
							.filter("curUser=?", user).fetchOne();
					if (param == null)
						throw new javax.persistence.NoResultException();
					else {
						if (param.getSince() == false)
							lstReturnResponse = twtconnect.fetchHomeTimeline(
									param.getPage(), param.getContent(), null);
						else {
							EntityManager em = JPA.em();
							Long maxId = (Long) em.createQuery(
									"select MAX(e.timelineContentId) from TwitterHomeTimeline e where e.curUser="
											+ user.getId()).getSingleResult();

							if (maxId == null)
								acknowledgment = "1";
							else
								lstReturnResponse = twtconnect
										.fetchHomeTimeline(param.getPage(),
												param.getContent(), maxId);

						}
					}
				} catch (javax.persistence.NoResultException e) {
					System.out
							.println("No parameter has been set for This User");
					lstReturnResponse = twtconnect
							.fetchHomeTimeline(0, 0, null);
				}
				if (!lstReturnResponse.isEmpty()) {
					TwitterHomeTimeline twtTimeline = new TwitterHomeTimeline();
					List<TwitterHomeTimeline> query1 = TwitterHomeTimeline
							.all().filter("curUser=?", user).fetch();

					for (int i = 0; i < query1.size(); i++)
						query1.get(i).remove();

					// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
					// ROW OF D/B AND PERSIST INTO IT
					for (int i = 0; i < lstReturnResponse.size(); i++) {

						mapRetriveValues = lstReturnResponse.get(i);
						twtTimeline = new TwitterHomeTimeline();
						twtTimeline.setTimelineContentId(Long
								.parseLong(mapRetriveValues.get("updateId")
										.toString()));
						twtTimeline.setScreenName(mapRetriveValues.get(
								"screenName").toString());
						twtTimeline.setActualName(mapRetriveValues.get(
								"twitterName").toString());
						twtTimeline.setActualContent(mapRetriveValues.get(
								"updateContent").toString());
						twtTimeline.setContentDate(mapRetriveValues.get(
								"updateCreationTime").toString());
						twtTimeline.setCurUser(user);
						twtTimeline.persist();
					}
				} else
					acknowledgment = "1";
			} else
				acknowledgment = "0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR
	 * @param currentUser
	 * @return String acknowledgement
	 */
	@Transactional
	public String getFollowerRequest(User user) {
		try {
			if (twtUtil.isTWTLivecheck(user)) {
				lstReturnResponse = twtconnect.retrivePendingRequest();

				if (!lstReturnResponse.isEmpty()) {
					TwitterFollowerRequest twtRequest = new TwitterFollowerRequest();
					List<TwitterFollowerRequest> query1 = TwitterFollowerRequest
							.all().filter("curUser=?", user).fetch();

					if (query1.size() > 0) {
						for (int i = 0; i < query1.size(); i++)
							query1.get(i).remove();

					}

					// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
					// ROW OF D/B AND PERSIST INTO IT
					for (int i = 0; i < lstReturnResponse.size(); i++) {
						mapRetriveValues = lstReturnResponse.get(i);
						twtRequest = new TwitterFollowerRequest();
						twtRequest.setLink(mapRetriveValues.get("twitterLink")
								.toString());
						twtRequest.setUserId(mapRetriveValues.get("twitterId")
								.toString());
						twtRequest.setScreenName(mapRetriveValues.get(
								"twitterScreenName").toString());
						twtRequest.setName(mapRetriveValues.get("twitterName")
								.toString());
						twtRequest.setSendedOn(mapRetriveValues.get("sendedOn")
								.toString());
						twtRequest.setCurUser(user);
						twtRequest.persist();

					}
				} else
					acknowledgment = "You do not have any Pending Request";
			} else
				acknowledgment = "Please Authorize the Application First";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	// Context is PostTweet Original One
	/**
	 * @author axelor-APAR Call from PostTweet
	 * @param currentUser
	 * @param tweetIdOnWhichReplayNeedToMake
	 * 
	 * @return acknowledgement
	 */
	@Transactional
	public String orgGetTweetReplay(User user, String tweetId) {
		@SuppressWarnings("rawtypes")
		ArrayList<HashMap> lstComments = new ArrayList<HashMap>();

		TwitterPostTweet context = TwitterPostTweet.all().filter("acknowledgment=?", tweetId)
				.fetchOne();
		SocialNetworking sn = SocialNetworking.all()
				.filter("name=?", "twitter").fetchOne();

		try {
			if (twtUtil.isTWTLivecheck(user)) {
				lstComments = twtconnect.getCommentsOfTweet(tweetId);
				if (!lstComments.isEmpty()) {
					List<TwitterComment> query1 = TwitterComment.all()
							.filter("curUser=?", user).fetch();
					List<String> str = new ArrayList<String>();
					for (int i = 0; i < query1.size(); i++)
						str.add(query1.get(i).getTweetcommentid());

					// FOR LOOP USED TO CONVERT RESPONSE FROM ARRYALIST TO ONE
					// ROW OF D/B AND PERSIST INTO IT
					for (int i = 0; i < lstComments.size(); i++) {
						mapRetriveValues = lstComments.get(i);
						if (!str.contains(mapRetriveValues.get("commentId")
								.toString())) {
							Partner impcnt = Partner
									.all()
									.filter("snUserId=? and curUser=? and snType=?",
											mapRetriveValues.get("userid"),
											user, sn).fetchOne();
							TwitterComment tcmnt = new TwitterComment();
							tcmnt.setTweetcommentid(mapRetriveValues.get(
									"commentId").toString());
							tcmnt.setContentid(context);
							tcmnt.setTweetFrom(impcnt);
							tcmnt.setTweetContent(mapRetriveValues.get(
									"content").toString());
							tcmnt.setTweetTime(mapRetriveValues.get("time")
									.toString());
							tcmnt.setTweetFevourite(mapRetriveValues.get("fav")
									.toString());
							tcmnt.setCurUser(user);
							tcmnt.merge();
						} else
							acknowledgment = "1";
					}
				} else
					acknowledgment = "1";

			} else
				acknowledgment = "0";
		} catch (javax.persistence.OptimisticLockException ole) {
			System.out.println(ole.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR It will call from Sale-Order and used to Post its
	 *         date as Tweet
	 * @param com
	 *            .axelor.auth.db.User user
	 * @param String
	 *            contentId
	 * @param String
	 *            content
	 * @return
	 */

	@Transactional
	public String postTweetReplay(User user, String contentId, String content) {
		try {
			if (twtUtil.isTWTLivecheck(user)) {
				PersonalCredential credential = PersonalCredential
						.all()
						.filter("userId=? and snType=?",
								user,
								SocialNetworking.all()
										.filter("name=?", "twitter").fetchOne())
						.fetchOne();

				String replayTo = credential.getSnUserName() + content;
				mapRetriveValues = twtconnect.postTweetReplay(contentId,
						replayTo);
				if (!mapRetriveValues.containsKey("acknowledgment"))
					acknowledgment = "There is Some Problem";

			} else
				acknowledgment = "Please Authorize the Application First";
		} catch (Exception e) {
			acknowledgment = e.getMessage();
			e.printStackTrace();
		}
		return acknowledgment;
	}

	/**
	 * @author axelor-APAR it will used to Delete all the detail from Database
	 * @param currentUser
	 * @param idValOfPersonalCredential
	 * @return acknowledgement
	 */
	@Transactional
	public String removeAllDetail(User user, long idVal) {
		try {
			SocialNetworking sn = SocialNetworking.all()
					.filter("name=?", "twitter").fetchOne();
			PersonalCredential credential = PersonalCredential.all()
					.filter("id=? and userId=? and snType=?", idVal, user, sn)
					.fetchOne();

			TwitterComment.all().filter("curUser=?", user).remove();
			TwitterConfig.all().filter("curUser=?", user).remove();
			TwitterDirectMessage.all().filter("curUser=?", user).remove();
			TwitterHomeTimeline.all().filter("curUser=?", user).remove();
			TwitterFollowerRequest.all().filter("curUser=?", user).remove();
			Partner.all().filter("snType=? and curUser=?", sn, user)
					.remove();
			//TwitterInbox.all().filter("curUser=?", user).remove();
			MsgInbox.all().filter("curUser=? and snType=?", user, sn).remove();
			TwitterPostTweet.all().filter("curUser=?", user).remove();
			credential.remove();
			acknowledgment = "You have successfully Removed all associated Data with this account From here";
		}

		catch (Exception e) {
			acknowledgment = e.getMessage();
			e.printStackTrace();
		}
		return acknowledgment;
	}

	// ADDED BY MIHIR ON 16-04-2013 from HERE on to END
	@Transactional
	public TwitterPostTweet addTweet(String content, User user, String ack)
			throws Exception {

		DateTime date = twtconnect.getStatusTime(ack);
		TwitterPostTweet tweet = new TwitterPostTweet();
		tweet.setAcknowledgment(ack);
		tweet.setContent(content);
		tweet.setCurUser(user);
		tweet.setPostedTime(date);
		tweet.persist();
		return tweet;
	}

	public List<TwitterComment> fetchTweetsReply(TwitterPostTweet postTweet) {
		List<TwitterComment> lstTweetComment = TwitterComment.all()
				.filter("contentid=?", postTweet).fetch();
		return lstTweetComment;
	}
}
