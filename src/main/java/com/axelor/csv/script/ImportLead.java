package com.axelor.csv.script;

import com.axelor.apps.base.db.UserInfo;
import com.axelor.auth.db.User;

public class ImportLead{
	
	public User importCreatedBy(String importId){
		UserInfo userInfo = UserInfo.all_().filter("self.importId = ?1",importId).fetchOne();
		if(userInfo != null && userInfo.getInternalUser() != null)
			return userInfo.getInternalUser();
		return User.all_().filter("self.code = 'democrm'").fetchOne();
	}

}
