package com.axelor.sn.web

import java.util.EnumSet;
import com.axelor.apps.base.db.Partner;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import com.axelor.apps.base.db.LnStatusUpdates;
import com.axelor.apps.base.db.LnStatusComments;
import com.axelor.auth.db.User;
import com.axelor.apps.base.db.LnGroupDiscussion;
import com.axelor.apps.base.db.LnGroupDiscussionComments
import com.axelor.apps.base.db.LnGroup;

import com.axelor.apps.base.db.LnNetworkUpdates;
import com.axelor.apps.base.db.PersonalCredential;
import com.axelor.apps.base.db.SocialNetworking;
import com.axelor.apps.base.db.ApplicationCredentials;
import javax.persistence.EntityManager
import javax.persistence.EntityTransaction
import com.axelor.db.*;
import org.joda.time.DateTime;

import com.axelor.sn.service.LnService;
import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory
import com.google.code.linkedinapi.client.enumeration.NetworkUpdateType;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.schema.Connections
import com.google.code.linkedinapi.schema.Person
import com.google.common.cache.LocalCache.Values;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class LnController {
	
	@Inject
	LnService LinkedinService;
	
	/**
	 *This function is used to get  the Authorization URL
	 *Clicking this will go to a page where our application will be Authorized by the user 
	 */
	void getUrl(ActionRequest request, ActionResponse response) {

		def context = request.context as PersonalCredential
		User user = request.context.get("__user__")
		String authUrl = LinkedinService.getUrl(user)
		response.flash = "Click the link to get access <a href=" + authUrl + " target='_blank'>" + authUrl + "</a>"
	}

	void networkType(ActionRequest request, ActionResponse response) {
		SocialNetworking snType = SocialNetworking.all().filter("lower(name)= ?", "linkedin").fetchOne();
		if(snType != null)
			response.values = ["snType":snType]
		else
			throw new Exception("Network Type not Found...")
	}

	
	void fetchConnections(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		String acknowlegement = LinkedinService.fetchConnections(user)
		response.flash = acknowlegement
	}

	void sendMessage(ActionRequest  request, ActionResponse response) {
		User user = request.context.get("__user__")
		Partner contact = request.context.get("userid")
		String snUserId = contact.getSnUserId()
		String subject = request.context.get("subject")
		String message = request.context.get("msgcontent")
		String acknowlegement = LinkedinService.sendMessage(snUserId, subject, message, user)
		response.flash = acknowlegement
	}

	void updateStatus(ActionRequest request, ActionResponse response) {
		def context = request.context as LnStatusUpdates
		if(context.getId() == null) {
			User user = request.context.get("__user__")
			String content = request.context.get("updateContent").toString()
			HashMap updateKeyTime = LinkedinService.updateStatus(content, user)
			String updateId = updateKeyTime.get("updateId")
			DateTime date = new DateTime(updateKeyTime.get("updateTimeStamp"));
			response.values = ["updateId" : updateId, "updateTime" : date]
			response.flash = "Status Successfully Updated to LinkedIn..."
		}
	}

	void getComments(ActionRequest request, ActionResponse response) {
		String updateId = request.context.get("updateId")
		if(!updateId.equals(null)) {
			User user = request.context.get("__user__")
			LinkedinService.getComments(updateId, user)
		}
		else
			response.flash="Select A Status to Fetch Comments..."
	}

	void clearCommentfield(ActionRequest request, ActionResponse response) {
		response.values=["commentText":""]
	}

	//Function is used to refresh the comments in the view.
	void refreshComments(ActionRequest request, ActionResponse response) {
		def context = request.context as LnStatusUpdates
		context.getCommentList().clear()
		List<LnStatusComments> lstComment = LnStatusComments.all().filter("updateId=?", context).fetch();
		context.setCommentList(lstComment)
		response.values = context
	}

	void addStatusComment(ActionRequest request, ActionResponse response) {
		String updateId = request.context.get("updateId")
		String commentText = request.context.get("commentText")
		User user = request.context.get("__user__")
		LinkedinService.addStatusComment(user, updateId, commentText)
	}

	void getNetworkUpdates(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		String acknowledgement = LinkedinService.fetchNetworkUpdates(user)
		response.flash = acknowledgement
	}

	void getMembership(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		String acknowledgement = LinkedinService.getMembership(user)
		response.flash = acknowledgement
	}

	void getDiscussions(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		LnGroup group = request.context.get("__self__")
		LinkedinService.getDiscussions(user, group)
	}

	//Function is used to refresh the discussions in the view.
	void refreshDiscussions(ActionRequest request,ActionResponse response) {
		def context = request.context as LnGroup
		context.getDiscussionList().clear()
		List<LnGroupDiscussion> lstDiscussion = LnGroupDiscussion.all().filter("groupId=?", context).fetch();
		context.setDiscussionList(lstDiscussion)
		response.values = context
	}
	
   void postDiscussion(ActionRequest request, ActionResponse response) {
	   def context = request.context as LnGroupDiscussion
	   if(context.getId() == null) 	{
		   User user = request.context.get("__user__")
		   String title = request.context.get("discussionTitle")
		   String summary = request.context.get("discussionSummary")
		   LnGroup group = request.context.get("groupId")
		   String groupId = group.groupId
		   HashMap discussionIdTime = LinkedinService.addGroupDiscussion(title, summary, groupId, user)
		   String discussionId = discussionIdTime.get("discussionId").toString()
		   DateTime date = new DateTime(discussionIdTime.get("discussionTime"))
		   String discussionFrom = discussionIdTime.get("fromUser").toString()
		   response.values = ["discussionId" : discussionId, "discussionTime" : date, "discussionFrom" : discussionFrom]
		   response.flash = "Succesfully Posted to Group "+group.groupName.toUpperCase()+"..."
	   }
   }

	void getDiscussionComments(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		LnGroupDiscussion groupDiscussion = request.context.get("__self__")
		LinkedinService.getDiscussionComments(user, groupDiscussion)
	}

	void addDiscussionComment(ActionRequest request, ActionResponse response) {
		String commentText = request.context.get("commentText")
		User user = request.context.get("__user__")
		LnGroupDiscussion groupDiscussion = request.context.get("__self__")
		LinkedinService.addDiscussionComment(user, groupDiscussion, commentText)
	}

	//Function is used to refresh discussions comments in the view.
	void refreshDiscussionComments(ActionRequest request,ActionResponse response) {
		def context = request.context as LnGroupDiscussion
		context.getDiscussionCommentsList().clear()
		List<LnGroupDiscussionComments> lstGroupDiscussionComment = LnGroupDiscussionComments.all().filter("discussionId=?", context).fetch();
		context.setDiscussionCommentsList(lstGroupDiscussionComment)
		response.values = context
	}

	void deleteDiscussion(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		List lstIdValues = request.context.get("_ids")
		response.flash = LinkedinService.deleteDiscussion(lstIdValues, user)
	}
	
	void unAuthorizeApp(ActionRequest request, ActionResponse response) {
		User user = request.context.get("__user__")
		response.flash = LinkedinService.unAuthorize(user)
	}
}
