package com.axelor.apps.bank.payment.ebics.web;

public class EbicsBankController {
	public String normalizeFaxNumber(String faxNumber){
		return faxNumber.replaceAll("\\s|\\.", "");
	}
}
