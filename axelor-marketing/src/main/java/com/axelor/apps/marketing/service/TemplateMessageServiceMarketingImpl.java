package com.axelor.apps.marketing.service;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;

public class TemplateMessageServiceMarketingImpl extends TemplateMessageServiceBaseImpl {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected MailAccount emailAccount;
	
	@Inject
	public TemplateMessageServiceMarketingImpl(MessageService messageService) {
		super(messageService);
	}
	
	@Override
	protected Integer getMediaTypeSelect(Template template) {
		
		if (template.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_EMAILING) {
			return MessageRepository.MEDIA_TYPE_EMAIL;
		}
		
		return super.getMediaTypeSelect(template);
	}
	
	protected MailAccount getMailAccount()  {
		
		if ( emailAccount != null ) {
			log.debug( "Email account ::: {}", emailAccount );
			return emailAccount;
		}
		
		return super.getMailAccount();
	}

	public void setEmailAccount(MailAccount emailAccount)  {
		
		this.emailAccount = emailAccount;
		
	}
}
