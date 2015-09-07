package com.axelor.apps.hr.service.employee;

import java.math.BigDecimal;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.exception.AxelorException;

public interface EmployeeService extends UserService{

	public BigDecimal getDurationHours(Object object) throws AxelorException;

	public BigDecimal getUserDuration(BigDecimal hourDuration);

}