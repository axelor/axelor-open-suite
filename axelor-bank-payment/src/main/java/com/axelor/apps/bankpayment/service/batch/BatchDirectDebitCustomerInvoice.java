package com.axelor.apps.bankpayment.service.batch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.db.Query;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BatchDirectDebitCustomerInvoice extends BatchDirectDebit {

	@Override
	protected void process() {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		Query<Invoice> query = Beans.get(InvoiceRepository.class).all();
		List<String> filterList = new ArrayList<>();

		filterList.add("self.operationTypeSelect = :operationTypeSelect");
		query.bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);

		filterList.add("self.statusSelect = :statusSelect");
		query.bind("statusSelect", InvoiceRepository.STATUS_VENTILATED);

		filterList.add("self.amountRemaining > 0");
		filterList.add("self.hasPendingPayments = FALSE");

		if (accountingBatch.getDueDate() != null) {
			filterList.add("self.dueDate <= :dueDate");
			query.bind("dueDate", accountingBatch.getDueDate());
		}

		if (accountingBatch.getCompany() != null) {
			filterList.add("self.company = :company");
			query.bind("company", accountingBatch.getCompany());
		}

		if (accountingBatch.getBankDetails() != null) {
			Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

			if (accountingBatch.getIncludeOtherBankAccounts() && generalService.getGeneral().getManageMultiBanks()) {
				bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
			}

			filterList.add("self.companyBankDetails IN (:bankDetailsSet)");
			query.bind("bankDetailsSet", bankDetailsSet);
		}

		if (accountingBatch.getPaymentMode() != null) {
			filterList.add("self.paymentMode = :paymentMode");
			query.bind("paymentMode", accountingBatch.getPaymentMode());
		}

		processQuery(query, filterList);
	}

	private void processQuery(Query<Invoice> query, List<String> filterList) {
		List<Long> anomalyList = Lists.newArrayList(0L);
		filterList.add("self.id NOT IN (:anomalyList)");
		query.bind("anomalyList", anomalyList);

		String filter = Joiner.on(" AND ").join(Lists.transform(filterList, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return String.format("(%s)", input);
			}
		}));

		query.filter(filter);
		Set<Long> treatedSet = new HashSet<>();
		List<Invoice> invoiceList;
		BankDetails bankDetails = batch.getAccountingBatch().getBankDetails();

		while (!(invoiceList = query.fetch(FETCH_LIMIT)).isEmpty()) {
			for (Invoice invoice : invoiceList) {
				if (treatedSet.contains(invoice.getId())) {
					throw new IllegalArgumentException("Invoice payment generation error");
				}

				treatedSet.add(invoice.getId());

				try {
					addPayment(invoice, bankDetails);
					incrementDone();
				} catch (Exception e) {
					incrementAnomaly();
					anomalyList.add(invoice.getId());
					query.bind("anomalyList", anomalyList);
					TraceBackService.trace(e, IException.INVOICE_ORIGIN, batch.getId());
					LOG.error(e.getMessage());
				}
			}
		}
	}

}
