package com.axelor.apps.account.ebics.web;

public class EbicsBankController {
	public String normalizeFaxNumber(String faxNumber){
		return faxNumber.replaceAll("\\s|\\.", "");
	}
}
