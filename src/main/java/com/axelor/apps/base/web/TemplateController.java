package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.service.template.TemplateService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TemplateController {
	
	@Inject
	private TemplateService ts;
	
	public void checkTargetReceptor(ActionRequest request, ActionResponse response){
		
		try {
			ts.checkTargetReceptor(request.getContext().asType(Template.class));
		}
		catch (Exception e) { TraceBackService.trace(response, e); }
	}

}
