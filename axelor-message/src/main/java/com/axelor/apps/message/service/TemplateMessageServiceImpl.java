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

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TemplateMessageServiceImpl implements TemplateMessageService {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateMessageServiceImpl.class); 

	@Inject
	private MessageService messageService;
	
	@Inject
	private MailAccountService mailAccountService;
	
	protected TemplateMaker maker;
	
	@Inject
	private EmailAddressRepository emailAddressRepo;
	
	
	
	public Message generateMessage(Object object, long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException  {
		
		LOG.debug("model : "+model);
		LOG.debug("tag : "+tag);
		LOG.debug("object id : "+objectId);
		LOG.debug("object : "+object);
		
		this.initMaker(objectId, model, tag);
		
		String content = "";
		String subject = "";
		String toRecipients = "";
		String ccRecipients = "";
		String bccRecipients = "";
		String addressBlock= "";
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
		
		if(template.getSubject() != null)  {
			this.maker.setTemplate(template.getSubject());
			subject = this.maker.make();
		}
		
		if(template.getToRecipients() != null)  {
			this.maker.setTemplate(template.getToRecipients());
			toRecipients = this.maker.make();
		}
		
		if(template.getCcRecipients() != null)  {
			this.maker.setTemplate(template.getCcRecipients());
			ccRecipients = this.maker.make();
		}
		
		if(template.getBccRecipients() != null)  {
			this.maker.setTemplate(template.getBccRecipients());
			bccRecipients = this.maker.make();
		}
		
		mediaTypeSelect=template.getMediaTypeSelect();
		
		String filePath = this.getFilePath(template);
		
		JPA.clear();
		Message message = messageService.createMessage(
				model, 
				new Long(objectId).intValue(), 
				subject, 
				content, 
				this.getEmailAddress(toRecipients),
				this.getEmailAddress(ccRecipients),
				this.getEmailAddress(bccRecipients),
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
		this.maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		Class<? extends Model> myClass = (Class<? extends Model>) Class.forName( model );

		//Set context
		this.maker.setContext(JPA.find(myClass.newInstance().getClass(), objectId), tag);
		
		return this.maker;
		
	}
	
	public List<EmailAddress> getEmailAddress(String recipients)  {
		
		List<EmailAddress> emailAddressList = Lists.newArrayList(); 
		
		if(recipients!=null && !recipients.isEmpty())  {
			String[] toTab = recipients.split(";");
			for(String s : toTab)  {
				EmailAddress emailAddress = emailAddressRepo.findByAddress(s);
				if(emailAddress != null)  {
					emailAddressList.add(emailAddress);
				}
			}
		}
		
		return emailAddressList;
	}
	
	
	
}
