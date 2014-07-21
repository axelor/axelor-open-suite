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
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.axelor.apps.message.db.EmailAddress;
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
			+"<p><strong>ImportID:</strong> $emailAddress.importId$</p>"
			+"<p><strong>Name: $emailAddress.name$</p>"
			+"<p><strong>Email:</strong> $emailAddress.address;format=\"upper\"$</p>";

	public String contentWithContext = ""
			+"<h1>About Me ($contact.name;format=\"upper\"$)</h1>"
			+"<hr />"
			+"<p><strong>ImportID:</strong> $emailAddress.importId$</p>"
			+"<p><strong>Name: $emailAddress.name$</p>"
			+"<p><strong>Email:</strong> $emailAddress.address;format=\"upper\"$</p>"
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
		maker.setContext(EmailAddress.all().fetchOne(), "emailAddress");
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
		maker.setContext(EmailAddress.all().fetchOne(), map, "emailAddress");
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
