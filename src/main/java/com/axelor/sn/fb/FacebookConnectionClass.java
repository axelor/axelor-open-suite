package com.axelor.sn.fb;

//Imports for SCRIBE to Authorize//
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
//ENDS HERE////

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.inject.servlet.SessionScoped;
import com.restfb.*;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.*;

/**
 * 
 * @author axelor-APAR
 * 
 */
@SessionScoped
public class FacebookConnectionClass {
	String userTokenTemp;
	@SuppressWarnings("rawtypes")
	HashMap mapReturnvalues;
	long ackLong = 0;
	String ack = "";
	String apiKey;
	String apiSecretKey;
	String userToken;
	String tokenUrl;
	FacebookClient facebookClient;
	AccessToken accessToken;

	String fbUserID;
	String fbPwd;
	@SuppressWarnings("rawtypes")
	ArrayList lstReturnBack;

	// ============CORE Methods============

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiSecretKey() {
		return apiSecretKey;
	}

	public void setApiSecretKey(String apiSecretKey) {
		this.apiSecretKey = apiSecretKey;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	Token EMPTY_TOKEN = null;
	OAuthService service;

	/**
	 * THIS METHOD IS USED TO OBTAIN UNIQUE AUTHORIZATION URL FOR AUTHENTICATION
	 * OF THE USER ON FACEBOOK
	 * 
	 * @param apiKey
	 * @param apiSecret
	 * @param redirectUrl
	 * @return
	 */
	public String getAuthorizationURL(String apiKey, String apiSecret,
			String redirectUrl) {
		// OLD URL Static One
		// "http://192.168.0.159:8080/axelor-demo/ws/snapps/100"
		service = new ServiceBuilder()
				.provider(FacebookApi.class)
				.apiKey(apiKey)
				.apiSecret(apiSecret)
				.callback(redirectUrl)
				.scope("manage_friendlists,manage_notifications,manage_pages,publish_stream,read_stream,read_friendlists,read_mailbox,create_event,read_requests,email,user_about_me,user_activities,user_birthday,user_education_history,user_groups,user_hometown,user_interests,user_likes,user_location,user_questions,user_relationships,user_relationship_details,user_religion_politics,user_subscriptions,user_website,user_work_history,user_events,user_games_activity,user_notes,user_photos,user_status,user_videos,friends_about_me,friends_activities,friends_birthday,friends_education_history,friends_groups,friends_hometown,friends_interests,friends_likes,friends_location,friends_questions,friends_relationships,friends_relationship_details,friends_religion_politics,friends_subscriptions,friends_website,friends_work_history,friends_events,friends_notes,friends_photos,friends_status,friends_videos")
				.build();
		// Obtain the Authorization URL
		String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		return authorizationUrl;
	}

	/**
	 * THIS METHOD IS USED TO RETRIVE ACCESSTOKEN FOR CURRENT SESSION USER
	 * 
	 * @param apiKey
	 * @param apiSecret
	 * @param code
	 *            - facebook unique code for current session user which will
	 *            used to retrive accesstoken and token secret
	 * @param redirectUrl
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap getAccessToken(String apiKey, String apiSecret, String code,
			String redirectUrl) {
		mapReturnvalues = new HashMap();
		service = new ServiceBuilder()
				.provider(FacebookApi.class)
				.apiKey(apiKey)
				.apiSecret(apiSecret)
				.callback(redirectUrl)
				.scope("manage_friendlists,manage_notifications,manage_pages,publish_stream,read_stream,read_friendlists,read_mailbox,create_event,read_requests,email,user_about_me,user_activities,user_birthday,user_education_history,user_groups,user_hometown,user_interests,user_likes,user_location,user_questions,user_relationships,user_relationship_details,user_religion_politics,user_subscriptions,user_website,user_work_history,user_events,user_games_activity,user_notes,user_photos,user_status,user_videos,friends_about_me,friends_activities,friends_birthday,friends_education_history,friends_groups,friends_hometown,friends_interests,friends_likes,friends_location,friends_questions,friends_relationships,friends_relationship_details,friends_religion_politics,friends_subscriptions,friends_website,friends_work_history,friends_events,friends_notes,friends_photos,friends_status,friends_videos")
				.build();
		Verifier verifier = new Verifier(code);
		service.getAuthorizationUrl(EMPTY_TOKEN);
		Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		System.out.println("Got the Access Token!");
		System.out.println("(if your curious it looks like this: "
				+ accessToken.getToken() + " )");
		if (!accessToken.getToken().isEmpty()) {

			facebookClient = new DefaultFacebookClient(accessToken.getToken());
			User user = facebookClient.fetchObject("me", User.class);
			mapReturnvalues.put("token", accessToken.getToken());
			mapReturnvalues.put("snUserName", user.getName());
		}
		// String token_name = accessToken.getToken() + ":" + user.getName();
		return mapReturnvalues;
	}

	public boolean isNullFBObj(FacebookClient client) {
		facebookClient = client;
		boolean status = true;
		if (facebookClient != null)
			status = false;
		return status;
	}

	// ===============Fetch Methods===================

	/**
	 * THIS METHOD USED TO SEARCH PERSON ON FACEBOOK
	 * 
	 * @param personName
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> fetchObjectOfPerson(String personName) {
		// facebookClient = new DefaultFacebookClient(userToken);
		ArrayList<HashMap> returnBack = new ArrayList<HashMap>();
		try {
			Connection<User> user = facebookClient.fetchConnection("search",
					User.class, Parameter.with("q", personName),
					Parameter.with("type", "user"));
			List<User> retriveValue = user.getData();
			for (int i = 0; i < retriveValue.size(); i++) {
				User detailValue = facebookClient.fetchObject(
						retriveValue.get(i).getId(), User.class);
				mapReturnvalues = new HashMap();
				mapReturnvalues.put("personId", detailValue.getId());
				mapReturnvalues.put("personFirstname",
						detailValue.getFirstName());
				mapReturnvalues
						.put("personLastname", detailValue.getLastName());
				mapReturnvalues.put("personGender", detailValue.getGender());
				mapReturnvalues.put("personProfileLink", detailValue.getLink());
				returnBack.add(mapReturnvalues);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapReturnvalues = new HashMap();
				mapReturnvalues.put("oauthError", -1);
				returnBack.add(mapReturnvalues);
			} else
				oe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnBack;
	}

	/**
	 * THIS METHOD IS USED TO IMPORT CONTACT LIST OF FACEBOOK
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getListOfFriends() { // String userToken) {
		lstReturnBack = new ArrayList<HashMap>();
		HashMap mapFriend;
		try {
			System.out.println("Import Contacts to ERP");
			Connection<User> friends = facebookClient.fetchConnection(
					"me/friends", User.class);
			List<User> retriveData = friends.getData();

			for (User user : retriveData) {
				mapFriend = new HashMap();
				mapFriend.put("facebookId", user.getId());
				
				
				mapFriend.put("facebookName", user.getName());
				mapFriend.put("facebookLink",
						"http://www.facebook.com/" + user.getId());
				lstReturnBack.add(mapFriend);
			}

			// ADDS User's Own Data to Friend List
			User link = facebookClient.fetchObject("me", User.class);
			mapFriend = new HashMap();
			mapFriend.put("facebookId", link.getId());
			mapFriend.put("facebookName", link.getName());
			mapFriend.put("facebookLink", link.getLink());

			lstReturnBack.add(mapFriend);

		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapFriend = new HashMap();
				mapFriend.put("oauthError", -1);
				lstReturnBack.add(mapFriend);
			} else
				oe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lstReturnBack;
	}

	/**
	 * THIS METHOD IS USED TO RETRIVE COMMENTS OF THE FACEBOOK STATUS
	 * 
	 * @param contentId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getComments(String contentId) {
		lstReturnBack = new ArrayList<HashMap>();
		HashMap mapComment;
		try {
			Connection<Comment> commentFeeds = facebookClient.fetchConnection(
					contentId + "/comments", Comment.class);
			List<Comment> comments = commentFeeds.getData();
			for (int i = 0; i < comments.size(); i++) {
				mapComment = new HashMap();
				mapComment.put("commentId", comments.get(i).getId());
				mapComment
						.put("commentFrom", comments.get(i).getFrom().getId());
				mapComment.put("commentContent", comments.get(i).getMessage());
				mapComment.put("commentTime", comments.get(i).getCreatedTime());
				mapComment.put("commentLike", comments.get(i).getLikeCount());
				lstReturnBack.add(mapComment);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapComment = new HashMap();
				mapComment.put("oauthError", -1);
				lstReturnBack.add(mapComment);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstReturnBack;
	}

	/**
	 * THIS METHOD USED TO RETRIVE INBOX OF FACEBOOK
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> retriveMessage() {// String userToken) {
		ArrayList<HashMap> listInboxMsg = new ArrayList<HashMap>();
		HashMap mapInbox;
		try {
			System.out.println("Retrving Your Messages");
			Connection<Post> myInbox = facebookClient.fetchConnection(
					"me/inbox", Post.class);
			List<Post> inboxMessages = myInbox.getData();

			for (int i = 0; i < inboxMessages.size(); i++) {
				Post post = facebookClient.fetchObject(inboxMessages.get(i)
						.getId(), Post.class);
				mapInbox = new HashMap();
				if (post.getMessage() == null)
					continue;

				mapInbox.put("messageId", inboxMessages.get(i).getId());
				mapInbox.put("messageFromID", inboxMessages.get(i).getTo().get(0)
						.getId());
				mapInbox.put("messageFrom", inboxMessages.get(i).getTo().get(0)
						.getName());
				mapInbox.put("messageContent", post.getMessage());
				mapInbox.put("sentTime", inboxMessages.get(i).getUpdatedTime()
						.toString());
				listInboxMsg.add(mapInbox);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapInbox = new HashMap();
				mapInbox.put("oauthError", -1);
				listInboxMsg.add(mapInbox);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listInboxMsg;
	}

	/**
	 * THIS METHOD IS USED TO RETRIVE NOTIFICATIONS OF THE FACEBOOK
	 * 
	 * @param paramLimit
	 * @param sinceValue
	 * @param untilValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getNotification(int paramLimit, Date sinceValue,
			Date untilValue) {

		HashMap mapNotification;
		ArrayList<HashMap> listNotifications = new ArrayList();
		Notification jsonNotification;
		try {
			if (paramLimit > 0 && sinceValue == null && untilValue == null) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("limit", paramLimit));
			} else if (sinceValue != null && paramLimit <= 0
					&& untilValue == null) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("since", sinceValue));
			} else if (untilValue != null && sinceValue == null
					&& paramLimit <= 0) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("until", untilValue));
			} else if (paramLimit > 0 && sinceValue != null
					&& untilValue == null) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("limit", paramLimit),
						Parameter.with("since", sinceValue));
			} else if (paramLimit > 0 && untilValue != null
					&& sinceValue == null) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("limit", paramLimit),
						Parameter.with("until", untilValue));
			} else if (untilValue != null && sinceValue != null
					&& paramLimit <= 0) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("until", untilValue),
						Parameter.with("since", sinceValue));
			} else if (paramLimit > 0 && untilValue != null
					&& sinceValue != null) {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class,
						Parameter.with("limit", paramLimit),
						Parameter.with("until", untilValue),
						Parameter.with("since", sinceValue));
			} else {
				jsonNotification = facebookClient.fetchObject(
						"me/notifications", Notification.class);
			}
			List<Data> valueData = jsonNotification.data;
			for (int i = 0; i < valueData.size(); i++) {
				mapNotification = new HashMap();
				mapNotification.put("notifId", valueData.get(i).id);
				mapNotification.put("notifTitle", valueData.get(i).title);
				mapNotification.put("notifLink", valueData.get(i).link);
				mapNotification
						.put("updateTime", valueData.get(i).updated_time);
				listNotifications.add(mapNotification);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapNotification = new HashMap();
				mapNotification.put("oauthError", -1);
				listNotifications.add(mapNotification);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listNotifications;
	}

	/**
	 * @see Json Parsing
	 * @author AXELOR-APAR
	 * @see Data and Summary
	 * 
	 */

	public static class Notification {

		@Facebook
		Summary summary;

		@Facebook
		List<Data> data;

	}

	public static class Data {
		@Facebook
		String id;

		@Facebook
		String title;

		@Facebook
		String link;

		@Facebook
		String updated_time;

	}

	public static class Summary {
		@Facebook
		Long unseen_count;

	}

	/**
	 * THIS METHOD IS USED TO RERTIVE PENDING FRIEND REQUEST OF FACEBOOK
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getFriendRequest() {

		ArrayList<HashMap> returnBack = new ArrayList<HashMap>();
		HashMap requesetdFriend;
		User user = null;
		try {
			List<FqlUser> users = facebookClient.executeFqlQuery(
					"SELECT uid_from FROM friend_request WHERE uid_to = me()",
					FqlUser.class);

			for (int i = 0; i < users.size(); i++) {
				user = facebookClient.fetchObject(users.get(i).uid_from,
						User.class);
				requesetdFriend = new HashMap();
				requesetdFriend.put("friendLink", user.getLink());
				requesetdFriend.put("friendName", user.getName());
				requesetdFriend.put("friendGender", user.getGender());
				returnBack.add(requesetdFriend);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				requesetdFriend = new HashMap();
				requesetdFriend.put("oauthError", -1);
				returnBack.add(requesetdFriend);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnBack;
	}

	/**
	 * JSON PARSING CLASS FOR FQL USER QUERY
	 * 
	 * @author AXELOR-APAR
	 * 
	 */

	public static class FqlUser {
		@Facebook
		String uid_from;

		@Override
		public String toString() {
			return String.format("%s ", uid_from);
		}
	}

	/**
	 * THIS METHOD IS USED TO RETRIVE NEWS FEED FOR CURRENT SESSION USER
	 * 
	 * @param paramLimit
	 * @param sinceValue
	 * @param untilValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getNewsFeed(int paramLimit, Date sinceValue,
			Date untilValue) {
		Connection<Post> connection;
		HashMap feedValue;
		lstReturnBack = new ArrayList<HashMap>();
		try {
			if (paramLimit > 0 && sinceValue == null && untilValue == null) {
				System.out.println("Limit Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("limit", paramLimit));
			} else if (sinceValue != null && paramLimit <= 0
					&& untilValue == null) {
				System.out.println("Since Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("since", sinceValue));
			} else if (untilValue != null && sinceValue == null
					&& paramLimit <= 0) {
				System.out.println("Untill Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("until", untilValue));
			} else if (paramLimit > 0 && sinceValue != null
					&& untilValue == null) {
				System.out.println("Limit and Since Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("limit", paramLimit),
						Parameter.with("since", sinceValue));
			} else if (paramLimit > 0 && untilValue != null
					&& sinceValue == null) {
				System.out.println("Untill and Limit Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("limit", paramLimit),
						Parameter.with("until", untilValue));
			} else if (untilValue != null && sinceValue != null
					&& paramLimit <= 0) {
				System.out.println("Since and Until Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("until", untilValue),
						Parameter.with("since", sinceValue));
			} else if (paramLimit > 0 && untilValue != null
					&& sinceValue != null) {
				System.out.println("All Parameter");
				connection = facebookClient.fetchConnection("me/home",
						Post.class, Parameter.with("limit", paramLimit),
						Parameter.with("until", untilValue),
						Parameter.with("since", sinceValue));
			} else {
				System.out.println("Without Parameter Called");
				connection = facebookClient.fetchConnection("me/home",
						Post.class);
			}

			List<Post> feedData = connection.getData();
			for (int i = 0; i < feedData.size(); i++) {
				feedValue = new HashMap();
				feedValue.put("feedId", feedData.get(i).getId());
				feedValue.put("feedUpdateFromID", feedData.get(i).getFrom().getId());
				feedValue.put("feedUpdateFrom", feedData.get(i).getFrom()
						.getName());
				feedValue.put("feedCreationTime", feedData.get(i)
						.getCreatedTime());
				feedValue.put("feedType", feedData.get(i).getType());
				feedValue.put("feedLink", feedData.get(i).getLink());
				feedValue.put("feedMessage", feedData.get(i).getMessage());
				lstReturnBack.add(feedValue);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				feedValue = new HashMap();
				feedValue.put("oauthError", -1);
				lstReturnBack.add(feedValue);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstReturnBack;
	}

	/**
	 * IT WILL RETURN PAGE DETAIL WHICH OWN BY CURRENT SESSION USER'S
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getPageDetail() {
		lstReturnBack = new ArrayList<HashMap>();
		HashMap mapPageValue;
		User user = facebookClient.fetchObject("me", User.class);
		String query = " SELECT page_id, name,page_url,username From page WHERE page_id IN (SELECT page_id FROM page_admin WHERE uid = '"
				+ user.getId() + "')";
		try {
			List<FqlPage> page = facebookClient.executeFqlQuery(query,
					FqlPage.class);
			for (int i = 0; i < page.size(); i++) {
				mapPageValue = new HashMap();
				mapPageValue.put("pageId", page.get(i).page_id);
				mapPageValue.put("pageName", page.get(i).name);
				mapPageValue.put("pageUrl", page.get(i).page_url);
				mapPageValue.put("pageUsername", page.get(i).username);
				lstReturnBack.add(mapPageValue);
			}
		}

		catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapPageValue = new HashMap();
				mapPageValue.put("oauthError", -1);
				lstReturnBack.add(mapPageValue);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstReturnBack;
	}

	/**
	 * JSON PARSING CLASS FOR FQL PAGE QUERY
	 * 
	 * @author APAR-AXELOR
	 * 
	 */

	public static class FqlPage {
		@Facebook
		String page_id;

		@Facebook
		String name;

		@Facebook
		String page_url;

		@Facebook
		String username;
	}

	/**
	 * IT WILL FETCH PAGE POST'S COMMENT
	 * 
	 * @param contentId
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getPageComments(String contentId) {
		lstReturnBack = new ArrayList<HashMap>();
		HashMap mapPageComment;
		try {
			Connection<Comment> commentFeeds = facebookClient.fetchConnection(
					contentId + "/comments", Comment.class);

			List<Comment> comments = commentFeeds.getData();
			for (int i = 0; i < comments.size(); i++) {
				mapPageComment = new HashMap();
				mapPageComment.put("commentId", comments.get(i).getId());
				mapPageComment.put("commentBy", comments.get(i).getFrom()
						.getName());
				mapPageComment.put("commentContent", comments.get(i)
						.getMessage());
				mapPageComment.put("commentTime", comments.get(i)
						.getCreatedTime());
				mapPageComment.put("commentLike", comments.get(i)
						.getLikeCount());
				lstReturnBack.add(mapPageComment);
			}
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190) {
				mapPageComment = new HashMap();
				mapPageComment.put("oauthError", -1);
				lstReturnBack.add(mapPageComment);
			} else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstReturnBack;
	}

	// ===============POST METHODS=============

	/**
	 * THIS METHOD IS USED TO POST MESSAGE TO FACEBOOK AS STATUS
	 * 
	 * @param msg
	 * @param paramPrivacy
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap publishMessage(String msg, String paramPrivacy) {
		mapReturnvalues = new HashMap();
		Privacy privacy = new Privacy();
		privacy.value = paramPrivacy;
		try {
			// facebookClient = new DefaultFacebookClient(userToken);
			FacebookType publishMessageResponse = facebookClient.publish(
					"me/feed", FacebookType.class,
					Parameter.with("message", msg),
					Parameter.with("privacy", privacy));
			mapReturnvalues.put("acknowledgment",
					publishMessageResponse.getId());

		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			ack = "Something Went Wrong\n" + e;
		}
		return mapReturnvalues;
	}

	public static class Privacy {
		@Facebook
		public String value;
	}

	/**
	 * THIS METHOD WILL ALLOW TO POST EVENT ONTO FB
	 * 
	 * @param startD
	 *            - START DATE
	 * @param endD
	 *            - END DATE
	 * @param ocession
	 *            - EVENT NAME
	 * @param location
	 *            - LOCATION
	 * @param paramPrivacy
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap publishEvent(Date startD, Date endD, String ocession,
			String location, String paramPrivacy) {
		mapReturnvalues = new HashMap();
		Privacy privacy = new Privacy();
		privacy.value = paramPrivacy;
		try {
			// facebookClient = new DefaultFacebookClient(userToken);
			FacebookType publishEventResponse = facebookClient.publish(
					"me/events", FacebookType.class,
					Parameter.with("name", ocession),
					Parameter.with("location", location),
					Parameter.with("Start_time", startD),
					Parameter.with("end_time", endD),
					Parameter.with("privacy", privacy.value));
			mapReturnvalues.put("acknowledgment", publishEventResponse.getId());
		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			ack = e.getMessage();
		}
		return mapReturnvalues;
	}

	/**
	 * METHOD USED TO POST CONTENT TO PARTICULAR PAGE WHOESE ID BEEN PASS HERE
	 * 
	 * @param postContent
	 * @param pageId
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap postToPgae(String postContent, String pageId) { // String
																	// userToken
		mapReturnvalues = new HashMap();
		// facebookClient = new DefaultFacebookClient(userToken);
		try {
			FacebookType publishToPage = facebookClient.publish(pageId
					+ "/feed", FacebookType.class,
					Parameter.with("message", postContent));
			mapReturnvalues.put("acknowledgment", publishToPage.getId());

		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mapReturnvalues;
	}

	/**
	 * THIS METHOD IS USED TO POST LIKE CONETENT ON FACEBOOK
	 * 
	 * @param contentId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap postLike(String contentId) {// , String userToken) {
		mapReturnvalues = new HashMap();
		Boolean value = null;
		facebookClient = new DefaultFacebookClient(userToken);
		try {
			Boolean publishToPage = facebookClient.publish(
					contentId + "/likes", Boolean.class);
			value = publishToPage.booleanValue();
			mapReturnvalues.put("status", value);
		}

		catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else
				oe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnvalues;
	}

	/**
	 * THIS METHOD IS USED TO POST COMMENT ON PASSED VALID FACEBBOK CONTENT ID
	 * 
	 * @param contentd
	 * @param commentContent
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap postStatusComemnt(String contentd, String commentContent) { // String
																				// userToken,
		mapReturnvalues = new HashMap();
		// facebookClient = new DefaultFacebookClient(userToken);
		try {
			FacebookType publishComment = facebookClient.publish(contentd
					+ "/comments", FacebookType.class,
					Parameter.with("message", commentContent));
			ack = publishComment.getId();
			if (!ack.isEmpty())
				mapReturnvalues.put("acknowledgment", ack);

		} catch (FacebookOAuthException oe) {
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else
				oe.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnvalues;
	}

	// ========DELETION METHODS======

	/**
	 * COMMON METHOD TO DELETE WHATEVER BEEN POSTED BY PASSING THEIR ID
	 * 
	 * @param contentId
	 * @param isUnlike
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap delete(String contentId, boolean isUnlike) {
		boolean value = false;
		mapReturnvalues = new HashMap();
		try {
			// facebookClient = new DefaultFacebookClient(userToken); , String
			// userToken
			if (isUnlike)
				value = facebookClient.deleteObject(contentId + "/likes");
			else
				value = facebookClient.deleteObject(contentId);

			mapReturnvalues.put("status", value);

		} catch (FacebookOAuthException oe) {
			System.out.println("Inside OauthException");
			if (oe.getErrorCode().intValue() == 190)
				mapReturnvalues.put("oauthError", -1);
			else if (oe.getErrorCode().intValue() == 100)
				mapReturnvalues.put("status", value);
			oe.printStackTrace();
		} catch (FacebookException fbex) {
			mapReturnvalues.put("status", value);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnvalues;
	}

	// =========ENDS OF ALL METHODS==========

}
