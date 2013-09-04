/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool.templating;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.axelor.apps.tool.db.Contact;
import com.axelor.apps.tool.db.Title;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Maps;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class STTest {
	
	public Contact contact;
	public String contentFinal;
	public Map<String, Object> map = Maps.newHashMap();
	public String content = ""
			+"<h1>About Me ($contact.lastName;format=\"upper\"$)</h1>"
			+"<hr />"
			+"<p><strong>PayeurQuality:</strong> $contact.payeurQuality;format=\"%,2.3f\"$</p>"
			+"<p><strong>Title: $contact.title$</p>"
			+"<p><strong>First Name:</strong> $contact.firstName$</p>"
			+"<p><strong>Last Name:</strong> $contact.lastName;format=\"upper\"$</p>"
			+"<p><strong>DateOfBirth:</strong> $contact.dateOfBirth;format=\"dd/MM/YYYY\"$</p>"
			+"<p>&nbsp;</p>"
			+"<p><em>Contact me:</em>&nbsp;<a href='mailto:$contact.email$' target='_blank'>$contact.fullName$</a></p>"
			+"<hr />$__time__;format=\"HH:mm\"$"
			+"<ul>$__date__$"
			+"<li>Java</li>"
			+"<li>JavaScript</li>"
			+"<li>Groovy</li>"
			+"<li>HTML5</li>"
			+"</ul>"
			+"<pre>public class Hello {<br /><br />"
			+"private String testKey1 = $testKey1$<br />"
			+"private String testKey2 = $testKey2$<br />"
			+"private String testKey3 = $testKey3$<br />"
			+"}</pre>";
	
	@Before
	public void prepareTest() {
		contact = new Contact("Belloy","Pierre");
		contact.setEmail("p.belloy@axelor.com");
		contact.setFullName(contact.getFullName());
		contact.setDateOfBirth(new LocalDate());
		contact.setPayeurQuality(new BigDecimal("2.2569"));
		contact.setLanguage("fr");
		
		Title title = new Title("TitleName", "TitleCode");
		contact.setTitle(title);
		
		map.put("testKey1", "This is the key 1");
		map.put("testKey2", "This is the key 2");
		map.put("testKey3", "This is the key 3");
		
		contentFinal = ""
				+"<h1>About Me (PIERRE)</h1>"
				+"<hr />"
				+"<p><strong>PayeurQuality:</strong> 2,257</p>"
				+"<p><strong>Title: "+title.toString()+"</p>"
				+"<p><strong>First Name:</strong> "+contact.getFirstName()+"</p>"
				+"<p><strong>Last Name:</strong> "+contact.getLastName().toUpperCase()+"</p>"
				+"<p><strong>DateOfBirth:</strong> "+contact.getDateOfBirth().toString("dd/MM/YYYY")+"</p>"
				+"<p>&nbsp;</p>"
				+"<p><em>Contact me:</em>&nbsp;<a href='mailto:"+contact.getEmail()+"' target='_blank'>"+contact.getFullName()+"</a></p>"
				+"<hr />"+(new LocalTime()).toString("HH:mm")
				+"<ul>"+new LocalDate()
				+"<li>Java</li>"
				+"<li>JavaScript</li>"
				+"<li>Groovy</li>"
				+"<li>HTML5</li>"
				+"</ul>"
				+"<pre>public class Hello {<br /><br />"
				+ "private String testKey1 = "+map.get("testKey1")+"<br />"
				+ "private String testKey2 = "+map.get("testKey2")+"<br />"
				+ "private String testKey3 = "+map.get("testKey3")+"<br />"
				+ "}</pre>";
	}
	
	@Test
	public void test1() {
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		maker.setTemplate(content);
		maker.setContext(contact,map,"contact");
		String result = maker.make();
		
		Assert.assertNotNull(result);
		Assert.assertEquals(contentFinal, result);
	}

	@Test
	public void test2() {
		long start = System.currentTimeMillis();
		
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		for (int i = 0; i < 10000; i++) {
			maker.setTemplate(content);
			maker.setContext(contact,map,"contact");
			String result = maker.make();
			
			Assert.assertNotNull(result);
			Assert.assertEquals(contentFinal, result);
		}
		
		//Assert test total time < 10s
		Assert.assertTrue(((System.currentTimeMillis() - start)/1000) < 10);
	}
	
	@Test
	public void test3() {
		for (int i = 0; i < 10; i++) {
			ThreadTest thread = new ThreadTest();
			thread.run();
		}
	}
	
	class ThreadTest extends Thread {
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			
			TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
			
			for (int i = 0; i < 10000; i++) {
				maker.setTemplate(content);
				maker.setContext(contact,map,"contact");
				String result = maker.make();
				
				Assert.assertNotNull(result);
				Assert.assertEquals(contentFinal, result);
			}
			
			//Assert test total time < 10s
			Assert.assertTrue(((System.currentTimeMillis() - start)/1000) < 10);
		}
	}
	
}
