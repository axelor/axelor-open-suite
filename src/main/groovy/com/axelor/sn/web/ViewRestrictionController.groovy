package com.axelor.sn.web

import com.axelor.auth.db.Group
import com.axelor.db.*;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.views.*;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.apps.base.db.SocialNetworking
import com.axelor.sn.service.SNMetaService
import com.google.inject.Inject;

class ViewRestrictionController
{
	@Inject
	SNMetaService service;

	void getSelectedValues(ActionRequest request,ActionResponse response)
	{
		MetaMenu menuObj=request.context.get("menus");
		Set<Group> grpObj=request.context.get("groups");
		try
		{
			response.flash=service.setRestrictions(menuObj, grpObj);
		}
		catch (Exception e) 
		{
			response.flash=e.getMessage();
			e.printStackTrace();
		}
	}
	
	void lowerSnName(ActionRequest request,ActionResponse response)
	{
		def context=request.context as SocialNetworking
		response.values=["name":context.name.toLowerCase()]
	}
}
