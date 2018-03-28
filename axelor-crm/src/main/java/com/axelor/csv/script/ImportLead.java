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
package com.axelor.csv.script;

import com.google.inject.Inject;

import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;

public class ImportLead{
	
	@Inject
	private UserRepository userRepo;
	
	public User importCreatedBy(String importId){
		User user = userRepo.all().filter("self.importId = ?1",importId).fetchOne();
		if(user != null)
			return user;
		return userRepo.all().filter("self.code = 'democrm'").fetchOne();
	}

}
