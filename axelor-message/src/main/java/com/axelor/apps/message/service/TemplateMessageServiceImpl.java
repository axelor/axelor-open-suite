/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.message.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TemplateMessageServiceImpl extends TemplateRepository implements TemplateMessageService {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateMessageServiceImpl.class); 

	@Inject
	private MessageService messageService;
	
	@Inject
	private MailAccountService mailAccountService;
	
	protected TemplateMaker maker;
	
	@Inject
	private EmailAddressRepository emailAddressRepo;
	
	
	
	public Message generateMessage(Object object, long objectId, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException  {

		return this.generateMessage(
				object, 
				objectId, 
				object.getClass().getCanonicalName(), 
				object.getClass().getSimpleName(), 
				template);
		
	}
	
	
	public Message generateMessage(Object object, long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException  {
		
		LOG.debug("model : "+model);
		LOG.debug("tag : "+tag);
		LOG.debug("object id : "+objectId);
		LOG.debug("object : "+object);
		LOG.debug("template : "+template);
		
		this.initMaker(objectId, model, tag);
		
		String content = "";
		String subject = "";
		String toRecipients = "";
		String ccRecipients = "";
		String bccRecipients = "";
		String addressBlock= "";
		String fromEmailAddress= "";
		int mediaTypeSelect;
		
		if(template.getContent() != null)  {
			//Set template
			this.maker.setTemplate(template.getContent());
			//Make it
			content = this.maker.make();
		}
		
		
		if(template.getAddressBlock() != null)  {
			this.maker.setTemplate(template.getAddressBlock());
			//Make it
			addressBlock = this.maker.make();
		}
		
		MailAccount mailAccount = mailAccountService.getDefaultMailAccount();
		content += "<p></p><p></p>" + mailAccountService.getSignature(mailAccount);
		
		if(mailAccount != null){
			mailAccount = mailAccountService.find(mailAccount.getId());
			LOG.debug( "Mail account :::", mailAccount );
		}
		
		if ( template.getSubject() != null)  {
			this.maker.setTemplate(template.getSubject());
			subject = this.maker.make();
			LOG.debug( "Subject :::", subject );
		}
		
		if(template.getFromEmailAddress() != null)  {
			this.maker.setTemplate(template.getFromEmailAddress());
			fromEmailAddress = this.maker.make();
			LOG.debug( "Reply to :::", fromEmailAddress );
		}
		
		if(template.getToRecipients() != null)  {
			this.maker.setTemplate(template.getToRecipients());
			toRecipients = this.maker.make();
			LOG.debug( "To :::", toRecipients );
		}
		
		if(template.getCcRecipients() != null)  {
			this.maker.setTemplate(template.getCcRecipients());
			ccRecipients = this.maker.make();
			LOG.debug( "CC :::", ccRecipients );
		}
		
		if(template.getBccRecipients() != null)  {
			this.maker.setTemplate(template.getBccRecipients());
			bccRecipients = this.maker.make();
			LOG.debug( "BCC :::", bccRecipients );
		}
		
		mediaTypeSelect=template.getMediaTypeSelect();
		LOG.debug( "Media :::", mediaTypeSelect );
		LOG.debug( "Content :::", content );
		
		String filePath = this.getFilePath(template);
		
		Message message = messageService.createMessage(
				model, 
				new Long(objectId).intValue(), 
				subject, 
				content, 
				this.getEmailAddress(fromEmailAddress),
				this.getEmailAddresses(toRecipients),
				this.getEmailAddresses(ccRecipients),
				this.getEmailAddresses(bccRecipients),
				mailAccount,
				filePath,
				addressBlock,
				mediaTypeSelect
				);
		
		return message;
		
	}
	
	protected String getFilePath(Template template)  throws AxelorException{

		String filePath = null;
		if(filePath == null)  {
			filePath = template.getFilePath();
		}
		
		return filePath;
		
	}
	
	
	public TemplateMaker initMaker(long objectId, String model, String tag) throws InstantiationException, IllegalAccessException, ClassNotFoundException  {
		//Init the maker
		this.maker = new TemplateMaker( Locale.FRENCH, '$', '$');
		
		Class<? extends Model> myClass = (Class<? extends Model>) Class.forName( model );

		//Set context
		this.maker.setContext(JPA.find(myClass.newInstance().getClass(), objectId), tag);
		
		return this.maker;
		
	}
	
	protected List<EmailAddress> getEmailAddresses(String recipients)  {
		
		List<EmailAddress> emailAddressList = Lists.newArrayList(); 
		
		if(recipients!=null && !recipients.isEmpty())  {
			String[] toTab = recipients.split(";");
			for(String s : toTab)  {
				emailAddressList.add(this.getEmailAddress(s));
			}
		}
		
		return emailAddressList;
	}
	
	
	protected EmailAddress getEmailAddress(String recipient)  {
		
		if(Strings.isNullOrEmpty(recipient))  {  return null;  }
		
		EmailAddress emailAddress = emailAddressRepo.findByAddress(recipient);
		
		if(emailAddress == null)  {
			Map<String, Object> values = new HashMap<String,Object>();
			values.put("address", recipient);
			emailAddress = emailAddressRepo.create(values);
		}
		
		return emailAddress;
	}
}
