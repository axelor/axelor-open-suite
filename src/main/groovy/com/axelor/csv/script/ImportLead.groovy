package com.axelor.csv.script

import com.axelor.apps.base.db.UserInfo;
import com.axelor.auth.db.User;

class ImportLead{
	
	public User importCreatedBy(String importId){
		UserInfo userInfo = UserInfo.filter("self.importId = ?1",importId).fetchOne()
		if(userInfo != null && userInfo.internalUser != null)
			return userInfo.internalUser
		return User.all().filter("self.code = 'democrm'").fetchOne()
	}

}
