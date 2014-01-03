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
package com.axelor.apps.tool.templating;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import com.axelor.db.Model;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class STSampleTest {
	
	public String template = "Hi $contact.name;format=\"upper\"$ $contact.lastName$";
	
	private static List<Contact> data = Lists.newArrayList();

	private static final int MAX_ITER = 5000;
	
	private static final char CHAR = '$';
	
	private STGroup stGroup;
	
	@Before
	public void before() {
		for(int i = 0 ; i < MAX_ITER ; i++) {
			data.add(new Contact("Name"+i,"LastName"+i));
		}
	}

	@Test
	public void test() {
		
		stGroup = new STGroup(CHAR, CHAR);
		stGroup.registerRenderer(String.class, new BasicFormatRenderer());
		
		for (Contact contact : data) {
			String result = run(contact);
			
			String expected = "Hi "+contact.getName().toUpperCase()+" "+contact.getLastName();
			Assert.assertEquals(expected, result);
		}
	}
	
	public String run(Contact o) {
		ST st = new ST(stGroup, template);
		st.add("contact", o);
		
		return st.render();
	}
	
	class BasicFormatRenderer implements AttributeRenderer {
		
	    public String toString(Object o) {
	        return o.toString();
	    }

	    public String toString(Object o, String formatName) {
	    	
	    	if(Strings.isNullOrEmpty(formatName)) {
	    		return toString(o);
	    	}
	    	
	        if (formatName.equals("upper")) {
	            return o.toString().toUpperCase();
	        } else if (formatName.equals("toLower")) {
	            return o.toString().toLowerCase();
	        }
	        return toString(o);
	    }
	    
		@Override
		public String toString(Object o, String formatString, Locale locale) {
			if(o == null) {
				return null;
			}
			return toString(o, formatString);
		}
	}
	
	class Contact extends Model {
		private Long id;
		private String name;
		private String lastName;
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		public Contact(String name, String lastName) {
			this.name = name;
			this.lastName = lastName;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public void setId(Long id) {
			this.id = id;
		}
	}
}
