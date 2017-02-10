package com.axelor.apps.hr.service.user;

import com.axelor.auth.db.User;
import com.google.inject.persist.Transactional;

public interface UserHrService {
	@Transactional
	public void createEmployee(User user);
}
