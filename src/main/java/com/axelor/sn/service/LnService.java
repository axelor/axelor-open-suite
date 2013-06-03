package com.axelor.sn.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.apps.base.db.LnStatusComments;
import com.axelor.apps.base.db.LnDirectMessages;
import com.axelor.apps.base.db.LnNetworkUpdates;
import com.axelor.apps.base.db.LnStatusUpdates;
import com.axelor.apps.base.db.ApplicationCredentials;
import com.axelor.apps.base.db.LnGroupDiscussion;
import com.axelor.apps.base.db.LnGroupDiscussionComments;
import com.axelor.apps.base.db.LnGroup;

import com.axelor.apps.base.db.LinkedinParameters;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PersonalCredential;
import com.axelor.apps.base.db.SocialNetworking;
import com.axelor.sn.linkedin.LinkedinConnectionClass;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import org.joda.time.DateTime;

public class LnService {
	
	@Inject
	LinkedinConnectionClass LinkedinConnect;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	EntityManager em = JPA.em();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap getUserLogin(User user) throws Exception {
		HashMap credentials = new HashMap();
		SocialNetworking snType = SocialNetworking.all().filter("lower(name)= ?", "linkedin").fetchOne();
		PersonalCredential personalCredential = PersonalCredential.all().filter("userId=? and snType=?", user, snType).fetchOne();
		if (personalCredential == null)
			throw new Exception("You are not authorized to use this application. Please go to Personal Credential under Linkedin to authorize this application.");
		credentials.put("snType", snType);
		credentials.put("personalCredential", personalCredential);
		return credentials;
	}

