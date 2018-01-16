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
package com.axelor.apps.base.db.repo;

import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;

public class UserBaseRepository extends UserRepository {
	
	@Override
	public User copy(User entity, boolean deep) {
		
		User copy = new User();
		
		copy.setGroup(entity.getGroup());
		copy.setRoles(entity.getRoles());
		copy.setPermissions(entity.getPermissions());
		copy.setMetaPermissions(entity.getMetaPermissions());
		copy.setActiveCompany(entity.getActiveCompany());
		copy.setCompanySet(entity.getCompanySet());
		copy.setLanguage(entity.getLanguage());
		copy.setHomeAction(entity.getHomeAction());
		copy.setSingleTab(entity.getSingleTab());
		copy.setNoHelp(entity.getNoHelp());
		
		return super.copy(copy, deep);
	}
}
