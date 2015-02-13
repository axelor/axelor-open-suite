package com.axelor.apps.human.resource.service.user;

import java.math.BigDecimal;

import com.axelor.apps.base.service.user.UserService;

public interface UserServiceHr extends UserService{
	
	public BigDecimal getDurationHours(BigDecimal userDuration);
		
	public BigDecimal getUserDuration(BigDecimal hourDuration);
	
}