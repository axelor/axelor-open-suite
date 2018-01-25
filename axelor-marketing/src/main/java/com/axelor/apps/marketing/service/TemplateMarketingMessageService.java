package com.axelor.apps.marketing.service;

import javax.inject.Inject;

import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;

public class TemplateMarketingMessageService extends TemplateMessageServiceBaseImpl {

	@Inject
	public TemplateMarketingMessageService(MessageService messageService, EmailAddressRepository emailAddressRepo) {
		super(messageService, emailAddressRepo);
	}
	
	@Override
	public Integer getMediaTypeSelect(Template template) {
		
		if (template.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_EMAILING) {
			return MessageRepository.MEDIA_TYPE_EMAIL;
		}
		
		return super.getMediaTypeSelect(template);
	}

}
