package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.service.debtrecovery.ReminderActionService
import com.axelor.apps.account.service.debtrecovery.ReminderService
import com.axelor.apps.account.service.debtrecovery.ReminderSessionService
import com.axelor.apps.base.db.Mail
import com.axelor.apps.base.db.Partner
import com.axelor.apps.base.service.MailService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Provider

@Slf4j
class ReminderController {
	
	@Inject
	private Provider<ReminderService> rs

	@Inject
	private Provider<ReminderSessionService> rss
	
	@Inject
	private Provider<ReminderActionService> ras
	
	@Inject
	private Provider<MailService> ms
	
		
	public void ReminderGenerate(ActionRequest request, ActionResponse response) {
		
		log.debug("Begin ReminderGenerate controller...")
		
		try {
						
			Partner partner = request.context as Partner
			partner = Partner.find(partner.id)
			
			rs.get().reminder(partner)
			
			MailService mailService = ms.get();
			for(Mail mail : mailService.getMailList(partner))  {
				mailService.generatePdfMail(mail)
			}
			
			response.reload = true
			
			if(contractLine.getWaitReminderMatrixLine() != null)  {
				
				response.flash = "Niveau de relance en attente de validation"
				
			}
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
		log.debug("End ReminderGenerate controller")
	}
	

	public void onSave(ActionRequest request, ActionResponse response)  {
						
		Partner partner = request.context as Partner
			partner = Partner.find(partner.id)
		
		if(partner.getWaitReminderMatrixLine()!=null)  {
				
			ras.get().runManualAction(partner)
			
			MailService mailService = ms.get();
			for(Mail mail : mailService.getMailList(partner))  {
				mailService.generatePdfMail(mail)
			}
		}
		response.reload = true
	}
	
	
	public void onMoveActive(ActionRequest request, ActionResponse response)  {
						
		Partner partner = request.context as Partner
			partner = Partner.find(partner.id)
		
		if(partner.getContractReminderMatrixLine != null)  {
			
			ras.get().moveReminderMatrixLine(partner, partner.getContractReminderMatrixLine())
			
		}
	}
	
	public void onMoveCancelled(ActionRequest request, ActionResponse response)  {
						
		Partner partner = request.context as Partner
			partner = Partner.find(partner.id)
		
		ras.get().moveReminderMatrixLine(partner, partner.getCancelledReminderMatrixLine())
		
	}
	
	//TODO à passer en xml
	def reminderUpdateDomain (ActionRequest request, ActionResponse response)  {
		
		log.debug("Begin reminderUpdateDomain controller...")
		
		Partner partner = request.context as Partner
		
		if(partner?.reminderMethod)  {
			
			String domainQuery = "";
						
			String matrixId = contractLine?.reminderMethod?.actContReminderMatrix.id
			log.debug("partner?.reminderMethod?.actContReminderMatrix.id : {}", partner?.reminderMethod?.actContReminderMatrix.id)
			domainQuery = "self.reminderMatrix.id = "+matrixId
			
			response.attrs = [
				"waitReminderMatrixLine": ["domain":domainQuery]
			   ]
		}
		
		log.debug("End reminderUpdateDomain controller")
	}
	
}