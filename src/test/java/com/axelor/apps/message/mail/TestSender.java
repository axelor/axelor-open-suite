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
