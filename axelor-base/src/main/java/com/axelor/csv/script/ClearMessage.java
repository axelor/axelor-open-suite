package com.axelor.csv.script;

import java.util.Map;

import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ClearMessage {
	
	@Inject
	MailMessageRepository mailRepo;
	
	//Delete all mail messages generated on import
	@Transactional
	public Object clearAllMailMessages(Object bean, Map values) {
		
		for(MailMessage mailMessage : mailRepo.all().fetch()){
			mailRepo.remove(mailMessage);
		}
		
		return bean;
		
	}

}
