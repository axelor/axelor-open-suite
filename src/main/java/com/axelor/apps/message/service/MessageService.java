/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Transport;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.IMessage;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.mail.MailSender;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MessageService {

	private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);
	
	private DateTime todayTime;
	
	@Inject
	private UserInfoService userInfoService;
	
	@Inject
	private TemplateMessageService templateMessageService;

	@Inject
	public MessageService(UserInfoService userInfoService) {

		this.todayTime = GeneralService.getTodayDateTime();
	}
	
	@Transactional
	public Message createMessage(Event event, MailAccount mailAccount)  {
		
		UserInfo recipientUserInfo = event.getUserInfo();
		
		List<EmailAddress> toEmailAddressList = Lists.newArrayList();
		
		if(recipientUserInfo != null)  {
			Partner partner = recipientUserInfo.getPartner();
			if(partner != null)  {
				EmailAddress emailAddress = partner.getEmailAddress();
				if(emailAddress != null)  {
					toEmailAddressList.add(emailAddress);
				}
			}
		}
		
		return this.createMessage(
				event.getDescription(), 
				null, 
				recipientUserInfo, 
				IMessage.RELATED_TO_EVENT, 
				event.getId().intValue(), 
				event.getRelatedToSelect(), 
				event.getRelatedToSelectId(), 
				todayTime.toLocalDateTime(), 
				event.getResponsibleUserInfo(), 
				null, false, 
				IMessage.STATUS_SENT, 
				"Remind : "+event.getSubject(), 
				IMessage.TYPE_RECEIVED,
				toEmailAddressList,
				null,
				null,
				mailAccount,
				null, null, 0
				)
				.save();
	}	
	
	
	@Transactional
	public Message createMessage(String model, int id, String subject, String content, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, 
			List<EmailAddress> bccEmailAddressList, MailAccount mailAccount, String linkPath,String addressBlock,int mediaTypeSelect)  {
		
		return this.createMessage(
				content, 
				null, 
				null, 
				model, 
				id, 
				null, 
				0, 
				todayTime.toLocalDateTime(), 
				userInfoService.getUserInfo(),
				userInfoService.getUserActiveCompany(),
				false, 
				IMessage.STATUS_DRAFT, 
				subject, 
				IMessage.TYPE_SENT,
				toEmailAddressList,
				ccEmailAddressList,
				bccEmailAddressList,
				mailAccount,
				linkPath,
				addressBlock,
				mediaTypeSelect)
				.save();
	}	
	
	
	private Message createMessage(String content, EmailAddress fromEmailAddress, UserInfo recipientUserInfo, String relatedTo1Select, int relatedTo1SelectId,
			String relatedTo2Select, int relatedTo2SelectId, LocalDateTime sentDate, UserInfo senderUserInfo,Company company, boolean sentByEmail, int statusSelect, 
			String subject, int typeSelect, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, List<EmailAddress> bccEmailAddressList, 
			MailAccount mailAccount, String filePath,String addressBlock,int mediaTypeSelect)  {
		Message message = new Message();
		message.setContent(content);
		message.setFromEmailAddress(fromEmailAddress);
		message.setRecipientUserInfo(recipientUserInfo);
		message.setRelatedTo1Select(relatedTo1Select);
		message.setRelatedTo1SelectId(relatedTo1SelectId);
		message.setRelatedTo2Select(relatedTo2Select);
		message.setRelatedTo2SelectId(relatedTo2SelectId);
		message.setSentDateT(sentDate);
		message.setSenderUserInfo(senderUserInfo);
		message.setSentByEmail(sentByEmail);
		message.setStatusSelect(statusSelect);
		message.setSubject(subject);
		message.setTypeSelect(typeSelect);
		message.setCompany(company);
		message.setAddressBlock(addressBlock);
		message.setMediaTypeSelect(mediaTypeSelect);
		
		Set<EmailAddress> toEmailAddressSet = Sets.newHashSet();
		if(toEmailAddressList != null)  {
			toEmailAddressSet.addAll(toEmailAddressList);
		}
		message.setToEmailAddressSet(toEmailAddressSet);
		
		Set<EmailAddress> ccEmailAddressSet = Sets.newHashSet();
		if(ccEmailAddressList != null)  {
			ccEmailAddressSet.addAll(ccEmailAddressList);
		}
		message.setCcEmailAddressSet(ccEmailAddressSet);
		
		Set<EmailAddress> bccEmailAddressSet = Sets.newHashSet();
		if(bccEmailAddressList != null)  {
			bccEmailAddressSet.addAll(bccEmailAddressList);
		}
		message.setBccEmailAddressSet(bccEmailAddressSet);
		
		message.setMailAccount(mailAccount);
		
		message.setFilePath(filePath);
		
		return message;
	}	
	
	
	@Transactional
	public Message sendMessageByEmail(Message message)  {
		try {
			
			MailAccount mailAccount = message.getMailAccount();
			
			if(mailAccount != null)  {
			
				List<String> toRecipients = new ArrayList<String>();
				List<String> ccRecipients = new ArrayList<String>();
				List<String> bccRecipients = new ArrayList<String>();
				
				/** Ajout des destinataires  **/
				for(EmailAddress emailAddress : message.getToEmailAddressSet())  {
					
					if(emailAddress.getAddress() != null && !emailAddress.getAddress().isEmpty())  {
					
						toRecipients.add(emailAddress.getAddress());
					}
				}
				
				/** Ajout des destinataires en copie **/
				for(EmailAddress emailAddress : message.getBccEmailAddressSet())  {
					
					if(emailAddress.getAddress() != null && !emailAddress.getAddress().isEmpty())  {
					
						ccRecipients.add(emailAddress.getAddress());
					}
				}
				
				/** Ajout des destinataires  en copie privée **/
				for(EmailAddress emailAddress : message.getCcEmailAddressSet())  {
					
					if(emailAddress.getAddress() != null && !emailAddress.getAddress().isEmpty())  {
					
						bccRecipients.add(emailAddress.getAddress());
					}
				}
				
					
				Map<String, String> attachment = Maps.newHashMap();
				if(message.getFilePath() != null && !message.getFilePath().isEmpty())   {
					attachment.put("File 1", message.getFilePath());
				}	
					
				// Init the sender
				MailSender sender = new MailSender(
						"smtp", 
						mailAccount.getHost(), 
						mailAccount.getPort().toString(), 
						mailAccount.getLogin(),
						mailAccount.getName(),
						mailAccount.getPassword());
				
				
				// Create the Message
				javax.mail.Message msg = sender.createMessage(message.getContent(), message.getSubject(), toRecipients, ccRecipients, bccRecipients, attachment);
				// Send
				Transport.send(msg);
				
				message.setSentByEmail(true);
				message.setStatusSelect(IMessage.STATUS_SENT);
				message.save();
				
			}
			if(!message.getSentByEmail() && message.getRecipientUserInfo()!=null)  {
				message.setStatusSelect(IMessage.STATUS_SENT);
				message.setSentByEmail(false);
				message.save();
			}
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return message;
		
	}
	
	
	public String getSignature(MailAccount mailAccount)  {
		
		if(mailAccount != null && mailAccount.getSignature() != null)  {
			return "\n "+mailAccount.getSignature();
		}
		
		return "";
	}
	
	public String printMessage(Message message){
		Company company = message.getCompany();
		if(company == null)
			return null;
		PrintingSettings printSettings = company.getPrintingSettings();
		printSettings = company.getPrintingSettings();
		if(printSettings == null || printSettings.getDefaultMailBirtTemplate() == null)
			return null;
		BirtTemplate birtTemplate = printSettings.getDefaultMailBirtTemplate();
		LOG.debug("Default BirtTemplate : {}",birtTemplate);
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		maker.setContext(JPA.find(message.getClass(), message.getId()), "Message");
		try {
			return templateMessageService.generatePdfFromBirtTemplate(maker, birtTemplate, "url");
		} catch (AxelorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}