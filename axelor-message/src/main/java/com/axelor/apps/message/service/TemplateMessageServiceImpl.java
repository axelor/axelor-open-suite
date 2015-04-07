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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class TemplateMessageServiceImpl extends TemplateRepository implements TemplateMessageService {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateMessageServiceImpl.class); 

	protected TemplateMaker maker;
	
	private MessageService messageService;
	
	private EmailAddressRepository emailAddressRepo;

	@Inject
	public TemplateMessageServiceImpl( MessageService messageService, EmailAddressRepository emailAddressRepo ){
		this.messageService = messageService;
		this.emailAddressRepo = emailAddressRepo;
	}

	@Override
	public Message generateMessage(Model model, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, IOException  {

		return this.generateMessage( model.getId(), model.getClass().getCanonicalName(), model.getClass().getSimpleName(), template);
		
	}
	
	@Override
	public Message generateMessage( long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, IOException  {
		
		if ( !model.equals( template.getMetaModel().getFullName() ) ){
			throw new AxelorException( I18n.get(IExceptionMessage.TEMPLATE_SERVICE_3 ), IException.INCONSISTENCY, template.getMetaModel().getFullName() );
		}
		
		LOG.debug("model : {}", model);
		LOG.debug("tag : {}", tag);
		LOG.debug("object id : {}", objectId);
		LOG.debug("template : {}", template);
		
		this.initMaker(objectId, model, tag);
		
		String content = "";
		String subject = "";
		String from= "";
		String replyToRecipients = "";
		String toRecipients = "";
		String ccRecipients = "";
		String bccRecipients = "";
		String addressBlock= "";
		int mediaTypeSelect;
		
		if ( !Strings.isNullOrEmpty( template.getContent() ) )  {
			//Set template
			this.maker.setTemplate(template.getContent());
			//Make it
			content = this.maker.make();
		}
		
		
		if( !Strings.isNullOrEmpty( template.getAddressBlock() ) )  {
			this.maker.setTemplate(template.getAddressBlock());
			//Make it
			addressBlock = this.maker.make();
		}
		
		if ( !Strings.isNullOrEmpty( template.getSubject() ) )  {
			this.maker.setTemplate(template.getSubject());
			subject = this.maker.make();
			LOG.debug( "Subject :::", subject );
		}
		
		if( !Strings.isNullOrEmpty( template.getFromAdress() ) )  {
			this.maker.setTemplate(template.getFromAdress());
			from = this.maker.make();
			LOG.debug( "From :::", from );
		}
		
		if( !Strings.isNullOrEmpty( template.getReplyToRecipients() ) )  {
			this.maker.setTemplate(template.getReplyToRecipients());
			replyToRecipients = this.maker.make();
			LOG.debug( "Reply to :::", replyToRecipients );
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
		
		Message message = messageService.createMessage(
				model, 
				new Long(objectId).intValue(), 
				subject, 
				content, 
				this.getEmailAddress(from),
				this.getEmailAddresses(replyToRecipients),
				this.getEmailAddresses(toRecipients),
				this.getEmailAddresses(ccRecipients),
				this.getEmailAddresses(bccRecipients),
				getMetaFiles(template),
				addressBlock,
				mediaTypeSelect
				);
		
		return message;
		
	}
	
	public Set<MetaFile> getMetaFiles(Template template) throws AxelorException, IOException {
		
		List<MetaAttachment> metaAttachments = Beans.get( MetaAttachmentRepository.class ).all().filter( "self.objectId = ?1 AND self.objectName = ?2", template.getId(), Template.class.getName() ).fetch();
		
		Set<MetaFile> metaFiles = Sets.newHashSet();
		for ( MetaAttachment metaAttachment: metaAttachments ){ metaFiles.add( metaAttachment.getMetaFile() ); }
		
		return metaFiles;

	}
	
	
	@SuppressWarnings("unchecked")
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
