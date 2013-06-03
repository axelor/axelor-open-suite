package com.axelor.sn.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;

import com.google.inject.servlet.SessionScoped;

import twitter4j.*;
import twitter4j.auth.*;

/**
 * 
 * @author axelor-APAR
 * 
 */
@SessionScoped
public class TwitterConnectionClass {
	// Main TwitterFactory Object by which all operations can be performed
	Twitter twitter;// = new TwitterFactory().getInstance();

	// To Storing AccessToken
	AccessToken accessToken;

	// For Geting Current User from its code Temporary File will be created
	// storing objects
	Subject subject = SecurityUtils.getSubject();
	com.axelor.auth.db.User currentUser = com.axelor.auth.db.User.all()
			.filter("self.code = ?1", subject.getPrincipal()).fetchOne();

	String userid, content, time, fav;

	@SuppressWarnings("rawtypes")
	HashMap mapReturnValue;

	// ========== CORE METHOD ===============

	/**
	 * THIS METHOD WILL CHECK WHETER INSTANCE IS NULL OR NOT AND IF IT ISN'T
	 * NULL THEN RETURN FALSE ELSE TRUE IN UTILITY CLASS
	 * 
	 * @param twitterPasse
	 * @param accessTokenPassed
	 * @return
	 */
	public boolean isNullTWTObj(Twitter twitterPasse,
			AccessToken accessTokenPassed) {
		twitter = twitterPasse;
		accessToken = accessTokenPassed;
		boolean status = true;
		if (twitter != null)
			if (accessToken != null)
				status = false;

		return status;
	}

