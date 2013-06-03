package com.axelor.sn.web
import java.util.Formatter.DateTime;
import com.axelor.auth.db.User
import com.axelor.db.*
import javax.inject.Inject
import javax.persistence.*;
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime;
import org.joda.time.format.*;
import com.axelor.db.mapper.types.JodaAdapter.DateTimeAdapter;
import com.axelor.db.mapper.types.JodaAdapter.LocalDateAdapter;
import com.axelor.meta.views.*;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Request;
import com.axelor.apps.base.db.*;
import com.axelor.meta.db.*;
import com.axelor.sn.service.SNFBService
import com.axelor.sn.service.SNTWTService
import com.axelor.web.SNApp;
import com.fasterxml.jackson.databind.node.NodeCursor.Array;
import com.google.inject.matcher.Matchers.Returns;
import java.util.ArrayList;
import javax.inject.Inject;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Response;


public class FBController {
	@Inject
	SNFBService service;
	
	@Inject
	SNTWTService twtservice;

	String ack="";
	String apikey;
	String apisecret;
	String userName,password

	void getAuthUrlFB(ActionRequest request,ActionResponse response) {
		User user = request.context.get("__user__")
		SocialNetworking sn = SocialNetworking.all().filter("name = ?", "facebook").fetchOne();
		def context = request.context as PersonalCredential
		ack = service.obtainAuthUrl(user,sn);
		if(ack.equals("0"))
			response.flash="Sorry You can't Do anyting Admin Doesnt set Application Credentials";
		else if(ack.equals("1"))
			response.flash="You allready have one account Associated!!";
		else
			response.flash = "<a target=_blank href="+ack+">Click Here To Authorize The Application</a>";
	}

	void searchPerson(ActionRequest request,ActionResponse response) {
		String userToken;
		try {
			User user = request.context.get("__user__")
			def context = request.context as FBSearchPerson
			String param = context.searchparam
			ack=service.searchPerson(user,param)
			if(ack.equals("0"))
				response.flash = "Please Authorise The Applicaton First";

			else if (ack.equals("1"))
				response.flash = "You need to Re-Authorise The Application Please go to Personal Credential";

			else
				response.values = ["resultSearch":FBSearchResult.all().filter("searchPerson = ? and curUser = ?",FBSearchPerson.all().filter("searchparam = ?", param).fetchOne(),user).fetch()]
		}
		catch(Exception e) {
			e.printStackTrace()
			ack = e.getMessage()
			response.flash = ack;
		}
	}

	void saySNType(ActionRequest request, ActionResponse response) {
		try {
			SocialNetworking snType = SocialNetworking.all().filter("name = ?", "facebook").fetchOne()
			response.values = ["snType":snType]
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}



	void fbPostStatus(ActionRequest request,ActionResponse response) {
		def context = request.context as FBPostMessage
		if(context.getId() == null) {
			try {
				User user = request.context.get("__user__")
				String privacy = request.context.get("privacy")
				ack = service.postStatus(user,context.content,privacy)
				if(ack.equals("0"))
					throw new IllegalAccessError("Please Authorise The Applicaton First");

				else if(ack.equals("1"))
					throw new IllegalAccessError("You need to Re-Authorise The Application Please go to Personal Credential");

				else if(!ack.isEmpty())
					response.values = ["acknowledgment":ack];
				else
					response.flash = "There is Some issue with Posting Status";
			}
			catch(Exception e) {
				e.printStackTrace()
				ack = e.getMessage()
				response.flash = ack;
			}
		}
		else
			response.flash = "Record Already Saved"
	}


	public void postEvent(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			String privacy = request.context.get("privacy")
			def context = request.context as FBPostEvent
			org.joda.time.DateTime startDate = context.startdate
			Date startD = startDate.toDate()
			org.joda.time.DateTime endDate = context.enddate
			Date endD = endDate.toDate()
			ack = service.fbPostEvent(user, startD, endD, context.occession, context.location, privacy)
			if(ack.equals("0"))
				throw new IllegalAccessError("Please Authorise The Applicaton First");

			else if(ack.equals("1"))
				throw new IllegalAccessError("You need to Re-Authorise The Application Please go to Personal Credential");

			else if(!ack.isEmpty())
				response.values = ["acknowledgment":ack];
			else
				response.flash = "There is Some issue With Posting Event";
		}
		catch(Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace()
		}
	}


