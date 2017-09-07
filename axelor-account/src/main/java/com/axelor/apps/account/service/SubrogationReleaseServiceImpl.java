package com.axelor.apps.account.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.persist.Transactional;

public class SubrogationReleaseServiceImpl implements SubrogationReleaseService {

	@Override
	public List<Invoice> retrieveInvoices(Company company) {
		Query<Invoice> query = Beans.get(InvoiceRepository.class).all()
				.filter("self.company = :company AND self.partner.factorizedCustomer = TRUE "
						+ "AND self.statusSelect = :statusSelect "
						+ "AND self.amountRemaining > 0 AND self.hasPendingPayments = FALSE")
				.order("invoiceDate").order("dueDate").order("invoiceId");
		query.bind("company", company);
		query.bind("statusSelect", InvoiceRepository.STATUS_VENTILATED);
		List<Invoice> invoiceList = query.fetch();
		return invoiceList;
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void transmitRelease(SubrogationRelease subrogationRelease) {
		SequenceService sequenceService = Beans.get(SequenceService.class);
		String sequenceNumber = sequenceService.getSequenceNumber("subrogationRelease",
				subrogationRelease.getCompany());
		subrogationRelease.setSequenceNumber(sequenceNumber);
		subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_TRANSMITTED);
	}

	@Override
	public String printToPDF(SubrogationRelease subrogationRelease, String name) throws AxelorException {
		ReportSettings reportSettings = ReportFactory.createReport(IReport.SUBROGATION_RELEASE, name);
		reportSettings.addParam("SubrogationReleaseId", subrogationRelease.getId());
		reportSettings.addParam("Locale", AuthUtils.getUser().getLanguage());
		reportSettings.addFormat("pdf");
		reportSettings.toAttach(subrogationRelease);
		reportSettings.generate();
		return reportSettings.getFileLink();
	}

	@Override
	public String exportToCSV(SubrogationRelease subrogationRelease) throws AxelorException, IOException {
		List<String[]> allMoveLineData = new ArrayList<>();

		Comparator<Invoice> byInvoiceDate = (i1, i2) -> i1.getInvoiceDate().compareTo(i2.getInvoiceDate());
		Comparator<Invoice> byDueDate = (i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate());
		Comparator<Invoice> byInvoiceId = (i1, i2) -> i1.getInvoiceId().compareTo(i2.getInvoiceId());

		List<Invoice> releaseDetails = subrogationRelease.getInvoiceSet().stream()
				.sorted(byInvoiceDate.thenComparing(byDueDate).thenComparing(byInvoiceId)).collect(Collectors.toList());

		for (Invoice invoice : releaseDetails) {
			String[] items = new String[6];
			BigDecimal inTaxTotal = invoice.getInTaxTotal().abs();

			if (InvoiceToolService.isOutPayment(invoice)) {
				inTaxTotal = inTaxTotal.negate();
			}

			items[0] = invoice.getPartner().getPartnerSeq();
			items[1] = invoice.getInvoiceId();
			items[2] = invoice.getInvoiceDate().toString();
			items[3] = invoice.getDueDate().toString();
			items[4] = inTaxTotal.toString();
			items[5] = invoice.getCurrency().getCode();
			allMoveLineData.add(items);
		}

		AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
		String filePath = accountConfigService
				.getExportPath(accountConfigService.getAccountConfig(subrogationRelease.getCompany()));
		String fileName = String.format("%s %s.csv", I18n.get("Subrogation release"),
				subrogationRelease.getSequenceNumber());
		Files.createDirectories(Paths.get(filePath));
		Path path = Paths.get(filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|', null, allMoveLineData);

		try (InputStream is = new FileInputStream(path.toFile())) {
			Beans.get(MetaFiles.class).attach(is, fileName, subrogationRelease);
		}

		return path.toString();
	}

	@Override
	public void accountRelease(SubrogationRelease subrogationRelease) {

		subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_ACCOUNTED);
	}

}
