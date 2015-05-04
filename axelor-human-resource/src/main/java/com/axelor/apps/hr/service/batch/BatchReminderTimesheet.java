package com.axelor.apps.hr.service.batch;

import java.util.HashSet;
import java.util.List;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class BatchReminderTimesheet extends AbstractBatch{

	@Inject
	private TemplateMessageService templateMessageService;
	
	@Inject MessageServiceImpl messageServiceImpl;
	
	@Override 
	protected void process() {	
		if(batch.getMailBatch().getTemplate() != null)
			this.generateEmailTemplate();
		else
			this.generateEmail();
	}
	
	
	public void generateEmailTemplate(){
		
		Company company = batch.getMailBatch().getCompany(); 
		Template template = batch.getMailBatch().getTemplate();
		List<Timesheet> timesheetList = null;
		if(Beans.get(CompanyRepository.class).all().fetch().size() >1){
			timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user.activeCompany.id = ?1 AND self.statusSelect = 1", company.getId()).fetch();
		}else{
			timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.statusSelect = 1").fetch();
		}
		String model = template.getMetaModel().getFullName();
		String tag = template.getMetaModel().getName();
		for (Timesheet timesheet : timesheetList) {
			Message message = new Message();
			try{
				message = templateMessageService.generateMessage(timesheet.getId(), model, tag, template);
				message = messageServiceImpl.sendByEmail(message);
				incrementDone();
			}
			catch(Exception e){
				incrementAnomaly();
				TraceBackService.trace(new Exception(e),IException.REMINDER,batch.getId());
			}
		}
	}
	
	
	public void generateEmail(){
		Company company = batch.getMailBatch().getCompany(); 
		List<Timesheet> timesheetList = null;
		if(Beans.get(CompanyRepository.class).all().fetch().size() >1){
			timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.user.activeCompany.id = ?1 AND self.statusSelect = 1", company.getId()).fetch();
		}else{
			timesheetList = Beans.get(TimesheetRepository.class).all().filter("self.statusSelect = 1").fetch();
		}
		for (Timesheet timesheet : timesheetList) {
			Message message = new Message();
			try{
				message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
				message.setReplyToEmailAddressSet(new HashSet<EmailAddress>());
				message.setCcEmailAddressSet(new HashSet<EmailAddress>());
				message.setBccEmailAddressSet(new HashSet<EmailAddress>());
				message.addToEmailAddressSetItem(timesheet.getUser().getEmployee().getContactPartner().getEmailAddress());
				message.setSenderUser(AuthUtils.getUser());
				message.setSubject(batch.getMailBatch().getSubject());
				message.setContent(batch.getMailBatch().getContent());
				message.setMailAccount(Beans.get(MailAccountRepository.class).all().filter("self.isDefault = true").fetchOne());
				
				message = messageServiceImpl.sendByEmail(message);
				
				incrementDone();
			}
			catch(Exception e){
				incrementAnomaly();
				TraceBackService.trace(new Exception(e),IException.INVOICE_ORIGIN,batch.getId());
			}
		}
	}
	
	
	@Override
	protected void stop() {

		String comment = String.format("\t* %s Email(s) sent \n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}
}
