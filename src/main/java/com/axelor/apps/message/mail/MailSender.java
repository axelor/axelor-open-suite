/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
	
	
	public void send(String content, String subject, List<String> recipients) throws MessagingException, UnsupportedEncodingException {
		Preconditions.checkArgument(recipients != null && recipients.size() > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.size()];
		int i = 0;
		for (String recipient : recipients) {
			toAddresses[i] = new InternetAddress(recipient);
			i++;
		}
		msg.setRecipients(Message.RecipientType.TO, toAddresses);

		msg.setSubject(subject);
		msg.setContent(this.createMultiPart(content, null));

		Transport.send(msg);
	}
	

	public void send(Message msg) throws MessagingException, UnsupportedEncodingException {
		Transport.send(msg);
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
	
	public Message createMessage(String content, String subject, List<String> recipients, Map<String, String> files) throws UnsupportedEncodingException, MessagingException {
		Preconditions.checkArgument(recipients != null && recipients.size() > 0, "Recipients can not be null or empty");

		Message msg = new MimeMessage(getSession());
		InternetAddress from = new InternetAddress(getUserName(), getAliasName());
		msg.setFrom(from);

		InternetAddress[] toAddresses = new InternetAddress[recipients.size()];
		int i = 0;
		for (String recipient : recipients) {
			toAddresses[i] = new InternetAddress(recipient);
			i++;
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
