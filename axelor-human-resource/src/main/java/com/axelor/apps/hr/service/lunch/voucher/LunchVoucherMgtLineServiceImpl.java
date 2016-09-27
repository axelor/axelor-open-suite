package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class LunchVoucherMgtLineServiceImpl implements LunchVoucherMgtLineService{

	
	
	@Override
	@Transactional
	public LunchVoucherMgtLine create(Employee employee) throws AxelorException {
		LunchVoucherMgtLine lunchVoucherMgtLine = new LunchVoucherMgtLine();
		lunchVoucherMgtLine.setEmployee(employee);
		lunchVoucherMgtLine.setLunchVoucherNumber(20);
		
		return lunchVoucherMgtLine;
	}

}
