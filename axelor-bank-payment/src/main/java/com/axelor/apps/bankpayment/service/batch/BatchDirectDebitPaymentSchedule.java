package com.axelor.apps.bankpayment.service.batch;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.base.db.BankDetails;
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
import com.google.inject.persist.Transactional;

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

		filterList.add("self.paymentSchedule.statusSelect = :paymentScheduleStatusSelect");
		bindingList.add(Pair.of("paymentScheduleStatusSelect", PaymentScheduleRepository.STATUS_CONFIRMED));

		filterList.add("self.paymentSchedule.typeSelect = :paymentScheduleTypeSelect");
		bindingList.add(Pair.of("paymentScheduleTypeSelect", paymentScheduleType));

		filterList.add("self.statusSelect = :statusSelect");
		bindingList.add(Pair.of("statusSelect", PaymentScheduleLineRepository.STATUS_IN_PROGRESS));

		if (accountingBatch.getDueDate() != null) {
			filterList.add("self.scheduleDate <= :dueDate");
			bindingList.add(Pair.of("dueDate", accountingBatch.getDueDate()));
		}

		if (accountingBatch.getCompany() != null) {
			filterList.add("self.paymentSchedule.company = :company");
			bindingList.add(Pair.of("company", accountingBatch.getCompany()));
		}

		if (accountingBatch.getBankDetails() != null) {
			Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

			if (accountingBatch.getIncludeOtherBankAccounts() && appBaseService.getAppBase().getManageMultiBanks()) {
				bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsSet());
			}

			filterList.add("self.paymentSchedule.bankDetails IN (:bankDetailsSet)");
			bindingList.add(Pair.of("bankDetailsSet", bankDetailsSet));
		}

		if (accountingBatch.getPaymentMode() != null) {
			filterList.add("self.paymentSchedule.paymentMode = :paymentMode");
			bindingList.add(Pair.of("paymentMode", accountingBatch.getPaymentMode()));
		}

		List<PaymentScheduleLine> paymentScheduleLineList = processQuery(filterList, bindingList);

		try {
			createBankOrder(accountingBatch, paymentScheduleLineList);
		} catch (AxelorException e) {
			TraceBackService.trace(e, IException.DIRECT_DEBIT, batch.getId());
			LOG.error(e.getMessage());
		}

	}

	private List<PaymentScheduleLine> processQuery(List<String> filterList, List<Pair<String, Object>> bindingList) {
		List<PaymentScheduleLine> doneList = new ArrayList<>();

		List<Long> anomalyList = Lists.newArrayList(0L);
		filterList.add("self.id NOT IN (:anomalyList)");
		bindingList.add(Pair.of("anomalyList", anomalyList));

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
		PaymentScheduleLineService paymentScheduleLineService = Beans.get(PaymentScheduleLineService.class);

		while (!(paymentScheduleLineList = query.fetch(FETCH_LIMIT)).isEmpty()) {
			for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
				if (treatedSet.contains(paymentScheduleLine.getId())) {
					throw new IllegalArgumentException("Payment generation error");
				}

				treatedSet.add(paymentScheduleLine.getId());

				try {
					paymentScheduleLineService.createPaymentMove(paymentScheduleLine);
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

	/**
	 * Create a bank order for the specified list of payment schedule line.
	 * 
	 * @param accountingBatch
	 * @param paymentScheduleLineList
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	protected BankOrder createBankOrder(AccountingBatch accountingBatch,
			List<PaymentScheduleLine> paymentScheduleLineList) throws AxelorException {

		BankOrderCreateService bankOrderCreateService = Beans.get(BankOrderCreateService.class);
		BankOrderLineService bankOrderLineService = Beans.get(BankOrderLineService.class);
		BankOrderRepository bankOrderRepo = Beans.get(BankOrderRepository.class);
		PaymentScheduleLineRepository paymentScheduleLineRepo = Beans.get(PaymentScheduleLineRepository.class);

		if (!JPA.em().contains(accountingBatch)) {
			accountingBatch = Beans.get(AccountingBatchRepository.class).find(accountingBatch.getId());
		}

		LocalDate bankOrderDate = accountingBatch.getDueDate();

		BankOrder bankOrder = bankOrderCreateService.createBankOrder(accountingBatch.getPaymentMode(),
				BankOrderRepository.PARTNER_TYPE_CUSTOMER, bankOrderDate, accountingBatch.getCompany(),
				accountingBatch.getBankDetails(), accountingBatch.getCompany().getCurrency(), null, null);

		for (PaymentScheduleLine paymentScheduleLine : paymentScheduleLineList) {
			if (!JPA.em().contains(paymentScheduleLine)) {
				paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
			}

			PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();

			BankOrderLine bankOrderLine = bankOrderLineService.createBankOrderLine(
					accountingBatch.getPaymentMode().getBankOrderFileFormat(), accountingBatch.getCompany(),
					paymentSchedule.getPartner(), paymentSchedule.getBankDetails(),
					paymentScheduleLine.getInTaxAmountPaid(), accountingBatch.getCompany().getCurrency(), bankOrderDate,
					paymentScheduleLine.getDebitNumber(), paymentScheduleLine.getName());
			bankOrder.addBankOrderLineListItem(bankOrderLine);
		}

		return bankOrderRepo.save(bankOrder);
	}

}