	/**
	 * Need To Remove Duplications from List
	 * @param request
	 * @param response
	 */
	void getAllContactsFB(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			ack = service.importContactsFB(user)
			response.flash = ack;
		}
		catch(Exception e) {
			response.flash = e.getMessage()
			e.printStackTrace()
		}
	}


	void getCommentsOfStatus(ActionRequest request,ActionResponse response) {
		String statusId;
		User user = request.context.get("__user__")
		String acknowledgment = request.context.get("acknowledgment");

		def context = request.context as FBPostMessage
		try {
			ArrayList lstRetrivedValues = service.getCommentsFB(user, context.getAcknowledgment());
			if(lstRetrivedValues.size  == 1)
				response.flash = lstRetrivedValues.get(0);

			else
				response.values = ["comments":FBComment.all().filter("curUser = ? and contentid = ?", user, context).fetch()]
		}
		catch (Exception e) {
			response.flash = e.getMessage()
			e.printStackTrace()
		}
	}

	void fetchInbox(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			ack = service.getInbox(user)
			ack= twtservice.getInbox(user);
			response.flash = ack;
		}
		catch(Exception e) {
			response.flash = e.getMessage()
			e.printStackTrace();
		}
	}

	void getNotificationsFromFB(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			ack = service.getNotifications(user);
			response.flash = ack;
		}
		catch(Exception e) {
			response.flash=e.getMessage();
			e.printStackTrace();
		}
	}


	void getFriendRequest(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			ack = service.getFriendRequest(user);
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}

	void retriveNewsFeed(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			SocialNetworking sn = request.context.get("snType")
			ack = service.getNewsFeeds(user);
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}

	void getPages(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			ack = service.getPageFB(user);
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}


	void postToPage(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			FBPages page = request.context.get("page")
			def context = request.context as FBPagePost;
			if(context.id == null) {
				ack = service.postPageContent(user,page,context);
				if(ack.equals("0"))
					throw new IllegalAccessError("Please Authorise The Applicaton First");

				else if(ack.equals("1"))
					throw new IllegalAccessError("You need to Re-Authorise The Application Please go to Personal Credential");

				else if(!ack.isEmpty())
					response.values = ["acknowledgment":ack];
				else
					response.flash = "There is Some issue With Posting Topic";
			}
			else
				response.flash="Record Already Saved"
		}
		catch (Exception e) {
			response.flash=e.getMessage();
			e.printStackTrace();
		}
	}

	void deletePagePost(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			List lstIds = request.context.get("_ids");
			if(lstIds.empty)
				ack = "Please Select Record(s) and Click on Delete"

			else
				ack = service.deletePagePost(user,lstIds);

			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}

	void getPagePostComment(ActionRequest request,ActionResponse response) {
		User user = request.context.get("__user__");
		String statusId;
		def context = request.context as FBPagePost
		try {
			ack = service.getPageCommentsFB(user, context.getAcknowledgment());
			if(ack.isEmpty())
				response.values=["postedComments":FBPageComment.all().filter("curUser = ? and contentid=?" , user, context).fetch()];
			else
				response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.toString()
			e.printStackTrace()
		}
	}

	void getDeleteMessage(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			List lstIds = request.context.get("_ids");
			if(lstIds.empty)
				ack = "Please Select Record(s) and Click on Delete"

			else
				ack = service.getDeleteMessage(user,lstIds);
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}


	void getDeleteEvent(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			List lstIds = request.context.get("_ids");
			if(lstIds.empty)
				ack = "Please Select Record(s) and Click on Delete"
			else
				ack = service.getDeleteEvent(user,lstIds);
			response.flash = ack;
		}
		catch (Exception e) {
			response.exception = e.getMessage();
			e.printStackTrace();
		}
	}

	void deleteInBox(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			List lstIds = request.context.get("_ids");
			if(lstIds.empty)
				ack = "Please Select Record(s) and Click on Delete"

			else
				ack = service.deleteInBox(user,lstIds);

			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}


	void getLikes(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			def context = request.context as FBNewsFeed
			if(context.contentLike)
				ack = service.getLike(user,context.id);
			/*		else
			 ack = service.destroyLikes(user, context.id)	*/
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash = e.getMessage();
			e.printStackTrace();
		}
	}

	void postComment(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			String postedComment = request.context.get("postComment");
			def context = request.context as FBPostMessage
			context.acknowledgment;
			ack = service.postCommmentonStatus(user,context.acknowledgment,postedComment)
			response.values = ["postComment":""]
			response.flash = ack;
		}
		catch (Exception e) {
			response.flash=e.getMessage();
			e.printStackTrace();
		}
	}

	void postPagePostComment(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__");
			String postedComment = request.context.get("postComment")
			def context = request.context as FBPagePost
			context.acknowledgment;
			ack = service.postCommmentonStatus(user,context.acknowledgment,postedComment)
			response.values = ["postComment":""]
			response.flash = ack;
		}
		catch (Exception e) {
			response.exception = e.getMessage();
			e.printStackTrace();
		}
	}

	void clearAllData(ActionRequest request,ActionResponse response) {
		try {
			User user = request.context.get("__user__")
			List lstIds = request.context.get("_ids")
			if(lstIds.empty)
				ack = "Please Select Record and Click on Remove"
			else {
				long idVal = lstIds.get(0);
				ack = service.removeAllDetail(user, idVal)
			}
			response.flash = ack;
		}
		catch (java.lang.IndexOutOfBoundsException index) {
			response.flash = index.getMessage();
			index.printStackTrace();
		}
		catch (Exception e) {
			response.flash=e.getMessage();
			e.printStackTrace();
		}
	}
	
	
	
	// SALE PRODUCT INTEGRATION
	
	
	void sayParent(ActionRequest request,ActionResponse response) {
		def context = request.context as Product
		String productCode=context.code;
		String productName=context.name;
		String createdProduct;
		ProductCategory category=context.productCategory;

		try	{
			if(category == null &&  productCode != null && productName != null)
				createdProduct="Recently New Product Have been Introduce as code:"+productCode+",Product Name:"+productName;

			else if(category != null && productCode != null && productName != null)
				createdProduct="Recently New Product Have been Introduce as code:"+productCode+",Product Name:"+productName+", Category:"+category.name

			else
				createdProduct="";


			if(context.fbPost==null)
				response.values=["postfb":createdProduct]
		}
		catch (Exception e) {
			e.printStackTrace()
		}
	}

	void postOnFB(ActionRequest request,ActionResponse response) {
		String acknowledgment;
		String content = request.context.get("postfb")
		User user=request.context.get("__user__")
		def context = request.context as Product
		if(context.fbPost != null) {
			println("Post Comment is called");
			acknowledgment=service.postCommmentonStatus(user, context.fbPost.acknowledgment, content);//For Posting Comments
			response.values=["postfb":""]
		}
		else {

			acknowledgment=service.postStatusFromOther(user,content,"ALL_FRIENDS")
			if(!acknowledgment.startsWith("[a-zA-Z]")) {
				FBPostMessage posted = FBPostMessage.all().filter("acknowledgment = ?",acknowledgment).fetchOne();
				
					response.values=["fbPost":posted,"postfb":""];
					acknowledgment = "";
				
				

			}
			else
			acknowledgment = "Problem Occure during merging content";
		}
		response.flash=acknowledgment;
	}

	void getComments(ActionRequest request,ActionResponse response) {
		def context = request.context as Product
		ArrayList comments=new ArrayList();
		String acknowledgment;
		int totalValue=0;

		try {
			if(context.fbPost!=null) {
				List keys=request.context.keySet().asList()
				List values=request.context.values().asList()
				User user=values.get(keys.indexOf("__user__"))
				comments = service.getCommentsFB(user, context.fbPost.acknowledgment);
				if(comments.size()>1) {
					response.values=["txtUser":comments.each {it}]
					totalValue=(comments.size()/3);
					acknowledgment=totalValue+" Comment(s) is There"
					response.flash=acknowledgment;
				}
				else
					response.flash=comments.get(0);
			}
			else
				response.flash="There is no Posted Status"
		}
		catch (Exception e) {
			e.printStackTrace()
		}
	}
	void testMethod(ActionRequest request,ActionResponse response) {
		println(request.context.toString())
	}
}
