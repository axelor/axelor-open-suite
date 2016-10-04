package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BankOrderService {

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(BankOrder bankOrder) throws AxelorException;
	
	@Transactional
	public BigDecimal updateAmount(BankOrder bankOrder)throws AxelorException;
	
	@Transactional
	public void send(BankOrder bankOrder);
	
	@Transactional
	public void sign(BankOrder bankOrder);
	
	@Transactional
	public BankOrder generateSequence(BankOrder bankOrder);
	
	public void checkLines(BankOrder bankOrder)throws AxelorException;
}
