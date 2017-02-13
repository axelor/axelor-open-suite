package com.axelor.apps.account.ebics.service;

import com.axelor.apps.account.db.EbicsBank;

public class EbicsBankServiceImpl implements EbicsBankService {
	
	@Override
	public void computeFullName(EbicsBank ebicsBank) {
		ebicsBank.setFullName(ebicsBank.getBank().getFullName());
		ebicsBank.setName(ebicsBank.getBank().getBankName());
		ebicsBank.setHostId(ebicsBank.getBank().getCode());
	}
}
