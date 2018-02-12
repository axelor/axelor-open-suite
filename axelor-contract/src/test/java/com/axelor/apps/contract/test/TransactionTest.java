/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.contract.test;

import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.persist.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TransactionTest {

	@Transactional(rollbackOn = {Exception.class})
	public void setup() {
		User user = Beans.get(UserRepository.class).findByCode("admin");
		user.setEmail("aa@axelor.com");
		JPA.save(user);
	}



	@Test
	public void test() {
		setup();

		try {
			t1();
		} catch(Exception e) {
		}

		User user = Beans.get(UserRepository.class).findByCode("admin");
		System.out.println(user.getEmail());
		Assert.assertTrue(user.getEmail() == null || !user.getEmail().equals("p.belloy@axelor.com"));
	}

	@Transactional(rollbackOn = {Exception.class})
	public void t1() throws Exception {
		System.out.println("I'm in t1");

		User user = Beans.get(UserRepository.class).findByCode("admin");
		user.setEmail("p.belloy@axelor.com");

		t2(user);

		throw new Exception("This is an exception");

		//JPA.save(user);
	}

	@Transactional(rollbackOn = {Exception.class})
	public void t2(User user) {
		user.setEmail("p.belloy@axelor.com");

		JPA.save(user);
	}




	@Test
	public void test2() {
		setup();

		try {
			t11();
		} catch(Exception e) {
		}

		User user = Beans.get(UserRepository.class).findByCode("admin");
		System.out.println(user.getEmail());
		Assert.assertTrue(user.getEmail() == null || !user.getEmail().equals("p.belloy@axelor.com"));
	}

	@Transactional(rollbackOn = {Exception.class})
	private void t11() throws Exception {
		User user = Beans.get(UserRepository.class).findByCode("admin");

		Beans.get(myNestedService.class).nestedMethod(user);

		throw new Exception("This is an exception");
	}

	public class myNestedService {

		@Transactional
		public void nestedMethod(User user) throws Exception {
			user.setEmail("p.belloy@axelor.com");

			JPA.save(user);

			throw new Exception("This is an exception");
		}

	}

}
