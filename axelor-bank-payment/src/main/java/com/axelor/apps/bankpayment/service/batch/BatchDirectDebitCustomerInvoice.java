/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.batch;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BatchDirectDebitCustomerInvoice extends BatchDirectDebit {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void process() {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		List<String> filterList = new ArrayList<>();
		List<Pair<String, Object>> bindingList = new ArrayList<>();

		filterList.add("self.operationTypeSelect = :operationTypeSelect");
		bindingList.add(Pair.of("operationTypeSelect", (Object) InvoiceRepository.OPERATION_TYPE_CLIENT_SALE));

		filterList.add("self.statusSelect = :statusSelect");
		bindingList.add(Pair.of("statusSelect", (Object) InvoiceRepository.STATUS_VENTILATED));

		filterList.add("self.amountRemaining > 0");
		filterList.add("self.hasPendingPayments = FALSE");

		if (accountingBatch.getDueDate() != null) {
			filterList.add("self.dueDate <= :dueDate");
			bindingList.add(Pair.of("dueDate", (Object) accountingBatch.getDueDate()));
		}

		if (accountingBatch.getCompany() != null) {
			filterList.add("self.company = :company");
			bindingList.add(Pair.of("company", (Object) accountingBatch.getCompany()));
		}

		if (accountingBatch.getBankDetails() != null) {
			Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

			if (accountingBatch.getIncludeOtherBankAccounts() && appBaseService.getAppBase().getManageMultiBanks()) {
				bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
			}

			filterList.add("self.companyBankDetails IN (:bankDetailsSet)");
			bindingList.add(Pair.of("bankDetailsSet", (Object) bankDetailsSet));
		}

		if (accountingBatch.getPaymentMode() != null) {
			filterList.add("self.paymentMode = :paymentMode");
			bindingList.add(Pair.of("paymentMode", (Object) accountingBatch.getPaymentMode()));
		}

		List<InvoicePayment> invoicePaymentList = processQuery(filterList, bindingList);

		if (!invoicePaymentList.isEmpty()) {
			try {
				Beans.get(BankOrderMergeService.class).mergeFromInvoicePayments(invoicePaymentList);
			} catch (AxelorException e) {
				TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
				LOG.error(e.getMessage());
			}
		}

	}

	private List<InvoicePayment> processQuery(List<String> filterList, List<Pair<String, Object>> bindingList) {

		List<InvoicePayment> doneList = new ArrayList<>();

		List<Long> anomalyList = Lists.newArrayList(0L);
		filterList.add("self.id NOT IN (:anomalyList)");
		bindingList.add(Pair.of("anomalyList", (Object) anomalyList));

		String filter = Joiner.on(" AND ").join(Lists.transform(filterList, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return String.format("(%s)", input);
			}
		}));

		Query<Invoice> query = Beans.get(InvoiceRepository.class).all().filter(filter);

		for (Pair<String, Object> binding : bindingList) {
			query.bind(binding.getLeft(), binding.getRight());
		}

		Set<Long> treatedSet = new HashSet<>();
		List<Invoice> invoiceList;
		InvoicePaymentCreateService invoicePaymentCreateService = Beans.get(InvoicePaymentCreateService.class);
		BankDetailsRepository bankDetailsRepo = Beans.get(BankDetailsRepository.class);
		BankDetails companyBankDetails = getCompanyBankDetails(batch.getAccountingBatch());

		while (!(invoiceList = query.fetch(FETCH_LIMIT)).isEmpty()) {
			if (!JPA.em().contains(companyBankDetails)) {
				companyBankDetails = bankDetailsRepo.find(companyBankDetails.getId());
			}

			for (Invoice invoice : invoiceList) {
				if (treatedSet.contains(invoice.getId())) {
					throw new IllegalArgumentException("Invoice payment generation error");
				}

				treatedSet.add(invoice.getId());

				try {
					doneList.add(invoicePaymentCreateService.createInvoicePayment(invoice, companyBankDetails));
					incrementDone();
				} catch (Exception e) {
					incrementAnomaly();
					anomalyList.add(invoice.getId());
					query.bind("anomalyList", anomalyList);
					TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
					LOG.error(e.getMessage());
					break;
				}
			}

			JPA.clear();
		}

		return doneList;
	}

}
