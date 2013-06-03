package com.axelor.sn.linkedin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.CommentField;
import com.google.code.linkedinapi.client.enumeration.GroupMembershipField;
import com.google.code.linkedinapi.client.enumeration.NetworkUpdateType;
import com.google.code.linkedinapi.client.enumeration.PostField;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Comments;
import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.GroupMembership;
import com.google.code.linkedinapi.schema.GroupMemberships;
import com.google.code.linkedinapi.schema.Network;
import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.Post;
import com.google.code.linkedinapi.schema.Posts;
import com.google.code.linkedinapi.schema.Update;
import com.google.code.linkedinapi.schema.UpdateComment;
import com.google.code.linkedinapi.schema.UpdateComments;
import com.google.code.linkedinapi.schema.Updates;
import com.google.code.linkedinapi.schema.Comment;

public class LinkedinConnectionClass {

	static LinkedInOAuthService oauthService = null;
	LinkedInRequestToken requestToken = null;
	static LinkedInApiClientFactory factory = null;
	LinkedInApiClient client = null;
	final Set<ProfileField> setProfileFields = EnumSet.of(ProfileField.ID,
			ProfileField.FIRST_NAME, ProfileField.LAST_NAME,
			ProfileField.PUBLIC_PROFILE_URL,ProfileField.PICTURE_URL);
	final Set<NetworkUpdateType> networkUpdateType = EnumSet
			.of(NetworkUpdateType.SHARED_ITEM);
	final Set<GroupMembershipField> groupFields = EnumSet.of(
			GroupMembershipField.GROUP_ID,
			GroupMembershipField.MEMBERSHIP_STATE,
			GroupMembershipField.GROUP_NAME);
	final Set<PostField> postField = EnumSet.of(PostField.ID,
			PostField.SUMMARY, PostField.TITLE, PostField.CREATION_TIMESTAMP,
			PostField.CREATOR_FIRST_NAME, PostField.CREATOR_LAST_NAME);
	final Set<CommentField> commentField = EnumSet.of(CommentField.ID,
			CommentField.CREATOR, CommentField.CREATION_TIMESTAMP,
			CommentField.TEXT);

	public String getUrl(String consumerKey, String consumerSecret,
			String redirectUrl) throws IOException {
		String authUrl = "";
		oauthService = LinkedInOAuthServiceFactory.getInstance()
				.createLinkedInOAuthService(consumerKey, consumerSecret);
		requestToken = oauthService.getOAuthRequestToken(redirectUrl);
		factory = LinkedInApiClientFactory.newInstance(consumerKey,
				consumerSecret);
		authUrl = requestToken.getAuthorizationUrl();
		File temp = new File("/tmp/" + requestToken.getToken() + ".txt");
		ObjectOutputStream outStream = new ObjectOutputStream(
				new FileOutputStream(temp));
		outStream.writeObject(requestToken);
		outStream.close();
		return authUrl;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap getUserToken(String verifier, String tokenCode)
			throws IOException, ClassNotFoundException {
		File temp = new File("/tmp/" + tokenCode + ".txt");
		ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(
				temp));
		requestToken = (LinkedInRequestToken) inStream.readObject();
		temp.delete();
		inStream.close();
		LinkedInAccessToken accessToken = oauthService.getOAuthAccessToken(requestToken, verifier);
		HashMap userDetails = new HashMap();

		client = factory.createLinkedInApiClient(accessToken);
		Person profile = client.getProfileForCurrentUser(setProfileFields);

		userDetails.put("accessToken", accessToken.getToken());
		userDetails.put("accessTokenSecret", accessToken.getTokenSecret());
		userDetails.put("userName",
				profile.getFirstName() + " " + profile.getLastName());

		return userDetails;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getUserConnections(String userToken,
			String userTokenSecret) throws IOException {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		Person profile = client.getProfileForCurrentUser(setProfileFields);
		Connections connections = client.getConnectionsForCurrentUser(setProfileFields);

		HashMap userDetails = new HashMap();
		userDetails.put("userId", profile.getId());
		userDetails.put("userName",
				profile.getFirstName() + " " + profile.getLastName());
		userDetails.put("userLink", profile.getPublicProfileUrl());

		ArrayList<HashMap> userConnections = new ArrayList<HashMap>();
		userConnections.add(userDetails);

		for (Person person : connections.getPersonList()) {
			userDetails = new HashMap<String, String>();
			userDetails.put("userId", person.getId());
			userDetails.put("userName", person.getFirstName() + " " + person.getLastName());
			userDetails.put("userLink", person.getPublicProfileUrl());
			System.out.println(userDetails);
			userConnections.add(userDetails);
		}
		return userConnections;
	}

	public void sendMessage(String userToken, String userTokenSecret,
			ArrayList<String> lstUserId, String subject, String message) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		client.sendMessage(lstUserId, subject, message);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap updateStatus(String userToken, String userTokenSecret,
			String updateContent) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		client.updateCurrentStatus(updateContent);

		Updates update = client.getUserUpdates(networkUpdateType, 0, 1)
				.getUpdates();
		Iterator<Update> updatesIterator = update.getUpdateList().iterator();
		Update updateData = updatesIterator.next();
		HashMap updateKeyTime = new HashMap();
		
		updateKeyTime.put("updateId", updateData.getUpdateKey());
		updateKeyTime.put("updateTimeStamp", updateData.getTimestamp());
		return updateKeyTime;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getComments(String userToken,
			String userTokenSecret, String contentId) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);

