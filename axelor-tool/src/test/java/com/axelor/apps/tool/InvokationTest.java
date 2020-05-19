/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

import com.axelor.apps.tool.db.Contact;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    contact = new Contact("Durand", "Pierre");
    contact.setEmail("@test.com");
    contact.setFullName(contact.getFullName());
    contact.setDateOfBirth(LocalDate.now());
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

    if (property == null) {
      return null;
    }

    Member member = findMember(c, property);
    if (member != null) {
      try {
        if (member instanceof Method) {
          return ((Method) member).invoke(o);
        } else if (member instanceof Field) {
          return ((Field) member).get(o);
        }
      } catch (Exception e) {
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
      } else {
        members = new HashMap<String, Member>();
        membersCache.put(clazz, members);
      }

      // try getXXX and isXXX properties, look up using reflection
      String methodSuffix =
          Character.toUpperCase(memberName.charAt(0))
              + memberName.substring(1, memberName.length());

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
