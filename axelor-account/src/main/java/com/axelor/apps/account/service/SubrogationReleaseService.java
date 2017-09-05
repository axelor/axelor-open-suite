package com.axelor.apps.account.service;

import java.io.IOException;
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;

public interface SubrogationReleaseService {

	List<Invoice> retrieveInvoices(Company company);

	void transmitRelease(SubrogationRelease subrogationRelease);

	String printToPDF(SubrogationRelease subrogationRelease, String name) throws AxelorException;

	String exportToCSV(SubrogationRelease subrogationRelease) throws AxelorException, IOException;

	void accountRelease(SubrogationRelease subrogationRelease);

}
