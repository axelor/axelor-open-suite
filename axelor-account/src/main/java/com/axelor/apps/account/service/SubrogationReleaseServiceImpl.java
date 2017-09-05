package com.axelor.apps.account.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
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
	public String printToPDF(SubrogationRelease subrogationRelease, String name) throws AxelorException {
		return ReportFactory.createReport("SubrogationRelease", name)
				.addParam("SubrogationReleaseId", subrogationRelease.getId())
				.addParam("Locale", AuthUtils.getUser().getLanguage()).addFormat("pdf").toAttach(subrogationRelease)
				.generate().getFileLink();
	}

	@Override
	public String exportToCSV(SubrogationRelease subrogationRelease) throws AxelorException, IOException {
		List<String[]> allMoveLineData = new ArrayList<>();

		for (Invoice invoice : subrogationRelease.getReleaseDetails()) {
			String[] items = new String[5];
			items[0] = invoice.getInvoiceId();
			items[1] = invoice.getInvoiceDate().toString();
			items[2] = invoice.getDueDate().toString();
			items[3] = invoice.getInTaxTotal().toString();
			items[4] = invoice.getCurrency().getCode();
			allMoveLineData.add(items);
		}

		AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
		String filePath = accountConfigService
				.getExportPath(accountConfigService.getAccountConfig(subrogationRelease.getCompany()));
		String fileName = String.format("%s %s.csv", I18n.get("Subrogation release"), subrogationRelease.getSequence());
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
