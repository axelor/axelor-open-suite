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
package com.axelor.apps.base.test.templateRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.apps.base.service.template.TemplateContextLineService;
import com.axelor.apps.base.test.TestModule;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TemplateContextLineTest {
	
	@Inject
	private TemplateContextLineService tcls;

	@Test
	public void test() {
		Map<String, Object> test = Maps.newHashMap();
		
		test.put("select id from Partner partner  where id > 1", "Partner");
		test.put("select id from   Partner   where id > 1", "Partner");
		test.put("select id FROM   Partner where id > 1", "Partner");
		test.put("select id FROM Partner WHERE id > 1", "Partner");
		test.put("select id FROM   Partner where id > 1", "Partner");
		test.put("select id FROM Partner WHERE id > 1", "Partner");
		test.put("select id FROM   Partner   where id > 1", "Partner");
		
		for (String string : test.keySet()) {
			String result = extract(string);
			Assert.assertEquals(test.get(string), result);
		}
	}
	
	public String extract(String string) {
		Pattern pattern = Pattern.compile("(from|FROM)(\\s*)(.+?)(\\s)");
		
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			return matcher.group(3).trim();
		}
		return null;
	}
	
	@Test
	public void test1() {
		String query = "select self from User self WHERE self.createdBy = ?";
		Model bean = User.find(Long.valueOf("1"));
		
		Object o = tcls.evaluate(query, bean);
		
		System.err.println(o);
		System.err.println(o.getClass());
		
		if(o instanceof Collection<?>) {
			List<Object> list = (List) o;
			for (Object line : list) {
				System.err.println(line.getClass());
				System.err.println(line);
			}
		}
	}

}
