/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.templating;

import com.axelor.apps.tool.db.Contact;
import com.axelor.apps.tool.db.Title;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class STTest {

  public Contact contact;
  public String contentFinal;
  public Map<String, Object> map = Maps.newHashMap();
  public String content =
      ""
          + "<h1>About Me ($contact.lastName;format=\"upper\"$)</h1>"
          + "<hr />"
          + "<p><strong>PayeurQuality:</strong> $contact.payeurQuality;format=\"%,2.3f\"$</p>"
          + "<p><strong>Title: $contact.title$</p>"
          + "<p><strong>First Name:</strong> $contact.firstName$</p>"
          + "<p><strong>Last Name:</strong> $contact.lastName;format=\"upper\"$</p>"
          + "<p><strong>DateOfBirth:</strong> $contact.dateOfBirth;format=\"dd/MM/YYYY\"$</p>"
          + "<p>&nbsp;</p>"
          + "<p><em>Contact me:</em>&nbsp;<a href='mailto:$contact.email$' target='_blank'>$contact.fullName$</a></p>"
          + "<hr />$__time__;format=\"HH\"$"
          + "<ul>$__date__$"
          + "<li>Java</li>"
          + "<li>JavaScript</li>"
          + "<li>Groovy</li>"
          + "<li>HTML5</li>"
          + "</ul>"
          + "<pre>public class Hello {<br /><br />"
          + "private String testKey1 = $testKey1$<br />"
          + "private String testKey2 = $testKey2$<br />"
          + "private String testKey3 = $testKey3$<br />"
          + "}</pre>";

  @Before
  public void prepareTest() {
    contact = new Contact("Doe", "John");
    contact.setEmail("john.doe@axelor.com");
    contact.setFullName(contact.getFullName());
    contact.setDateOfBirth(LocalDate.now());
    contact.setPayeurQuality(new BigDecimal("2.2569"));
    contact.setLanguage("fr");

    Title title = new Title("TitleName", "TitleCode");
    contact.setTitle(title);

    map.put("testKey1", "This is the key 1");
    map.put("testKey2", "This is the key 2");
    map.put("testKey3", "This is the key 3");

    contentFinal =
        ""
            + "<h1>About Me (JOHN)</h1>"
            + "<hr />"
            + "<p><strong>PayeurQuality:</strong> 2,257</p>"
            + "<p><strong>Title: "
            + title.toString()
            + "</p>"
            + "<p><strong>First Name:</strong> "
            + contact.getFirstName()
            + "</p>"
            + "<p><strong>Last Name:</strong> "
            + contact.getLastName().toUpperCase()
            + "</p>"
            + "<p><strong>DateOfBirth:</strong> "
            + contact.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            + "</p>"
            + "<p>&nbsp;</p>"
            + "<p><em>Contact me:</em>&nbsp;<a href='mailto:"
            + contact.getEmail()
            + "' target='_blank'>"
            + contact.getFullName()
            + "</a></p>"
            + "<hr />"
            + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH"))
            + "<ul>"
            + LocalDate.now()
            + "<li>Java</li>"
            + "<li>JavaScript</li>"
            + "<li>Groovy</li>"
            + "<li>HTML5</li>"
            + "</ul>"
            + "<pre>public class Hello {<br /><br />"
            + "private String testKey1 = "
            + map.get("testKey1")
            + "<br />"
            + "private String testKey2 = "
            + map.get("testKey2")
            + "<br />"
            + "private String testKey3 = "
            + map.get("testKey3")
            + "<br />"
            + "}</pre>";
  }

  @Test
  public void test1() {
    TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');

    maker.setTemplate(content);
    maker.setContext(contact, map, "contact");
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
      maker.setContext(contact, map, "contact");
      String result = maker.make();

      Assert.assertNotNull(result);
      Assert.assertEquals(contentFinal, result);
    }

    // Assert test total time < 10s
    Assert.assertTrue(((System.currentTimeMillis() - start) / 1000) < 10);
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
        maker.setContext(contact, map, "contact");
        String result = maker.make();

        Assert.assertNotNull(result);
        Assert.assertEquals(contentFinal, result);
      }

      // Assert test total time < 10s
      Assert.assertTrue(((System.currentTimeMillis() - start) / 1000) < 10);
    }
  }
}
