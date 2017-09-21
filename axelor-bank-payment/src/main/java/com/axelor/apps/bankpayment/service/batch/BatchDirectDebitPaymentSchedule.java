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
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BatchDirectDebitPaymentSchedule extends BatchDirectDebit {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void process() {
		processPaymentScheduleLines(PaymentScheduleRepository.TYPE_TERMS);
	}

	protected void processPaymentScheduleLines(int paymentScheduleType) {
		AccountingBatch accountingBatch = batch.getAccountingBatch();
		List<String> filterList = new ArrayList<>();
		List<Pair<String, Object>> bindingList = new ArrayList<>();

		filterList.add("self.paymentSchedule.typeSelect = :paymentScheduleTypSelect");
		bindingList.add(Pair.of("paymentScheduleTypSelect", (Object) paymentScheduleType));

		filterList.add("self.statusSelect = :statusSelect");
		bindingList.add(Pair.of("statusSelect", (Object) PaymentScheduleLineRepository.STATUS_IN_PROGRESS));

		if (accountingBatch.getDueDate() != null) {
			filterList.add("self.scheduleDate <= :dueDate");
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

	}

	private List<PaymentScheduleLine> processQuery(List<String> filterList, List<Pair<String, Object>> bindingList) {
		List<PaymentScheduleLine> doneList = new ArrayList<>();

		List<Long> anomalyList = Lists.newArrayList(0L);
		filterList.add("self.id NOT IN (:anomalyList)");
		bindingList.add(Pair.of("anomalyList", (Object) anomalyList));

		String filter = Joiner.on(" AND ").join(Lists.transform(filterList, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return String.format("(%s)", input);
			}
		}));

		Query<PaymentScheduleLine> query = Beans.get(PaymentScheduleLineRepository.class).all().filter(filter);

		for (Pair<String, Object> binding : bindingList) {
			query.bind(binding.getLeft(), binding.getRight());
		}

		Set<Long> treatedSet = new HashSet<>();
		List<PaymentScheduleLine> paymentScheduleLineList;
		BankDetailsRepository bankDetailsRepo = Beans.get(BankDetailsRepository.class);
		BankDetails bankDetails = batch.getAccountingBatch().getBankDetails();

		while (!(paymentScheduleLineList = query.fetch(FETCH_LIMIT)).isEmpty()) {
			if (!JPA.em().contains(bankDetails)) {
				bankDetails = bankDetailsRepo.find(bankDetails.getId());
			}

			for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
				if (treatedSet.contains(paymentScheduleLine.getId())) {
					throw new IllegalArgumentException("Payment generation error");
				}

				treatedSet.add(paymentScheduleLine.getId());

				try {
					
					doneList.add(paymentScheduleLine);
					incrementDone();
				} catch (Exception e) {
					incrementAnomaly();
					anomalyList.add(paymentScheduleLine.getId());
					query.bind("anomalyList", anomalyList);
					TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
					LOG.error(e.getMessage());
				}
			}

			JPA.clear();
		}

		return doneList;
	}

}
