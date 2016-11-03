package com.axelor.apps.account.service.bankOrder;

import java.math.BigDecimal;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BankOrderService {

	
	@Transactional
	public BigDecimal computeTotalAmount(BankOrder bankOrder)throws AxelorException;
	
	@Transactional
	public void confirm(BankOrder bankOrder);
	
	@Transactional
	public void sign(BankOrder bankOrder);
	
	@Transactional
	public void validate(BankOrder bankOrder);
	
	@Transactional
	public BankOrder generateSequence(BankOrder bankOrder);
	
	public void checkLines(BankOrder bankOrder)throws AxelorException;
	
	@Transactional
	public void validatePayment(BankOrder bankOrder);
	
	@Transactional
	public void cancelPayment(BankOrder bankOrder);
	
	@Transactional
	public void cancelBankOrder(BankOrder bankOrder);
	
}
