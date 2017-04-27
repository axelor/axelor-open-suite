package com.axelor.apps.bankpayment.ebics.web;

public class EbicsBankController {
	
	public String normalizeFaxNumber(String faxNumber){
		return faxNumber.replaceAll("\\s|\\.", "");
	}
	
	
}
