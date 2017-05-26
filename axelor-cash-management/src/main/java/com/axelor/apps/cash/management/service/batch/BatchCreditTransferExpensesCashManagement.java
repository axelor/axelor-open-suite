package com.axelor.apps.cash.management.service.batch;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.batch.BatchCreditTransferExpenses;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class BatchCreditTransferExpensesCashManagement extends BatchCreditTransferExpenses {

	protected static final int FETCH_LIMIT = 10;

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final ExpenseRepository expenseRepo;
	protected final ExpenseService expenseService;

	@Inject
	public BatchCreditTransferExpensesCashManagement(ExpenseRepository expenseRepo, ExpenseService expenseService) {
		this.expenseRepo = expenseRepo;
		this.expenseService = expenseService;
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

		Query<Expense> query = expenseRepo.all()
				.filter("self.ventilated = true "
						+ "AND self.paymentStatusSelect = :paymentStatusSelect "
						+ "AND self.company = :company "
						+ "AND self.bankDetails IN (:bankDetailsSet) "
						+ "AND self.user.partner.outPaymentMode = :paymentMode "
						+ "AND self.id NOT IN (:anomalyList)")
				.bind("paymentStatusSelect", InvoicePaymentRepository.STATUS_DRAFT)
				.bind("company", accountingBatch.getCompany())
				.bind("bankDetailsSet", bankDetailsSet)
				.bind("paymentMode", accountingBatch.getPaymentMode())
				.bind("anomalyList", anomalyList);

		for (List<Expense> expenseList; !(expenseList = query.fetch(FETCH_LIMIT)).isEmpty(); JPA.clear()) {
			for (Expense expense : expenseList) {
				try {
					addPayment(expense);
					incrementDone();
				} catch (Exception ex) {
					incrementAnomaly();
					anomalyList.add(expense.getId());
					query = query.bind("anomalyList", anomalyList);
					TraceBackService.trace(ex);
					log.error(String.format("Credit transfer batch: anomaly for expense %s", expense.getExpenseSeq()));
				}
			}
		}

	}

	@Override
	protected void stop() {
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSES_REPORT_TITLE));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSES_DONE_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSES_DONE_PLURAL, batch.getDone()),
				batch.getDone()));
		sb.append(String.format(
				I18n.get(IExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSES_ANOMALY_SINGULAR,
						IExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSES_ANOMALY_PLURAL, batch.getAnomaly()),
				batch.getAnomaly()));
		addComment(sb.toString());
		super.stop();
	}

	private void addPayment(Expense expense) throws AxelorException {
		log.debug(String.format("Credit transfer batch: adding payment for expense %s", expense.getExpenseSeq()));
		expenseService.addPayment(expense);
	}

}