	public String getUrl(User user) throws Exception {
		String authUrl = "";
		SocialNetworking snType = SocialNetworking.all().filter("lower(name)= ?", "linkedin").fetchOne();
		if (snType == null)
			throw new Exception("Network Type Not Found");
		else {
			PersonalCredential personalCredential = PersonalCredential.all().filter("userId=? and snType=?", user, snType).fetchOne();
			if (personalCredential == null) {
				try {
					ApplicationCredentials applicationCredentials = ApplicationCredentials.all().filter("snType=?", snType).fetchOne();
					if(applicationCredentials == null) {
						throw new Exception("Application not set. Contact your Admin to set Application.");
					}
					String redirectUrl = applicationCredentials.getRedirectUrl() + "/" + user.getId();
					authUrl = LinkedinConnect.getUrl(applicationCredentials.getApikey(),
							applicationCredentials.getApisecret(), redirectUrl);
				} catch (NullPointerException e) {
					throw new Exception(e.toString());
				}
			} else {
				throw new Exception("You Already have One Account Associated...");
			}
			return authUrl;
		}
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	public boolean getUserToken( String verifier, User user, String tokenCode) throws Exception {
		boolean status = false;
		
		HashMap userDetails = LinkedinConnect.getUserToken(verifier, tokenCode);
		String token = userDetails.get("accessToken").toString();
		String tokenSecret = userDetails.get("accessTokenSecret").toString();
		String name = userDetails.get("userName").toString();
		try {
			PersonalCredential personalCredential = new PersonalCredential();
			personalCredential.setUserToken(token);
			personalCredential.setUserTokenSecret(tokenSecret);
			personalCredential.setSnUserName(name);
			personalCredential.setUserId(user);
			SocialNetworking snType = SocialNetworking.all().filter("lower(name)= ?", "linkedin").fetchOne();
			personalCredential.setSnType(snType);
			personalCredential.merge();
			status = true;
		} catch (Exception e) {
			throw new Exception("There's Some Problem. Not Authorised.");
		}
		return status;
	}

	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public String fetchConnections(User user) throws Exception {
		HashMap credential = getUserLogin(user);
		SocialNetworking snType = (SocialNetworking) credential.get("snType");
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		ArrayList<HashMap> userConnections = LinkedinConnect.getUserConnections(userToken, userTokenSecret);
		List<String> lstUserId = (List<String>) em.createQuery("SELECT contact.snUserId FROM Partner contact " +
				"WHERE contact.curUser=" + user.getId() + " AND contact.snType="+ snType.getId()).getResultList();
		HashMap<String, String> userDetails = new HashMap<String, String>();
		for (int i = 0; i < userConnections.size(); i++) {
			userDetails = (HashMap<String, String>) userConnections.get(i);
			if (!lstUserId.contains(userDetails.get("userId").toString())) {
				ImportContact contact = new ImportContact();
				contact.setSnUserId(userDetails.get("userId"));
				contact.setName(userDetails.get("userName"));
				contact.setSnType(snType);
				contact.setCurUser(user);
				contact.setLink(userDetails.get("userLink"));
				contact.persist();
			}
		}
		return "Contacts Imported Successfully.";
	}
	*/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional
	public String importContactsLINERP(User user)
			throws Exception {
		HashMap credential = getUserLogin(user);
		SocialNetworking snType = (SocialNetworking) credential.get("snType");
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		ArrayList<HashMap> userConnections = LinkedinConnect.getUserConnections(userToken, userTokenSecret);
		List<String> lstUserId = (List<String>) em.createQuery("SELECT contact.snUserId FROM Partner contact " +
				"WHERE contact.curUser=" + user.getId() + " AND contact.snType="+ snType.getId()).getResultList();
		HashMap userDetails = new HashMap();
		for (int i = 0; i < userConnections.size(); i++) {
			userDetails = userConnections.get(i);
			if (!lstUserId.contains(userDetails.get("userId").toString())) {
				Partner contact = new Partner();
				contact.setSnUserId(userDetails.get("userId").toString());
				contact.setName(userDetails.get("userName").toString());
				contact.setSnType(snType);
				contact.setCurUser(user);
				contact.setIsContact(true);
				if (userDetails.get("userLink") != null)
					contact.setWebSite(userDetails.get("userLink").toString());
				contact.persist();
			}
		}
		return "Contacts Imported Successfully.";
	}

	@SuppressWarnings( "rawtypes")
	public String sendMessage(String snUserId, String subject, String message, User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		ArrayList<String> lstSnUserId = new ArrayList<String>();
		lstSnUserId.add(snUserId);
		LinkedinConnect.sendMessage(userToken, userTokenSecret, lstSnUserId, subject, message);
		return "Message Sent Successfully.";
	}

	@SuppressWarnings("rawtypes")
	public HashMap updateStatus(String updateContent, User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		HashMap updateKeyTime = LinkedinConnect.updateStatus(userToken, userTokenSecret, updateContent);
		return updateKeyTime;
	}

	@SuppressWarnings({  "unchecked", "rawtypes" })
	@Transactional
	public void getComments(String updateId, User user) throws Exception {
		HashMap credential = getUserLogin(user);
		SocialNetworking snType = (SocialNetworking) credential.get("snType");
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		ArrayList<HashMap> commentList = LinkedinConnect.getComments(userToken, userTokenSecret, updateId);

		List<String> lstSnUserIds = (List<String>) em.createQuery("SELECT contact.snUserId FROM Partner contact " +
				"WHERE contact.curUser=" + user.getId() + " AND contact.snType="+ snType.getId()).getResultList();

		LnStatusUpdates statusUpdate = LnStatusUpdates.all().filter("updateId=?", updateId).fetchOne();

		List<String> lstCommentIds = em.createQuery("SELECT commentList.commentId FROM LnStatusComments commentList " +
				"WHERE commentList.curUser=" + user.getId() + " AND commentList.updateId=" + statusUpdate.getId()).getResultList();

		HashMap commentData = new HashMap();
		for(int i = 0; i < commentList.size(); i++) {
			commentData = commentList.get(i);
			if(lstSnUserIds.contains(commentData.get("fromSnUserId").toString())) {
				if(!lstCommentIds.contains(commentData.get("commentId").toString())) {
					LnStatusComments comment = new LnStatusComments();
					Partner contact = Partner.all().filter("snUserId=? and curUser=?",
							commentData.get("fromSnUserId").toString(), user).fetchOne();
					DateTime date = new DateTime(commentData.get("commentTime"));
					comment.setUpdateId(statusUpdate);
					comment.setCommentId(commentData.get("commentId").toString());
					comment.setCommentText(commentData.get("commentText").toString());
					comment.setCommentTime(date);
					comment.setCurUser(user);
					comment.setFromUser(contact);
					comment.persist();
				}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Transactional
	public void addStatusComment(User user, String updateId, String commentText) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		HashMap commentData = LinkedinConnect.addStatusComment(userToken, userTokenSecret, updateId, commentText);
		LnStatusUpdates statusUpdate = LnStatusUpdates.all().filter("updateId=?", updateId).fetchOne();
		Partner contact = Partner.all().filter("snUserId=? and curUser=?", commentData.get("fromSnUserId").toString(), user).fetchOne();

		LnStatusComments comment = new LnStatusComments();
		DateTime date = new DateTime(commentData.get("commentTime"));
		comment.setUpdateId(statusUpdate);
		comment.setCommentId(commentData.get("commentId").toString());
		comment.setCommentText(commentData.get("commentText").toString());
		comment.setCommentTime(date);
		comment.setCurUser(user);
		comment.setFromUser(contact);
		comment.persist();
	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Transactional
	public String fetchNetworkUpdates(User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		SocialNetworking snType = (SocialNetworking) credential.get("snType");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		LinkedinParameters parameters = LinkedinParameters.all().filter("curUser=?", user).fetchOne();
		int count = 0;
		Date startDate = null, endDate = null;
		if (parameters != null) {
			 if ((parameters.getDays() != 0) && (parameters.getRecordNumbers() == 0)) {
					endDate = new Date();
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DATE, -(parameters.getDays()));
					startDate = new Date(sdf.format(c.getTime()));
				}
				else if ((parameters.getDays() == 0) && (parameters.getRecordNumbers() != 0)) {
					startDate = null;
					endDate = null;
					count = parameters.getRecordNumbers();
				}
				else if ((parameters.getDays() != 0) && (parameters.getRecordNumbers() != 0)) {
					endDate = new Date();
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DATE, -(parameters.getDays()));
					startDate = new Date(sdf.format(c.getTime()));
					count = parameters.getRecordNumbers();
				}
		}
		else {
			count = 0;
			startDate = null;
			endDate = null;
		}
		ArrayList<HashMap> networkUpdatesList = LinkedinConnect.fetchNetworkUpdates(userToken, userTokenSecret, count, startDate, endDate);

		List<String> lstSnUserIds = (List<String>) em.createQuery("SELECT contact.snUserId FROM Partner contact " +
				"WHERE contact.curUser=" + user.getId() + " AND contact.snType="+ snType.getId()).getResultList();

		List<String> lstNetworkUpdateIds = (List<String>) em.createQuery("SELECT networkUpdate.networkUpdateId FROM LnNetworkUpdates networkUpdate"
				+ " WHERE networkUpdate.curUser=" + user.getId()).getResultList();

		HashMap networkUpdateData = new HashMap();
		for(int i = 0; i < networkUpdatesList.size(); i++) {
			networkUpdateData = networkUpdatesList.get(i);
			if (lstSnUserIds.contains(networkUpdateData.get("fromUser").toString())) {
				if (!lstNetworkUpdateIds.contains(networkUpdateData.get("networkUpdateId").toString())) {
					LnNetworkUpdates networkUpdate = new LnNetworkUpdates();
					Partner fromUser = Partner.all().filter("snUserId=? and curUser=?",networkUpdateData.get("fromUser").toString(), user).fetchOne();
					DateTime date = new DateTime(networkUpdateData.get("networkUpdateTimeStamp"));
					networkUpdate.setNetworkUpdateId(networkUpdateData.get("networkUpdateId").toString());
					networkUpdate.setNetworkUpdateContent(networkUpdateData.get("networkUpdateContent").toString());
					networkUpdate.setCurUser(user);
					networkUpdate.setNetworkUpdateTime(date);
					networkUpdate.setFromUser(fromUser);
					networkUpdate.persist();
				}
			}
		}
		return "Network Updates Fetched...";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional
	public String getMembership(User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		ArrayList<HashMap> groupMemberships = LinkedinConnect.getMemberships(userToken, userTokenSecret);
		List<String> lstGroupIds = (List<String>) em.createQuery("SELECT group.groupId FROM LnGroup group WHERE group.curUser=" + user.getId()).getResultList();
		HashMap membership = new HashMap();
		for(int i = 0; i < groupMemberships.size(); i++) {
			membership = groupMemberships.get(i);
			if (!lstGroupIds.contains(membership.get("groupId").toString())) {
				LnGroup group = new LnGroup();
				group.setGroupId(membership.get("groupId").toString());
				group.setGroupName(membership.get("groupName").toString());
				group.setMembershipState(membership.get("membershipState").toString());
				group.setCurUser(user);

				group.persist();
			}
		}
		return "Group Memberships Obtained...";
	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Transactional
	public void getDiscussions(User user, LnGroup group) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		String groupId = group.getGroupId();
		LinkedinParameters parameters = LinkedinParameters.all().filter("curUser=?", user).fetchOne();
		int count = 0;
		Date modifiedDate = null;
		if (parameters != null) {
			if ((parameters.getRecordNumbers() == 0) && (parameters.getDays() != 0)) { 
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, -(parameters.getDays()));
				modifiedDate = new Date(sdf.format(c.getTime()));
			}
			else if ((parameters.getRecordNumbers() != 0) && (parameters.getDays() == 0)) {
				count = parameters.getRecordNumbers();
			}
			else if ((parameters.getRecordNumbers() != 0) && (parameters.getDays() != 0)) {
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DATE, -(parameters.getDays()));
				modifiedDate = new Date(sdf.format(c.getTime()));
				count = parameters.getRecordNumbers();
			}
		}
		else {
			count = 0;
			modifiedDate = null;
		}
		ArrayList<HashMap> groupDiscussionList = LinkedinConnect.getDiscussions(userToken, userTokenSecret, groupId, count, modifiedDate);
		List<String> lstGroupDiscussionIds = em.createQuery("SELECT discussion.discussionId FROM LnGroupDiscussion discussion " +
				"WHERE discussion.curUser=" + user.getId() + " AND discussion.groupId=" + group.getId()).getResultList();

		HashMap groupDiscussionData = new HashMap();
		for(int i = 0; i < groupDiscussionList.size(); i++) {
			groupDiscussionData = groupDiscussionList.get(i);
			if(!lstGroupDiscussionIds.contains(groupDiscussionData.get("discussionId").toString())) {
				DateTime date = new DateTime(groupDiscussionData.get("discussionTime"));
				LnGroupDiscussion groupDiscussion = new LnGroupDiscussion();
				groupDiscussion.setDiscussionId(groupDiscussionData.get("discussionId").toString());
				groupDiscussion.setDiscussionSummary(groupDiscussionData.get("discussionSummary").toString());
				groupDiscussion.setDiscussionTitle(groupDiscussionData.get("discussionTitle").toString());
				groupDiscussion.setDiscussionFrom(groupDiscussionData.get("fromUser").toString());
				groupDiscussion.setDiscussionTime(date);
				groupDiscussion.setGroupId(group);
				groupDiscussion.setIsByUser(false);
				groupDiscussion.setCurUser(user);
				groupDiscussion.persist();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public HashMap addGroupDiscussion( String title, String summary, String groupId, User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		HashMap discussionIdTime = LinkedinConnect.addGroupDiscussion(userToken, userTokenSecret, groupId, title, summary);
		return discussionIdTime;
	}

	@SuppressWarnings("rawtypes")
	@Transactional
	public void addDiscussionComment( User user, LnGroupDiscussion groupDiscussion, String commentText) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		String discussionId = groupDiscussion.getDiscussionId();
		HashMap commentData = LinkedinConnect.addDiscussionComment(userToken, userTokenSecret, discussionId, commentText);
		LnGroupDiscussionComments groupDiscussionComment = new LnGroupDiscussionComments();
		DateTime date = new DateTime(commentData.get("commentTime"));
		groupDiscussionComment.setCommentId(commentData.get("commentId").toString());
		groupDiscussionComment.setCommentText(commentData.get("commentText").toString());
		groupDiscussionComment.setCommentTime(date);
		groupDiscussionComment.setFromUser(commentData.get("commentFrom").toString());
		groupDiscussionComment.setCurUser(user);
		groupDiscussionComment.setDiscussionId(groupDiscussion);
		groupDiscussionComment.persist();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public void getDiscussionComments( User user, LnGroupDiscussion groupDiscussion) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		String discussionId = groupDiscussion.getDiscussionId();

		ArrayList<HashMap> commentList = LinkedinConnect.getDiscussionComments(userToken, userTokenSecret, discussionId);
		List<String> lstCommentIds = em.createQuery("SELECT discussionComments.commentId FROM LnGroupDiscussionComments discussionComments " +
				"WHERE discussionComments.curUser=" + user.getId() + " AND discussionComments.discussionId=" + groupDiscussion.getId()).getResultList();
		HashMap commentData = new HashMap();
		for(int i = 0; i < commentList.size(); i++) {
			commentData = commentList.get(i);
			if (!lstCommentIds.contains(commentData.get("commentId").toString())) {
				LnGroupDiscussionComments groupDiscussionComment = new LnGroupDiscussionComments();
				DateTime date = new DateTime(commentData.get("commentTime"));
				groupDiscussionComment.setCommentId(commentData.get("commentId").toString());
				groupDiscussionComment.setCommentText(commentData.get("commentText").toString());
				groupDiscussionComment.setCommentTime(date);
				groupDiscussionComment.setFromUser(commentData.get("commentFrom").toString());
				groupDiscussionComment.setCurUser(user);
				groupDiscussionComment.setDiscussionId(groupDiscussion);
				groupDiscussionComment.persist();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Transactional
	public String deleteDiscussion(List<Integer> lstIdValues, User user) throws Exception {
		HashMap credential = getUserLogin(user);
		PersonalCredential personalCredential = (PersonalCredential) credential.get("personalCredential");
		String userToken = personalCredential.getUserToken();
		String userTokenSecret = personalCredential.getUserTokenSecret();
		for (int i = 0; i < lstIdValues.size(); i++) {
			LnGroupDiscussion discussion = LnGroupDiscussion.find(lstIdValues.get(i).longValue());
			boolean status = LinkedinConnect.deleteDiscussion(userToken, userTokenSecret,discussion.getDiscussionId());
			if(status) {
				em.remove(discussion);
			}
		}
		return "Group Discussions Deleted Successfully ";
	}
	
	@Transactional
	public String unAuthorize(User user) throws Exception {
		SocialNetworking snType = SocialNetworking.all().filter("lower(name)= ?", "linkedin").fetchOne();
		if (snType == null)
			throw new Exception("Network Type Not Found");
		else {
			PersonalCredential personalCredential = PersonalCredential.all().filter("userId=? and snType=?", user, snType).fetchOne();
			if (personalCredential == null) {
				throw new Exception("You Have not Authorized the Application...");
			}
			else {
				LinkedinParameters.all().filter("curUser=?", user).remove();
				LnGroup.all().filter("curUser=?", user).remove();
				LnNetworkUpdates.all().filter("curUser=?", user).remove();
				LnStatusUpdates.all().filter("curUser=?", user).remove();
				LnDirectMessages.all().filter("curUser=?", user).remove();
				Partner.all().filter("curUser=? and snType=?", user, snType).remove();
				em.remove(personalCredential);
			}
			return "Successfully UnAuthorized...";
		}
	}
}
