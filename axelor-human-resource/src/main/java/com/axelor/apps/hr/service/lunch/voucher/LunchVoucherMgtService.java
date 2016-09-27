package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface LunchVoucherMgtService {
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void calculate(LunchVoucherMgt lunchVoucherMgt) throws AxelorException;
}
