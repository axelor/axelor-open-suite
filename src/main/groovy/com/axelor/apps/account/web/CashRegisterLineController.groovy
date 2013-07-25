package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.CashRegisterLine
import com.axelor.apps.account.service.CashRegisterLineService
import com.axelor.apps.base.db.Mail
import com.axelor.apps.base.service.MailService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class CashRegisterLineController {

	@Inject 
	private CashRegisterLineService crs
	
	@Inject
	private MailService ms;
	
	def void closeCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegisterLine cashRegisterLine = request.context as CashRegisterLine
		cashRegisterLine = CashRegisterLine.find(cashRegisterLine.id)
		
		try  {
			Mail mail = crs.closeCashRegister(cashRegisterLine)
			ms.generatePdfMail(mail)
			
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }	
	}
	
	def void openCashRegister(ActionRequest request, ActionResponse response)  {
		
		CashRegisterLine cashRegisterLine = request.context as CashRegisterLine
		cashRegisterLine = CashRegisterLine.find(cashRegisterLine.id)
		
		try  {
			crs.openCashRegister(cashRegisterLine)
			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
}
