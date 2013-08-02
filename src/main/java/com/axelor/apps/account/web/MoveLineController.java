package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MoveLineController {

//	@Inject
//	private MoveLineService mls
//	
//	@Inject
//	private IrrecoverableService is
	
	@Inject
	private Injector injector;
	
	public void usherProcess(ActionRequest request, ActionResponse response) {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = MoveLine.find(moveLine.getId());
		
		MoveLineService mls = injector.getInstance(MoveLineService.class);
		
		try {
			mls.usherProcess(moveLine);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = MoveLine.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.passInIrrecoverable(moveLine, true, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = MoveLine.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.notPassInIrrecoverable(moveLine, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
