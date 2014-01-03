/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.user;

import com.axelor.apps.account.db.CashRegister;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;

/**
 * UserInfoService est une classe implémentant l'ensemble des services pour
 * les informations utilisateur.
 * 
 */
public class UserInfoService {

	/**
	 * Méthode qui retourne le userInfo de l'utilisateur connecté
	 * 
	 * @return UserInfo
	 * 		Le userInfo de l'utilisateur
	 */
	public UserInfo getUserInfo() {
		User user = null;
		try{
			user = AuthUtils.getUser();
		}
		catch(Exception ex){}
		
		if (user != null){
			UserInfo userInfo = UserInfo.all().filter("internalUser = ?1", user).fetchOne();
			if (userInfo != null) { return userInfo; }
		}
	
		return null;
	}
	
	
	 /**
     * Méthode qui retourne l'agence de l'utilisateur connecté
     * 
     * @return CashRegister
     *      La caisse
     */
    public CashRegister getUserCashRegister() {
        
        UserInfo userInfo = getUserInfo();
        if (userInfo != null && userInfo.getActiveCashRegister() != null){
            return userInfo.getActiveCashRegister();
        }
    
        return null;
    }
    
	
	/**
	 * Méthode qui retourne la société active de l'utilisateur connecté
	 * 
	 * @return Company
	 * 		La société
	 */
	public Company getUserActiveCompany() {
		
		UserInfo userInfo = getUserInfo();
		if (userInfo != null && userInfo.getActiveCompany() != null){
			return userInfo.getActiveCompany();
		}
	
		return null;
	}
    
	
	/**
	 * Méthode qui retourne l'équipe active de l'utilisateur connecté
	 * 
	 * @return Team
	 * 		L'équipe
	 */
	public Team getUserActiveTeam() {
		
		UserInfo userInfo = getUserInfo();
		if (userInfo != null && userInfo.getActiveTeam() != null){
			return userInfo.getActiveTeam();
		}
	
		return null;
	}
	
	/**
	 * Méthode qui retourne l'équipe active de l'utilisateur connecté
	 * 
	 * @return Team
	 * 		L'équipe
	 */
	public Long getUserActiveTeamId() {
		
		UserInfo userInfo = getUserInfo();
		if (userInfo != null && userInfo.getActiveTeam() != null){
			return userInfo.getActiveTeam().getId();
		}
	
		return null;
	}
	
	/**
	 * Méthode qui retourne le tiers de l'utilisateur connecté
	 * 
	 * @return Partner
	 * 		Le tiers
	 */
	public Partner getUserPartner() {
		
		UserInfo userInfo = getUserInfo();
		if (userInfo != null && userInfo.getPartner() != null){
			return userInfo.getPartner();
		}
	
		return null;
	}
}
