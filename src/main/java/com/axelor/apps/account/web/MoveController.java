package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.PeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MoveController {
	
	@Inject
	private Injector injector;
	
	public void validate(ActionRequest request, ActionResponse response) {

		Move move = request.getContext().asType(Move.class);
		move = Move.find(move.getId());
		
		MoveService ms = injector.getInstance(MoveService.class);
		
		try {
			ms.validate(move);
			response.setReload(true);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void getPeriod(ActionRequest request, ActionResponse response) {
		
		Move move = request.getContext().asType(Move.class);
	
		try {
			if(move.getDate() != null && move.getCompany() != null) {
				
				PeriodService ps = injector.getInstance(PeriodService.class);
				response.setValue("period", ps.rightPeriod(move.getDate(), move.getCompany()));				
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
}
