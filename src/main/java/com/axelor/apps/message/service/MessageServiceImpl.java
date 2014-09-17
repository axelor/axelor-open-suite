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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.mail.MailBuilder;
import com.axelor.mail.MailSender;
import com.axelor.mail.SmtpAccount;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MessageServiceImpl extends MessageRepository implements MessageService {

	private DateTime todayTime;
	
	@Inject
	public MessageServiceImpl() {

		this.todayTime = new DateTime();
	}

	@Inject
	protected MailAccountService mailAccoutService;
	
	
	@Transactional
	public Message createMessage(String model, int id, String subject, String content, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, 
			List<EmailAddress> bccEmailAddressList, MailAccount mailAccount, String linkPath, String addressBlock, int mediaTypeSelect)  {
		
		return save(this.createMessage(
				content, 
				null, 
				model, 
				id, 
				null, 
				0, 
				todayTime.toLocalDateTime(), 
				false, 
				STATUS_DRAFT, 
				subject, 
				TYPE_SENT,
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
		
		
		Set<EmailAddress> toEmailAddressSet = Sets.newHashSet();
		if(toEmailAddressList != null)  {
			toEmailAddressSet.addAll(toEmailAddressList);
		}
		
		Set<EmailAddress> ccEmailAddressSet = Sets.newHashSet();
		if(ccEmailAddressList != null)  {
			ccEmailAddressSet.addAll(ccEmailAddressList);
		}
		
		Set<EmailAddress> bccEmailAddressSet = Sets.newHashSet();
		if(bccEmailAddressList != null)  {
			bccEmailAddressSet.addAll(bccEmailAddressList);
		}
		
		Message message = new Message(typeSelect, subject, content, statusSelect, mediaTypeSelect, addressBlock, toEmailAddressSet, ccEmailAddressSet, bccEmailAddressSet, 
                fromEmailAddress, sentByEmail, mailAccount);

		message.setRelatedTo1Select(relatedTo1Select);
		message.setRelatedTo1SelectId(relatedTo1SelectId);
		message.setRelatedTo2Select(relatedTo2Select);
		message.setRelatedTo2SelectId(relatedTo2SelectId);
		message.setSentDateT(sentDate);
		
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
		
	}
	
	
	protected void sendByEmail(Message message) throws MessagingException, IOException  {
		
		MailAccount mailAccount = message.getMailAccount();
		
		if(mailAccount != null)  {
			
			com.axelor.mail.MailAccount account = new SmtpAccount(
					mailAccount.getHost(), mailAccount.getPort().toString(), mailAccount.getLogin(), mailAccount.getPassword(), mailAccoutService.getSmtpSecurity(mailAccount));
					                               
			MailSender sender = new MailSender(account);

			List<String> toRecipients = this.getEmailAddresses(message.getToEmailAddressSet());
			List<String> ccRecipients = this.getEmailAddresses(message.getCcEmailAddressSet());
			List<String> bccRecipients = this.getEmailAddresses(message.getBccEmailAddressSet());

			MailBuilder mailBuilder = sender.compose();

			mailBuilder.subject(message.getSubject());

			if(!Strings.isNullOrEmpty(message.getContent()))  {
				mailBuilder.text(message.getContent());
			}
			
			if(!Strings.isNullOrEmpty(message.getFilePath()))  {
				mailBuilder.attach("File", message.getFilePath());
			}

			if(toRecipients != null && !toRecipients.isEmpty())  {
				mailBuilder.to(Joiner.on(",").join(toRecipients));
			}

			if(ccRecipients != null && !ccRecipients.isEmpty())  {
				mailBuilder.cc(Joiner.on(",").join(ccRecipients));
			}

			if(bccRecipients != null && !bccRecipients.isEmpty())  {
				mailBuilder.bcc(Joiner.on(",").join(bccRecipients));
			}
				
			mailBuilder.send();
			
			message.setSentByEmail(true);
			message.setStatusSelect(STATUS_SENT);
			save(message);
			
		}
	
	}
	
	
	public List<String> getEmailAddresses(Set<EmailAddress> emailAddressSet)  {
		               
	   List<String> recipients = Lists.newArrayList();
	   
	   for(EmailAddress emailAddress : emailAddressSet)  {
	           
	           if(!Strings.isNullOrEmpty(emailAddress.getAddress()))  {
	           
	                   recipients.add(emailAddress.getAddress());
	           }
	   }
	   
	   return recipients;
	}

}