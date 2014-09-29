/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import com.axelor.auth.db.User;
import com.axelor.db.Repository;

/**
 * UserService est une classe implémentant l'ensemble des services pour
 * les informations utilisateur.
 * 
 */
public interface UserService extends Repository<User> {

	/**
	 * Méthode qui retourne le user de l'utilisateur connecté
	 * 
	 * @return user
	 * 		Le user de l'utilisateur
	 */
	public User getUser();
	
	public Long getUserId();
	
	/**
	 * Méthode qui retourne la société active de l'utilisateur connecté
	 * 
	 * @return Company
	 * 		La société
	 */
	public Company getUserActiveCompany();
    

	/**
	 * Méthode qui retourne la société active de l'utilisateur connecté
	 * 
	 * @return Company
	 * 		La société
	 */
	public Long getUserActiveCompanyId(); 
	
	/**
	 * Méthode qui retourne l'équipe active de l'utilisateur connecté
	 * 
	 * @return Team
	 * 		L'équipe
	 */
	public Team getUserActiveTeam();
	
	/**
	 * Méthode qui retourne l'équipe active de l'utilisateur connecté
	 * 
	 * @return Team
	 * 		L'équipe
	 */
	public Long getUserActiveTeamId();
	
	/**
	 * Méthode qui retourne le tiers de l'utilisateur connecté
	 * 
	 * @return Partner
	 * 		Le tiers
	 */
	public Partner getUserPartner();
}
