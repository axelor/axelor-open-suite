package com.axelor.apps.base.service.batch;

import com.axelor.i18n.I18n;

public class BatchReminderMail extends BatchStrategy{
	
	
	protected int mailDone = 0;
	protected int mailAnomaly = 0;
	
	
	
	@Override
	protected void process() {	
		if(batch.getMailBatch().getTemplate() != null)
			this.generateEmailTemplate();
		else
			this.generateEmail();
	}
	
	public void generateEmailTemplate(){
		
	}
	
	public void generateEmail(){
			
	}
	
	@Override
	protected void stop() {

		String comment = String.format("\t* %s Emails sent \n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());
		
		comment += String.format("\t* %s email(s) traité(s)\n", mailDone);
		comment += String.format("\t* %s anomalie(s)", mailAnomaly);

		super.stop();
		addComment(comment);
		
	}
	
}
