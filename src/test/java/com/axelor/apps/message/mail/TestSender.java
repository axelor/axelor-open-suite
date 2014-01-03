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

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.junit.Test;

import com.axelor.apps.message.mail.MailSender;
import com.google.common.collect.Maps;

public class TestSender {

	private final String SMTP_HOST = "smtp.gmail.com";
	private final String PORT = "465";
	private final String FROM_ADDRESS = "xxx@gmail.com";
	private final String PASSWORD = "pass";
	private final String ALAIS = "My Alias";
	private String[] recipients = new String[] { "xxx@axelor.com" };

	/**
	 * Simple exemple to send simple email
	 */
	@Test
	public void testSimple() {
		try {

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, PASSWORD);
			// Short method to create and send the message
			sender.send("Here is a content", "Here is the subject", recipients);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple exemple to send simple email with an alias as sender
	 */
	@Test
	public void testAlais() {
		try {

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, ALAIS, PASSWORD);
			// Short method to create and send the message
			sender.send("Here is a content", "Here is the subject", recipients);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple exemple to send simple email with an alias as sender
	 */
	@Test
	public void testHtml() {
		try {

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, ALAIS, PASSWORD);
			// Short method to create and send the message
			sender.send("<h1>Hello world</h1> <br/> This is a text message. <br/> Regards!", "Here is the subject", recipients);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simple exemple to send email with attachment
	 * Note : Depending on the attachment size, can take times.
	 */
	@Test
	public void testAttachment() {
		try {

			// Create the Map of attachment
			// NOTE: the file name is not managed for the moment
			Map<String, String> attachment = Maps.newHashMap();
			attachment.put("File 1", "/home/axelor/Downloads/LIGNE+3+HIVER+13+(6+VOL)+WEB.pdf");

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, PASSWORD);
			// Create the Message
			Message msg = sender.createMessage("Here is a content", "Here is the subject", recipients, attachment);
			// Send
			Transport.send(msg);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
