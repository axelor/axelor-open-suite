package com.axelor.apps.bankpayment.ebics.service;

import java.io.IOException;

import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.exception.AxelorException;

public interface EbicsPartnerService {

	public void getBankStatements(EbicsPartner ebicsPartner)  throws AxelorException, IOException ;
	
}
