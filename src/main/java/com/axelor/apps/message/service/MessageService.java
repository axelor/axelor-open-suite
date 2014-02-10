/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.message.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Transport;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.IMessage;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.mail.MailSender;
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
				false, 
				IMessage.STATUS_SENT, 
				"Remind : "+event.getSubject(), 
				IMessage.TYPE_RECEIVED,
				toEmailAddressList,
				null,
				null,
				mailAccount,
				null
				)
				.save();
	}	
	
	
	@Transactional
	public Message createMessage(String model, int id, String subject, String content, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, 
			List<EmailAddress> bccEmailAddressList, MailAccount mailAccount, String linkPath)  {
		
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
				false, 
				IMessage.STATUS_DRAFT, 
				subject, 
				IMessage.TYPE_SENT,
				toEmailAddressList,
				ccEmailAddressList,
				bccEmailAddressList,
				mailAccount,
				linkPath)
				.save();
	}	
	
	
	private Message createMessage(String content, EmailAddress fromEmailAddress, UserInfo recipientUserInfo, String relatedTo1Select, int relatedTo1SelectId,
			String relatedTo2Select, int relatedTo2SelectId, LocalDateTime sentDate, UserInfo senderUserInfo, boolean sentByEmail, int statusSelect, 
			String subject, int typeSelect, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, List<EmailAddress> bccEmailAddressList, 
			MailAccount mailAccount, String filePath)  {
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
	
}
