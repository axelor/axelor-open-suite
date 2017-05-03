package com.axelor.apps.businessproject.service;

import java.util.Arrays;
import java.util.List;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceServiceImpl;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class InvoiceServiceProjectImpl extends InvoiceServiceImpl {

	@Inject
	public InvoiceServiceProjectImpl(ValidateFactory validateFactory, VentilateFactory ventilateFactory,
			CancelFactory cancelFactory, AlarmEngineService<Invoice> alarmEngineService, InvoiceRepository invoiceRepo,
			AppAccountService appAccountService) {
		super(validateFactory, ventilateFactory, cancelFactory, alarmEngineService, invoiceRepo, appAccountService);
	}
	
	
	
	public List<String> editInvoiceAnnex(Invoice invoice, String invoiceIds, boolean toAttach) throws AxelorException{
		
		if (!AuthUtils.getUser().getActiveCompany().getAccountConfig().getDisplayTimesheetOnPrinting() && !AuthUtils.getUser().getActiveCompany().getAccountConfig().getDisplayExpenseOnPrinting()) { return null; }
		
		String language;
		try {
			language = invoice.getPartner().getLanguageSelect() != null? invoice.getPartner().getLanguageSelect() : invoice.getCompany().getPrintingSettings().getLanguageSelect() != null ? invoice.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		} catch (NullPointerException e) {
			language = "en";
		}

		String title = I18n.get("Invoice");
		if(invoice.getInvoiceId() != null) {
			title += invoice.getInvoiceId();
		}
		
		Integer invoicesCopy = invoice.getInvoicesCopySelect();	
		ReportSettings rS = ReportFactory.createReport(IReport.INVOICE_ANNEX, title + "-" + I18n.get("Annex") + "-${date}");
		
		if (toAttach) {
			rS.toAttach(invoice);
		}
		
		String fileLink = rS.addParam("InvoiceId", invoiceIds)
				.addParam("Locale", language)
				.addParam("InvoicesCopy", invoicesCopy)
				.generate()
				.getFileLink();
		
		List<String> res = Arrays.asList(title, fileLink);
		
		return res;
	}

	
}
