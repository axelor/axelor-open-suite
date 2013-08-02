package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Year;
import com.axelor.apps.account.service.YearService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class YearController {

	@Inject
	private YearService ys;
	
	public void close(ActionRequest request, ActionResponse response) {
		
		Year year = request.getContext().asType(Year.class);
		
		try  {
			ys.closeYear(year);
			response.setReload(true);			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }	
	}
}
