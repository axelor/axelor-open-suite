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
package com.axelor.apps.message.mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.common.base.Preconditions;

public class MailSender extends MailConnection {

	public MailSender(String protocol, String host, String port, String userName, String password) throws MessagingException {
		this(protocol, host, port, userName, null, password);
	}

	public MailSender(String protocol, String host, String port, String userName, String alais, String password) throws MessagingException {
		super(protocol, host, port, userName, alais, password);
	}

	public void send(String content, String subject, String mineType, String[] recipients) throws MessagingException, UnsupportedEncodingException {
		Preconditions.checkArgument(recipients != null && recipients.length > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			toAddresses[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses);

		msg.setSubject(subject);
		msg.setContent(content, mineType);

		Transport.send(msg);
	}

	public void send(String content, String subject, String[] recipients) throws MessagingException, UnsupportedEncodingException {
		Preconditions.checkArgument(recipients != null && recipients.length > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			toAddresses[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses);

		msg.setSubject(subject);
		msg.setContent(this.createMultiPart(content, null));

		Transport.send(msg);
	}
	
	
	public void send(String content, String subject, List<String> recipientsTo, List<String> recipientsCc, List<String> recipientsBcc, Map<String, String> files) throws MessagingException, UnsupportedEncodingException {

		Transport.send(this.createMessage(content, subject, recipientsTo, recipientsCc, recipientsBcc, files));
	}
	

	public void send(Message msg) throws MessagingException, UnsupportedEncodingException {
		Transport.send(msg);
	}

	
	public Message createMessage(String content, String subject, List<String> recipientsTo, List<String> recipientsCc, List<String> recipientsBcc, Map<String, String> files) throws UnsupportedEncodingException, MessagingException {
		Preconditions.checkArgument((recipientsTo != null && recipientsTo.size() > 0) || (recipientsCc != null && recipientsCc.size() > 0) || (recipientsBcc != null && recipientsBcc.size() > 0), 
				"Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipientsTo.size()];
		InternetAddress[] ccAddresses = new InternetAddress[recipientsCc.size()];
		InternetAddress[] bccAddresses = new InternetAddress[recipientsBcc.size()];
		int i = 0;
		
		if(recipientsTo != null)  {
			for (String recipient : recipientsTo) {
				toAddresses[i] = new InternetAddress(recipient);
				i++;
			}
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
		}
		
		if(recipientsCc != null)  {
			for (String recipient : recipientsCc) {
				ccAddresses[i] = new InternetAddress(recipient);
				i++;
			}
			msg.setRecipients(Message.RecipientType.CC, ccAddresses);
		}
		
		if(recipientsBcc != null)  {
			for (String recipient : recipientsBcc) {
				bccAddresses[i] = new InternetAddress(recipient);
				i++;
			}
			msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
		}

		msg.setSubject(subject);
		msg.setContent(this.createMultiPart(content, files));

		return msg;
	}
	
	
	public Message createMessage(String content, String subject, String[] recipients) throws UnsupportedEncodingException, MessagingException {
		Preconditions.checkArgument(recipients != null && recipients.length > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			toAddresses[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses);

		msg.setSubject(subject);
		msg.setContent(this.createMultiPart(content, null));

		return msg;
	}

	public Message createMessage(String content, String subject, String[] recipients, Map<String, String> files) throws UnsupportedEncodingException, MessagingException {
		Preconditions.checkArgument(recipients != null && recipients.length > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++) {
			toAddresses[i] = new InternetAddress(recipients[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses);

		msg.setSubject(subject);
		msg.setContent(this.createMultiPart(content, files));

		return msg;
	}
	

	private Multipart createMultiPart(String content, Map<String, String> files) throws MessagingException {
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(this.createPart(content));
		this.createAttachment(files, mp);

		return mp;
	}

	private void createAttachment(Map<String, String> files, Multipart mp) {
		if(files != null && !files.isEmpty()) {
			for (String name : files.keySet()) {
				try {
					String link = files.get(name);
					mp.addBodyPart(this.createAttachmentPart(name,link));
				} catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
	}

	private MimeBodyPart createPart(String body) throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setContent(body, "text/html");

		return part;
	}

	private MimeBodyPart createAttachmentPart(String name, String link) throws MessagingException, IOException {
		MimeBodyPart part = new MimeBodyPart();
		if(link.startsWith("http://")) {
			part.setDataHandler(new DataHandler(new URLDataSource(new URL(link))));
			part.setFileName(name);
		}
		else {
			part.attachFile(link);
		}

		return part;
	}

}
