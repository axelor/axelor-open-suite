/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.auth.db.repo.UserRepository;
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
	
	@Inject
	private UserRepository userRepo;

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
		Model bean = userRepo.find(Long.valueOf("1"));
		
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
