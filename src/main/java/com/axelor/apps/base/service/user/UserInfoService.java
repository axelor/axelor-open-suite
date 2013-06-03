package com.axelor.apps.base.service.user;

import com.axelor.apps.base.db.Agency;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;

/**
 * UserInfoService est une classe implémentant l'ensemble des services pour
 * les informations utilisateur.
 * 
 * @author Pierre Belloy
 * 
 * @version 1.0
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
     * @return Agency
     *      L'agence
     */
    public Agency getUserAgency() {
        
        UserInfo userInfo = getUserInfo();
        if (userInfo != null && userInfo.getAgency() != null){
            return userInfo.getAgency();
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
}
