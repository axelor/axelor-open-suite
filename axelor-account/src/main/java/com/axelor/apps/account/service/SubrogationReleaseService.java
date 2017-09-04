package com.axelor.apps.account.service;

import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.base.db.Company;

public interface SubrogationReleaseService {

	List<Invoice> retrieveInvoices(Company company);
	
	void transmitRelease(SubrogationRelease subrogationRelease);
	
	String printToPDF(SubrogationRelease subrogationRelease);
	
	String exportToCSV(SubrogationRelease subrogationRelease);

}
