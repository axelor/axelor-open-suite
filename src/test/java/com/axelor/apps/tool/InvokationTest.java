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
package com.axelor.apps.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.axelor.apps.tool.db.Contact;

public class InvokationTest {
	
	protected static final Member INVALID_MEMBER;
	static {
		Member invalidMember;
		try {
			invalidMember = InvokationTest.class.getDeclaredField("INVALID_MEMBER");
		} catch (NoSuchFieldException ex) {
			invalidMember = null;
		} catch (SecurityException ex) {
			invalidMember = null;
		}

		INVALID_MEMBER = invalidMember;
	}
	
	/** Cache exact attribute class and property reflection Member object */
	protected static final Map<Class<?>, Map<String, Member>> membersCache =
			new HashMap<Class<?>, Map<String, Member>>();
	
	public Contact contact;
	
	@Before
	public void prepareTest() {
		contact = new Contact("Belloy","Pierre");
		contact.setEmail("p.belloy@axelor.com");
		contact.setFullName(contact.getFullName());
		contact.setDateOfBirth(new LocalDate());
		contact.setPayeurQuality(new BigDecimal("2.2569"));
		contact.setLanguage("fr");
	}

	@Test
	public void test() {
		for (int i = 0; i < 100000; i++) {
			ThreadTest thread = new ThreadTest();
			thread.run();
		}
	}
	
	class ThreadTest extends Thread {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				Object valueEmail = getProperty(contact, "email");
				Assert.assertEquals(contact.getEmail(), valueEmail);
				
				Object valueFullName = getProperty(contact, "fullName");
				Assert.assertEquals(contact.getFullName(), valueFullName);
				
				Object valueFisrtName = getProperty(contact, "firstName");
				Assert.assertEquals(contact.getFirstName(), valueFisrtName);
				
				Object valueLastName = getProperty(contact, "lastName");
				Assert.assertEquals(contact.getLastName(), valueLastName);
				
				Object valueDateOfBirth = getProperty(contact, "dateOfBirth");
				Assert.assertEquals(contact.getDateOfBirth(), valueDateOfBirth);
				
				Object valuePayeurQuality = getProperty(contact, "payeurQuality");
				Assert.assertEquals(contact.getPayeurQuality(), valuePayeurQuality);
				
				Object valueLanguage = getProperty(contact, "language");
				Assert.assertEquals("fr", valueLanguage);
			}
		}
	}
	
	public synchronized Object getProperty(Object o, String property) {
		if (o == null) {
			throw new NullPointerException("o");
		}

		Class<?> c = o.getClass();

		if ( property==null ) {
			return null;
		}

		Member member = findMember(c, property);
		if ( member!=null ) {
			try {
				if (member instanceof Method) {
					return ((Method)member).invoke(o);
				}
				else if (member instanceof Field) {
					return ((Field)member).get(o);
				}
			}
			catch (Exception e) {
			}
		}

		return null;
	}

	public static Member findMember(Class<?> clazz, String memberName) {
		if (clazz == null) {
			throw new NullPointerException("clazz");
		}
		if (memberName == null) {
			throw new NullPointerException("memberName");
		}

		synchronized (membersCache) {
			Map<String, Member> members = membersCache.get(clazz);
			Member member = null;
			if (members != null) {
				member = members.get(memberName);
				if (member != null) {
					return member != INVALID_MEMBER ? member : null;
				}
			}
			else {
				members = new HashMap<String, Member>();
				membersCache.put(clazz, members);
			}

			// try getXXX and isXXX properties, look up using reflection
			String methodSuffix = Character.toUpperCase(memberName.charAt(0)) +
				memberName.substring(1, memberName.length());
			
			member = tryGetMethod(clazz, "get" + methodSuffix);
			if (member == null) {
				member = tryGetMethod(clazz, "is" + methodSuffix);
				if (member == null) {
					member = tryGetMethod(clazz, "has" + methodSuffix);
				}
			}

			if (member == null) {
				// try for a visible field
				member = tryGetField(clazz, memberName);
			}

			members.put(memberName, member != null ? member : INVALID_MEMBER);
			return member;
		}
	}
	
	protected static Method tryGetMethod(Class<?> clazz, String methodName) {
		try {
			Method method = clazz.getMethod(methodName);
			if (method != null) {
				method.setAccessible(true);
			}

			return method;
		} catch (NoSuchMethodException ex) {
		} catch (SecurityException ex) {
		}

		return null;
	}

	protected static Field tryGetField(Class<?> clazz, String fieldName) {
		try {
			Field field = clazz.getField(fieldName);
			if (field != null) {
				field.setAccessible(true);
			}

			return field;
		} catch (NoSuchFieldException ex) {
		} catch (SecurityException ex) {
		}

		return null;
	}

}