		UpdateComments updateComments = client.getNetworkUpdateComments(contentId);
		Iterator<UpdateComment> commentIterator = updateComments.getUpdateCommentList().iterator();
		UpdateComment commentData = null;
		ArrayList<HashMap> commentList = new ArrayList<HashMap>();
		while (commentIterator.hasNext()) {
			commentData = commentIterator.next();
			HashMap comment = new HashMap();
			comment.put("commentId", commentData.getId());
			comment.put("commentText", commentData.getComment());
			comment.put("commentTime", commentData.getTimestamp());
			comment.put("fromSnUserId", commentData.getPerson().getId());
			commentList.add(comment);
		}
		return commentList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap addStatusComment(String userToken, String userTokenSecret,
			String contentId, String commentText) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		client.postComment(contentId, commentText);
		UpdateComments updateComments = client.getNetworkUpdateComments(contentId);
		Iterator<UpdateComment> commentsIterator = updateComments.getUpdateCommentList().iterator();
		UpdateComment comment = null;
		HashMap commentData = new HashMap();
		while (commentsIterator.hasNext()) {
			comment = commentsIterator.next();
			if (!commentsIterator.hasNext()) {
				commentData = new HashMap();
				commentData.put("commentId", comment.getId());
				commentData.put("commentText", comment.getComment());
				commentData.put("commentTime", comment.getTimestamp());
				commentData.put("fromSnUserId", comment.getPerson().getId());
			}
		}
		return commentData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> fetchNetworkUpdates(String userToken,
			String userTokenSecret, int count, Date startDate, Date endDate) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		Network networkUpdates;
		if (count != 0 && startDate == null)
			networkUpdates = client.getNetworkUpdates(networkUpdateType, 0, count);
		else if (count == 0 && startDate != null)
			networkUpdates = client.getNetworkUpdates(networkUpdateType, startDate,
					endDate);
		else if (count != 0 && startDate != null)
			networkUpdates = client.getNetworkUpdates(networkUpdateType, 0, count,
					startDate, endDate);
		else
			networkUpdates = client.getNetworkUpdates(networkUpdateType, 0, 15);

		Iterator<Update> networkUpdatesIterator = networkUpdates.getUpdates().getUpdateList().iterator();
		Update updateData = null;
		HashMap networkUpdate = new HashMap();
		ArrayList networkUpdatesList = new ArrayList();
		while (networkUpdatesIterator.hasNext()) {
			updateData = networkUpdatesIterator.next();
			if (updateData.getUpdateContent().getPerson().getCurrentShare().getComment() == null)
				continue;
			networkUpdate = new HashMap();
			networkUpdate.put("networkUpdateId", updateData.getUpdateKey());
			networkUpdate.put("networkUpdateContent", updateData.getUpdateContent().getPerson().getCurrentShare().getComment());
			networkUpdate.put("networkUpdateTimeStamp", updateData.getTimestamp());
			networkUpdate.put("fromUser", updateData.getUpdateContent().getPerson().getId());
			networkUpdatesList.add(networkUpdate);
		}
		return networkUpdatesList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getMemberships(String userToken, String userTokenSecret) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		GroupMemberships memberships = client.getGroupMemberships(groupFields);
		ArrayList groupList = new ArrayList();
		HashMap membership = new HashMap();
		for (GroupMembership member : memberships.getGroupMembershipList()) {
			membership = new HashMap();
			membership.put("groupId", member.getGroup().getId());
			membership.put("groupName", member.getGroup().getName());
			membership.put("membershipState", member.getMembershipState().getCode().toString());
			groupList.add(membership);
		}
		return groupList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<HashMap> getDiscussions(String userToken,
			String userTokenSecret, String groupId, int count, Date modifiedDate) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		Posts groupPost = null;
		if (count != 0 && modifiedDate == null)
			groupPost = client.getPostsByGroup(groupId, postField, 0, count);
		else if (count != 0 && modifiedDate != null)
			groupPost = client.getPostsByGroup(groupId, postField, 0, count, modifiedDate);
		else
			groupPost = client.getPostsByGroup(groupId, postField, 0, 15);

