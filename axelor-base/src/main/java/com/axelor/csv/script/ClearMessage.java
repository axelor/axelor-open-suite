package com.axelor.csv.script;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ClearMessage {
	
	@Inject
	MailMessageRepository mailRepo;

	private static final Logger LOG = LoggerFactory.getLogger(ClearMessage.class); 
			
	//Delete all mail messages generated on import
	@Transactional
	private void deleteMailMessages(){
		
		for(MailMessage mailMessage : mailRepo.all().fetch()){
			try{
				mailRepo.remove(mailMessage);
			}catch(Exception e){
				LOG.debug("MailMessage: {}, Exception: {}",mailMessage.getId(),e.getMessage());
			}
		}
	}
	
	public Object clearAllMailMessages(Object bean, Map values) {
		
		deleteMailMessages();
		return bean;
		
	}

}
