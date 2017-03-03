package com.axelor.apps.bank.payment.ebics.service;

import java.io.IOException;

import com.axelor.apps.bank.payment.db.EbicsPartner;
import com.axelor.exception.AxelorException;

public interface EbicsPartnerService {

	public void getBankStatements(EbicsPartner ebicsPartner)  throws AxelorException, IOException ;
	
}