	/**
	 * IT WILL GENERATE UNIQUE AUTHENTICATION URL WHICH WILL ALLOW USER TO
	 * CONNECT WITH TWITTER FROM ABS
	 * 
	 * @param apiKey
	 * @param apiSecret
	 * @param redirectUrl
	 * @return
	 */
	public String getAuthUrl4j(String apiKey, String apiSecret,
			String redirectUrl) {
		String acknowledgment = "";
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(apiKey, apiSecret);
		try {
			RequestToken requestToken = twitter
					.getOAuthRequestToken(redirectUrl);
			acknowledgment = requestToken.getAuthorizationURL();
			File file = new File(currentUser.getCode());
			ObjectOutputStream outStream = new ObjectOutputStream(
					new FileOutputStream(file));
			outStream.writeObject(twitter);
			outStream.writeObject(requestToken);
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return acknowledgment;
	}

	// ========= METHODS FOR RETRIVAL OF DATA =========

	/**
	 * THIS METHOD IS USED TO RETRIVE ALL FOLLOWERS
	 * 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> importContact() {
		ArrayList<HashMap> listContacts = new ArrayList<HashMap>();

		try {

			Paging paging = new Paging(1, 1);
			Collection<Status> statuses = twitter.getUserTimeline(paging);
			for (Status status : statuses) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("twitterId", status.getUser().getId());
				mapReturnValue.put("twitterName", status.getUser().getName());
				mapReturnValue.put("twitterLink", "http://twitter.com/"
						+ status.getUser().getScreenName());
				listContacts.add(mapReturnValue);
			}
			String twitterScreenName = twitter.getScreenName();
			PagableResponseList<User> followerIds = twitter.getFollowersList(
					twitterScreenName, -1);// IDs(, -1);
			for (User user : followerIds) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("twitterId", user.getId());
				mapReturnValue.put("twitterName", user.getName());
				mapReturnValue.put("twitterLink",
						"http://twitter.com/" + user.getScreenName());
				listContacts.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listContacts;
	}

	/**
	 * METHOD USED TO RETRIVE COMMENTS OF PASSED CONTENT
	 * 
	 * @param tweetId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getCommentsOfTweet(String tweetId) {
		ArrayList<HashMap> lstResult = new ArrayList<HashMap>();
		Long id = Long.parseLong(tweetId);
		try {
			RelatedResults results = twitter.getRelatedResults(id.longValue());
			List<Status> lstConversations = results.getTweetsWithConversation();

			for (int i = 0; i < lstConversations.size(); i++) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("userid", lstConversations.get(i).getUser()
						.getId()
						+ "");
				mapReturnValue
						.put("content", lstConversations.get(i).getText());
				mapReturnValue.put("time", lstConversations.get(i)
						.getCreatedAt().toString());
				mapReturnValue.put("fav", lstConversations.get(i).isFavorited()
						+ "");
				mapReturnValue
						.put("commentId", lstConversations.get(i).getId());
				lstResult.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstResult;
	}

	/**
	 * THIS METHOD USED TO RETRIVE WHOLE INBOX OF TWITTER
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getDirectMessages() {
		ArrayList<HashMap> lstResult = new ArrayList<HashMap>();
		try {
			ResponseList<twitter4j.DirectMessage> sender = twitter
					.getDirectMessages();
			for (twitter4j.DirectMessage dm : sender) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("msgId", dm.getId());
				mapReturnValue.put("msgContent", dm.getText());
				mapReturnValue.put("senderId", dm.getSender().getId());
				mapReturnValue.put("senderName", dm.getSender().getName());
				mapReturnValue.put("receiveDate", dm.getCreatedAt().toString());
				lstResult.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstResult;
	}

	/**
	 * METHOD IS USED TO RETRIVE USER's HOME TIMELINE
	 * 
	 * @param pageNo
	 * @param contentNo
	 * @param updateId
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> fetchHomeTimeline(int pageNo, int contentNo,
			Long updateId) {
		ArrayList<HashMap> lstTimeLineValues = new ArrayList<HashMap>();
		Paging page = null;
		ResponseList<Status> statuses;
		try {
			if (pageNo >= 0 && contentNo > 0 && updateId == null) {
				page = new Paging(pageNo, contentNo);
				statuses = twitter.getHomeTimeline(page);
			} else if (pageNo >= 0 && contentNo > 0 && updateId != null) {
				page = new Paging(pageNo, contentNo, updateId.longValue());
				statuses = twitter.getHomeTimeline(page);
			} else if (pageNo <= 0 && contentNo <= 0 && updateId != null) {
				page = new Paging(updateId.longValue());
				statuses = twitter.getHomeTimeline(page);
			} else if (pageNo > 0 && updateId != null) {
				page = new Paging(pageNo, updateId.longValue());
				statuses = twitter.getHomeTimeline(page);
			} else {
				statuses = twitter.getHomeTimeline();
			}

			for (Status status : statuses) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("updateId", status.getId() + "");
				mapReturnValue.put("screenName", "@"
						+ status.getUser().getScreenName());
				mapReturnValue.put("twitterName", status.getUser().getName());
				mapReturnValue.put("updateContent", status.getText());
				mapReturnValue.put("updateCreationTime", status.getCreatedAt());
				lstTimeLineValues.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstTimeLineValues;
	}

	/**
	 * IT WILL RETRIVE ALL INCOMING FOLLOWER'S REQUEST
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> retrivePendingRequest() {
		ArrayList lstReturnBack = new ArrayList();
		try {
			IDs ids = twitter.getIncomingFriendships(-1);
			long[] followerIds = ids.getIDs();
			for (int i = 0; i < followerIds.length; i++) {
				mapReturnValue = new HashMap();
				User user = twitter.showUser(followerIds[i]);
				mapReturnValue.put("twitterLink",
						"www.twitter.com/" + user.getScreenName());
				mapReturnValue.put("twitterId", user.getId());
				mapReturnValue.put("twitterScreenName", user.getScreenName());
				mapReturnValue.put("twitterName", user.getName());
				mapReturnValue.put("sendedOn", user.getCreatedAt().toString());
				lstReturnBack.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstReturnBack;
	}

	/**
	 * THIS METHOD USED TO ALLOW SEARCH ON PARTICULAR TWEET
	 * 
	 * @param searchKeyword
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> searchTweet(String searchKeyword) {
		ArrayList<HashMap> lstSearchTweet = new ArrayList<HashMap>();
		try {
			System.out.println("Executed");
			Query query = new Query();
			query.setQuery(searchKeyword);
			QueryResult result = twitter.search(query);
			List<Status> statuses = result.getTweets();
			for (int i = 0; i < statuses.size(); i++) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("userScreennName", "@"
						+ statuses.get(i).getUser().getScreenName());
				mapReturnValue.put("tweetText", statuses.get(i).getText());
				lstSearchTweet.add(mapReturnValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstSearchTweet;
	}

	/**
	 * THIS METHOD USED TO SEARCH A PARTICUAL PERSON ON TWITTER
	 * 
	 * @param searchKeyword
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> searchPerson(String searchKeyword) {
		ArrayList<HashMap> lstSearchValue = new ArrayList<HashMap>();
		try {
			ResponseList<User> searchedPerson = twitter.searchUsers(
					searchKeyword, 0);
			for (int i = 0; i < searchedPerson.size(); i++) {
				mapReturnValue = new HashMap();
				mapReturnValue.put("twitterUserId", searchedPerson.get(i)
						.getId());
				mapReturnValue.put("twitterUserName", searchedPerson.get(i)
						.getName());
				mapReturnValue.put("twitterProfileLink", "www.twitter.com/"
						+ searchedPerson.get(i).getScreenName());
				lstSearchValue.add(mapReturnValue);
			}
		} catch (TwitterException te) {
			te.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lstSearchValue;
	}

	public DateTime getStatusTime(String id) throws NumberFormatException,
			TwitterException // String apiKey,String apiSecret,String
								// userToken,String userTokenSecret,
	{
		Status s = twitter.showStatus(Long.parseLong(id));
		Date d = s.getCreatedAt();
		DateTime date = new DateTime(d);
		return date;
	}

	// =========== POST METHODS ============

	/**
	 * USED FOR POSTING TWEET
	 * 
	 * @param content
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap postTweet(String content) {
		mapReturnValue = new HashMap();
		try {
			Status status = twitter.updateStatus(content);
			mapReturnValue.put("acknowledgment", status.getId() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnValue;
	}

	/**
	 * IT WILL USED TO POST REPLAY ON PARTICULAR TWEET
	 * 
	 * @param cotnentId
	 * @param content
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap postTweetReplay(String cotnentId, String content) {
		mapReturnValue = new HashMap();
		try {
			Long id = Long.parseLong(cotnentId);
			StatusUpdate statusUpdate = new StatusUpdate(" " + content);
			statusUpdate.inReplyToStatusId(id.longValue());
			Status status = twitter.updateStatus(statusUpdate);
			mapReturnValue.put("acknowledgment", status.getId() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnValue;
	}

	/**
	 * METHOD USED TO SEND DIRECT MESSAGE TO FRIEND/FOLLOWER
	 * 
	 * @param toId
	 * @param msg
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap sendMessage(String toId, String msg) {
		mapReturnValue = new HashMap();
		Long id = Long.parseLong(toId);
		try {
			twitter4j.DirectMessage sender = twitter.sendDirectMessage(id, msg);
			mapReturnValue.put("acknowledgment", sender.getId() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnValue;
	}

	// =========== DELETION METHOD =============
	/**
	 * METHOD USED FOR DELETION OF PARTICULAR INBOX MESSAGE
	 * 
	 * @param msgId
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap deleteInbox(String msgId) {
		mapReturnValue = new HashMap();
		Long id = Long.parseLong(msgId);
		boolean valueStatus = false;
		try {
			DirectMessage msgDeleted = twitter.destroyDirectMessage(id);
			if (msgDeleted != null)
				mapReturnValue.put("acknowledgment", valueStatus);

		} catch (TwitterException te) {
			System.out.println(te.getErrorMessage());
			if (te.getErrorCode() == 34)
				mapReturnValue.put("acknowledgment", valueStatus);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnValue;
	}

	/**
	 * METHOD USED TO DELETE POSTED COMMENT
	 * 
	 * @param contentId
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap deleteContent(String contentId) {
		mapReturnValue = new HashMap();
		boolean valueStatus = true;
		Long id = Long.parseLong(contentId);
		try {
			Status status = twitter.destroyStatus(id);
			if (status != null)
				mapReturnValue.put("acknowledgment", valueStatus);

		} catch (TwitterException te) {
			if (te.getErrorCode() == 34)
				mapReturnValue.put("acknowledgment", valueStatus);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapReturnValue;
	}
}