		ArrayList<HashMap> groupDiscussionList = new ArrayList<HashMap>();
		HashMap groupDiscussionData;
		for (Post post : groupPost.getPostList()) {
			groupDiscussionData = new HashMap();
			groupDiscussionData.put("discussionId", post.getId());
			if (post.getTitle() != null)
				groupDiscussionData.put("discussionTitle", post.getTitle());
			else
				groupDiscussionData.put("discussionTitle", "");

			if (post.getSummary() != null)
				groupDiscussionData.put("discussionSummary", post.getSummary());
			else
				groupDiscussionData.put("discussionSummary", "");
			groupDiscussionData.put("fromUser", post.getCreator().getFirstName() + " "
					+ post.getCreator().getLastName());
			groupDiscussionData.put("discussionTime", post.getCreationTimestamp());

			groupDiscussionList.add(groupDiscussionData);
		}
		return groupDiscussionList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap addGroupDiscussion(String userToken, String userTokenSecret,
			String groupId, String title, String summary) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		client.createPost(groupId, title, summary);

		Posts post = client.getPostsByGroup(groupId, postField, 0, 1);
		HashMap discussionIdTime = new HashMap();
		discussionIdTime.put("discussionId", post.getPostList().get(0).getId());
		discussionIdTime.put("discussionTime", post.getPostList().get(0)
				.getCreationTimestamp());
		discussionIdTime.put("fromUser", post.getPostList().get(0).getCreator().getFirstName()
				+ " " + post.getPostList().get(0).getCreator().getLastName());

		return discussionIdTime;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<HashMap> getDiscussionComments(String userToken,
			String userTokenSecret, String discussionId) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		Comments cmnt = client.getPostComments(discussionId, commentField);
		Comments comments = client.getPostComments(discussionId, commentField,
				0, Integer.parseInt(cmnt.getTotal().toString()));
		ArrayList<HashMap> commentList = new ArrayList<HashMap>();
		HashMap commentData;
		for (int i = 0; i < comments.getTotal(); i++) {
			Comment comment = comments.getCommentList().get(i);
			commentData = new HashMap();
			commentData.put("commentId", comment.getId());
			commentData.put("commentText", comment.getText());
			commentData.put("commentTime", comment.getCreationTimestamp());
			commentData.put("commentFrom", comment.getCreator().getFirstName() + " "
					+ comment.getCreator().getLastName());
			commentList.add(commentData);
		}
		return commentList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap addDiscussionComment(String userToken,
			String userTokenSecret, String discussionId, String commentText) {
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		client.addPostComment(discussionId, commentText);
		Comments cmnt = client.getPostComments(discussionId, commentField);
		Comments comments = client.getPostComments(discussionId, commentField, Integer.parseInt(cmnt.getTotal().toString()) - 1, 1);
		HashMap commentData = new HashMap();
		Comment comment = comments.getCommentList().get(0);
		commentData.put("commentId", comment.getId());
		commentData.put("commentText", comment.getText());
		commentData.put("commentTime", comment.getCreationTimestamp());
		commentData.put("commentFrom", comment.getCreator().getFirstName() + " "
				+ comment.getCreator().getLastName());
		return commentData;
	}

	public boolean deleteDiscussion(String userToken, String userTokenSecret,
			String discussionId) {
		boolean status = false;
		client = factory.createLinkedInApiClient(userToken, userTokenSecret);
		try {
			client.deletePost(discussionId);
			status = true;
		} catch (Exception e) {

		}
		return status;
	}
}
