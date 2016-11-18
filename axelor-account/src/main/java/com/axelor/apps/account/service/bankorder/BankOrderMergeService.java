package com.axelor.apps.account.service.bankorder;

import java.util.List;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface BankOrderMergeService  {
	
	@Transactional
	public BankOrder mergeBankOrderList(List<BankOrder> bankOrderList) throws AxelorException;
	
}



























