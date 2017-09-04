package com.axelor.apps.account.service;

import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SubrogationReleaseServiceImpl implements SubrogationReleaseService {

	private InvoiceRepository invoiceRepo;

	@Inject
	public SubrogationReleaseServiceImpl(InvoiceRepository invoiceRepo) {
		this.invoiceRepo = invoiceRepo;
	}

	@Override
	public List<Invoice> retrieveInvoices(Company company) {
		Query<Invoice> query = invoiceRepo.all()
				.filter("self.company = :company AND self.partner.factorizedCustomer = TRUE "
						+ "AND self.statusSelect = :statusSelect "
						+ "AND self.amountRemaining > 0 AND self.hasPendingPayments = FALSE");
		query.bind("company", company);
		query.bind("statusSelect", InvoiceRepository.STATUS_VENTILATED);
		List<Invoice> invoiceList = query.fetch();
		return invoiceList;
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void transmitRelease(SubrogationRelease subrogationRelease) {
		// TODO: assign sequence
		subrogationRelease.setSequence("CITEL17092C1001");
		subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_TRANSMITTED);
	}

	@Override
	public String printToPDF(SubrogationRelease subrogationRelease) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportToCSV(SubrogationRelease subrogationRelease) {
		// TODO Auto-generated method stub
		return null;
	}

}
