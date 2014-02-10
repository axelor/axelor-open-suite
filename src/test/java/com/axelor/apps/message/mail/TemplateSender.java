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
package com.axelor.apps.message.mail;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.message.mail.MailSender;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Maps;

@RunWith(GuiceRunner.class)
@GuiceModules({ MyModule.class })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TemplateSender {

	private final String SMTP_HOST = "smtp.gmail.com";
	private final String PORT = "465";
	private final String FROM_ADDRESS = "xxx@gmail.com";
	private final String PASSWORD = "password";
	private String[] recipients = new String[] { "xxx@axelor.com" };

	public String content = ""
			+"<h1>About Me ($contact.name;format=\"upper\"$)</h1>"
			+"<hr />"
			+"<p><strong>ImportID:</strong> $contact.importId$</p>"
			+"<p><strong>Title: $contact.titleSelect$</p>"
			+"<p><strong>Phone:</strong> $contact.fixedPhonePro$</p>"
			+"<p><strong>Email:</strong> $contact.email;format=\"upper\"$</p>";

	public String contentWithContext = ""
			+"<h1>About Me ($contact.name;format=\"upper\"$)</h1>"
			+"<hr />"
			+"<p><strong>ImportID:</strong> $contact.importId$</p>"
			+"<p><strong>Title: $contact.titleSelect$</p>"
			+"<p><strong>Phone:</strong> $contact.fixedPhonePro$</p>"
			+"<p><strong>Email:</strong> $contact.email;format=\"upper\"$</p>"
			+"<pre>public class Hello {<br /><br />"
			+"private String testKey1 = $testKey1$<br />"
			+"private String testKey2 = $testKey2$<br />"
			+"private String testKey3 = $testKey3$<br />"
			+"}</pre>";;

	@Test
	public void testSimple() {

		//Init the maker
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		//Set template
		maker.setTemplate(content);
		//Set context
		maker.setContext(Partner.all().filter("self.isContact IS TRUE").fetchOne(), "contact");
		//Make it
		String result = maker.make();

		try {

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, PASSWORD);
			// Short method to create and send the message
			sender.send(result, "Here is the subject", recipients);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testWithContext() {

		//Init the maker
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');

		//Create custome context
		Map<String, Object> map = Maps.newHashMap();
		map.put("testKey1", "This is the key 1");
		map.put("testKey2", "This is the key 2");
		map.put("testKey3", "This is the key 3");

		//Set template
		maker.setTemplate(contentWithContext);
		//Set context with the context
		maker.setContext(Partner.all().filter("self.isContact IS TRUE").fetchOne(), map, "contact");
		//Make it
		String result = maker.make();

		try {

			// Init the sender
			MailSender sender = new MailSender("smtp", SMTP_HOST, PORT, FROM_ADDRESS, PASSWORD);
			// Short method to create and send the message
			sender.send(result, "Here is the subject", recipients);

		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


}
