package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.CashRegister
import com.axelor.apps.account.service.CashRegisterService
import com.axelor.apps.base.db.Mail
import com.axelor.apps.base.service.MailService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class CashRegisterController {

	@Inject 
	private CashRegisterService crs
	
	@Inject
	private MailService ms;
	
	def void closeCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegister cashRegister = request.context as CashRegister
		cashRegister = CashRegister.find(cashRegister.id)
		
		try  {
			Mail mail = crs.closeCashRegister(cashRegister)
			ms.generatePdfMail(mail)
			
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }	
	}
	
	def void openCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegister cashRegister = request.context as CashRegister
		cashRegister = CashRegister.find(cashRegister.id)
		
		try  {
			crs.openCashRegister(cashRegister)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
}
