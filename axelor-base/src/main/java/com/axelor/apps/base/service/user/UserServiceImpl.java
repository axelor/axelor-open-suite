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
package com.axelor.apps.base.service.user;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Team;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

/**
 * UserService is a class that implement all methods for user informations
 * 
 */
public class UserServiceImpl implements UserService  {

	@Inject
	private UserRepository userRepo;
	
	/**
	 * Method that return the current connected user
	 * 
	 * @return user
	 * 		the current connected user
	 */
	public User getUser() {
		User user = null;
		try{
			user = AuthUtils.getUser();
		}
		catch(Exception ex){}
		if(user == null) {
			user = userRepo.findByCode("admin");			
		}
		return user;
	}
	
	/**
	 * Method that return the id of the current connected user
	 * 
	 * @return user
	 * 		the id of current connected user
	 */
	public Long getUserId() {
		
		final User user = this.getUser();
	
		if(user == null)  {  return null;  }
		
		return user.getId();
	}
	
	/**
	 * Method that return the active company of the current connected user
	 * 
	 * @return Company
	 * 		the active company
	 */
	public Company getUserActiveCompany() {
		
		User user = getUser();
		
		if(user == null)  {  return null;  }
		
		return user.getActiveCompany();
	}
	
	/**
	 * Method that return the active company id of the current connected user
	 * 
	 * @return Company
	 * 		the active company id
	 */
	public Long getUserActiveCompanyId() {
		
		final Company company = this.getUserActiveCompany();
		
		if(company == null)  {  return null;  }
	
		return company.getId();
	}
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team
	 */
	public MetaFile getUserActiveCompanyLogo() {

		final Company company = this.getUserActiveCompany();
		
		if(company == null)  {  return null;  }
		
		return company.getLogo();
		
	}
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team
	 */
	public Team getUserActiveTeam() {
		
		final User user = getUser();
		
		if(user == null)  {  return null;  }
	
		return user.getActiveTeam();
	}
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team id
	 */
	public Long getUserActiveTeamId() {
		
		final Team team = this.getUserActiveTeam();
		
		if(team == null)  {  return null;  }
		
		return team.getId();
	}
	
	/**
	 * Method that return the partner of the current connected user
	 * 
	 * @return Partner
	 * 		the user partner
	 */
	public Partner getUserPartner() {
		
		final User user = getUser();

		if (user == null)  {  return null;  }
			
		return user.getPartner();
	}
}
 