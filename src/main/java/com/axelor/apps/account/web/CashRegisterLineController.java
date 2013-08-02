package com.axelor.apps.account.web;

import com.axelor.apps.account.db.CashRegisterLine;
import com.axelor.apps.account.service.CashRegisterLineService;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.service.MailService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CashRegisterLineController {

	@Inject 
	private CashRegisterLineService crs;
	
	@Inject
	private MailService ms;
	
	public void closeCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegisterLine cashRegisterLine = request.getContext().asType(CashRegisterLine.class);
		cashRegisterLine = CashRegisterLine.find(cashRegisterLine.getId());
		
		try  {
			Mail mail = crs.closeCashRegister(cashRegisterLine);
			ms.generatePdfMail(mail);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }	
	}
	
	public void openCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegisterLine cashRegisterLine = request.getContext().asType(CashRegisterLine.class);
		cashRegisterLine = CashRegisterLine.find(cashRegisterLine.getId());
		
		try  {
			crs.openCashRegister(cashRegisterLine);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
