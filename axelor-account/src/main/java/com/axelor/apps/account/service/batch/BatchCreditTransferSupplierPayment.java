package com.axelor.apps.account.service.batch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BatchCreditTransferSupplierPayment extends BatchStrategy {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final InvoiceRepository invoiceRepo;
	protected final InvoiceService invoiceService;
	protected final InvoicePaymentValidateService invoicePaymentValidateService;
	protected final GeneralService generalService;

	@Inject
	public BatchCreditTransferSupplierPayment(InvoiceRepository invoiceRepo, InvoiceService invoiceService,
			InvoicePaymentValidateService invoicePaymentValidateService, GeneralService generalService) {
		this.invoiceRepo = invoiceRepo;
		this.invoiceService = invoiceService;
		this.invoicePaymentValidateService = invoicePaymentValidateService;
		this.generalService = generalService;
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		super.start();
	}

	@Override
	protected void process() {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		List<Long> anomalyList = Lists.newArrayList(0L);	// Can't pass an empty collection to the query
		Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

		if (accountingBatch.getIncludeOtherBankAccounts()) {
			bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
		}

		Query<Invoice> query = invoiceRepo.all()
				.filter("self.operationTypeSelect = :operationTypeSelect "
						+ "AND (self.statusSelect = :statusSelectValidated "
						+ "OR self.statusSelect = :statusSelectVentilated AND self.companyInTaxTotalRemaining > 0) "
						+ "AND self.company = :company "
						+ "AND self.companyBankDetails IN (:bankDetailsSet) "
						+ "AND self.dueDate <= :dueDate "
						+ "AND self.currency = :currency "
						+ "AND self.paymentMode = :paymentMode "
						+ "AND self.id NOT IN (:anomalyList)")
				.bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
				.bind("statusSelectValidated", InvoiceRepository.STATUS_VALIDATED)
				.bind("statusSelectVentilated", InvoiceRepository.STATUS_VENTILATED)
				.bind("company", accountingBatch.getCompany())
				.bind("bankDetailsSet", bankDetailsSet)
				.bind("dueDate", accountingBatch.getDueDate())
				.bind("currency", accountingBatch.getCurrency())
				.bind("paymentMode", accountingBatch.getPaymentMode())
				.bind("anomalyList", anomalyList);

		for (List<Invoice> invoiceList; !(invoiceList = query.fetch(FETCH_LIMIT)).isEmpty(); JPA.clear()) {
			for (Invoice invoice : invoiceList) {
				try {
					addPayment(invoice);
					incrementDone();
				} catch (Exception ex) {
					incrementAnomaly();
					anomalyList.add(invoice.getId());
					query = query.bind("anomalyList", anomalyList);
					TraceBackService.trace(ex);
					ex.printStackTrace();
					log.error(String.format("Credit transfer batch for supplier payment: anomaly for invoice %s",
							invoice.getInvoiceId()));
				}
			}
		}

	}

	@Override
	protected void stop() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_REPORT_TITLE));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_INVOICE_DONE_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_INVOICE_DONE_PLURAL, batch.getDone()),
				batch.getDone()));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_ANOMALY_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_ANOMALY_PLURAL, batch.getAnomaly()),
				batch.getAnomaly()));
		addComment(sb.toString());
		super.stop();
	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	private void addPayment(Invoice invoice)
			throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

		log.debug(String.format("Credit transfer batch for supplier payment: adding payment for invoice %s",
				invoice.getInvoiceId()));

		switch (invoice.getStatusSelect()) {
		case InvoiceRepository.STATUS_VALIDATED:
			invoiceService.ventilate(invoice);
		case InvoiceRepository.STATUS_VENTILATED:
			InvoicePayment invoicePayment = new InvoicePayment();
			invoicePayment.setAmount(invoice.getInTaxTotal().subtract(invoice.getAmountPaid()));
			invoicePayment.setPaymentDate(generalService.getTodayDate());
			invoicePayment.setCurrency(invoice.getCurrency());
			invoicePayment.setPaymentMode(invoice.getPaymentMode());
			invoicePayment.setInvoice(invoice);
			invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_PAYMENT);
			invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_DRAFT);
			invoicePayment.setBankDetails(invoice.getCompanyBankDetails());
			invoicePaymentValidateService.validate(invoicePayment);
		}
	}

}
