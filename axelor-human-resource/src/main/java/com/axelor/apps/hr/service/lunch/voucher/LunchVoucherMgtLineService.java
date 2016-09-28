package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface LunchVoucherMgtLineService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public LunchVoucherMgtLine create(Employee employee) throws AxelorException;
	
}
