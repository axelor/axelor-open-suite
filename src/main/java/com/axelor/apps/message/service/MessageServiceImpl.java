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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Transport;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.IMessage;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.mail.MailSender;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MessageServiceImpl extends MessageRepository implements MessageService {

	private DateTime todayTime;
	
	@Inject
	public MessageServiceImpl() {

		this.todayTime = new DateTime();
	}
	
	
	@Transactional
	public Message createMessage(String model, int id, String subject, String content, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, 
			List<EmailAddress> bccEmailAddressList, MailAccount mailAccount, String linkPath,String addressBlock,int mediaTypeSelect)  {
		
		return save(this.createMessage(
				content, 
				null, 
				model, 
				id, 
				null, 
				0, 
				todayTime.toLocalDateTime(), 
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
				mediaTypeSelect));
	}	
	
	
	protected Message createMessage(String content, EmailAddress fromEmailAddress, String relatedTo1Select, int relatedTo1SelectId,
			String relatedTo2Select, int relatedTo2SelectId, LocalDateTime sentDate, boolean sentByEmail, int statusSelect, 
			String subject, int typeSelect, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, List<EmailAddress> bccEmailAddressList, 
			MailAccount mailAccount, String filePath,String addressBlock,int mediaTypeSelect)  {
		Message message = new Message();
		message.setContent(content);
		message.setFromEmailAddress(fromEmailAddress);
		message.setRelatedTo1Select(relatedTo1Select);
		message.setRelatedTo1SelectId(relatedTo1SelectId);
		message.setRelatedTo2Select(relatedTo2Select);
		message.setRelatedTo2SelectId(relatedTo2SelectId);
		message.setSentDateT(sentDate);
		message.setSentByEmail(sentByEmail);
		message.setStatusSelect(statusSelect);
		message.setSubject(subject);
		message.setTypeSelect(typeSelect);
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
			
			this.sendByEmail(message);
			
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return message;
		
	}
	
	protected void sendByEmail(Message message) throws MessagingException, UnsupportedEncodingException  {
		
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
			save(message);
			
		}
	
	
	}
	
	
	
	
	public String getSignature(MailAccount mailAccount)  {
		
		if(mailAccount != null && mailAccount.getSignature() != null)  {
			return "\n "+mailAccount.getSignature();
		}
		
		return "";
	}
	
	
}