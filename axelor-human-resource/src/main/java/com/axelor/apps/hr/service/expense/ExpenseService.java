package com.axelor.apps.hr.service.expense;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAccountManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.IExpense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExpenseService extends ExpenseRepository{

	@Inject
	private MoveService moveService;

	@Inject
	private MoveLineService moveLineService;

	@Inject
	private AccountManagementServiceAccountImpl accountManagementService;

	@Inject private AccountConfigHRService accountConfigService;

	public Expense compute (Expense expense){

		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal taxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		List<ExpenseLine> expenseLineList = expense.getExpenseLineList();
		for (ExpenseLine expenseLine : expenseLineList) {
			exTaxTotal = exTaxTotal.add(expenseLine.getUntaxedAmount());
			taxTotal = taxTotal.add(expenseLine.getTotalTax());
			inTaxTotal = inTaxTotal.add(expenseLine.getTotalAmount());
		}
		expense.setExTaxTotal(exTaxTotal);
		expense.setTaxTotal(taxTotal);
		expense.setInTaxTotal(inTaxTotal);
		return expense;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move ventilate(Expense expense) throws AxelorException{

		LocalDate moveDate = GeneralService.getTodayDate();
		if(expense.getMoveDate()!=null){
			moveDate = expense.getMoveDate();
		}

		Account account = null;
		AccountConfig accountConfig= accountConfigService.getAccountConfig(expense.getCompany());

		if(expense.getUser().getPartner() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.USER_PARTNER),expense.getUser().getName()), IException.CONFIGURATION_ERROR);
		}

		Move move = moveService.createMove(accountConfigService.getExpenseJournal(accountConfig), accountConfig.getCompany(), null, expense.getUser().getPartner(), moveDate, expense.getUser().getPartner().getPaymentMode());

		List<MoveLine> moveLines = new ArrayList<MoveLine>();

		AccountManagement accountManagement = null;
		Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();
		BigDecimal exTaxTotal = null;

		int moveLineId = 1;
		int expenseLineId = 1;
		moveLines.add( moveLineService.createMoveLine(move, expense.getUser().getPartner(), accountConfigService.getExpenseEmployeeAccount(accountConfig), expense.getInTaxTotal(), false, moveDate, moveDate, moveLineId++, ""));

		for(ExpenseLine expenseLine : expense.getExpenseLineList()){
			analyticAccounts.clear();
			Product product = expenseLine.getExpenseType();
			accountManagement = accountManagementService.getAccountManagement(product, expense.getCompany());

			account = accountManagementService.getProductAccount(accountManagement, true);

			if(account == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_LINE_4),
						 expenseLineId,expense.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}

			for (AnalyticAccountManagement analyticAccountManagement : accountManagement.getAnalyticAccountManagementList()){
				if(analyticAccountManagement.getAnalyticAccount() == null){
					throw new AxelorException(String.format(I18n.get(IExceptionMessage.MOVE_LINE_5),
							analyticAccountManagement.getAnalyticAxis().getName(),expenseLine.getExpenseType().getName(), expense.getCompany().getName()), IException.CONFIGURATION_ERROR);
				}
				analyticAccounts.add(analyticAccountManagement.getAnalyticAccount());
			}
			exTaxTotal = expenseLine.getUntaxedAmount();
			MoveLine moveLine = moveLineService.createMoveLine(move, expense.getUser().getPartner(), account, exTaxTotal, true, moveDate, moveDate, moveLineId++, "");
			moveLine.setAnalyticAccountSet(analyticAccounts);

			moveLines.add(moveLine);
			expenseLineId++;

		}

		moveLineService.consolidateMoveLines(moveLines);
		account = accountConfigService.getExpenseTaxAccount(accountConfig);
		BigDecimal taxTotal = BigDecimal.ZERO;
		for(ExpenseLine expenseLine : expense.getExpenseLineList()){
			exTaxTotal = expenseLine.getTotalTax();
			taxTotal = taxTotal.add(exTaxTotal);
		}

		MoveLine moveLine = moveLineService.createMoveLine(move, expense.getUser().getPartner(), account, taxTotal, true, moveDate, moveDate, moveLineId++, "");
		moveLines.add(moveLine);

		move.getMoveLineList().addAll(moveLines);

		moveService.validateMove(move);

		expense.setMove(move);
		expense.setVentilated(true);
		save(expense);

		return move;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel (Expense expense) throws AxelorException{
		Move move = expense.getMove();
		if(move == null)
		{
			expense.setStatusSelect(IExpense.STATUS_CANCELED);
			save(expense);
			return;
		}
		Beans.get(PeriodService.class).testOpenPeriod(move.getPeriod());
		try{
			Beans.get(MoveRepository.class).remove(move);
			expense.setMove(null);
			expense.setVentilated(false);
			expense.setStatusSelect(IExpense.STATUS_CANCELED);
		}
		catch(Exception e){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.EXPENSE_CANCEL_MOVE)), IException.CONFIGURATION_ERROR);
		}

		save(expense);
	}

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ExpenseLine> expenseLineList) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

		for(ExpenseLine expenseLine : expenseLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine));

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine) throws AxelorException  {

		Product product = expenseLine.getExpenseType();
		InvoiceLineGenerator invoiceLineGenerator = null;
		if(!invoice.getCompany().getAccountConfig().getInvoiceInAti()){
			invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), expenseLine.getUntaxedAmount(),
					null,BigDecimal.ONE,product.getUnit(),InvoiceLineGenerator.DEFAULT_SEQUENCE,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					null, null, false)  {

				@Override
				public List<InvoiceLine> creates() throws AxelorException {

					InvoiceLine invoiceLine = this.createInvoiceLine();

					List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
					invoiceLines.add(invoiceLine);

					return invoiceLines;
				}
			};
		}

		else{
			invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), expenseLine.getTotalAmount(),
					null,BigDecimal.ONE,product.getUnit(),10,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					null, null, false)  {

				@Override
				public List<InvoiceLine> creates() throws AxelorException {

					InvoiceLine invoiceLine = this.createInvoiceLine();

					List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
					invoiceLines.add(invoiceLine);

					return invoiceLines;
				}
			};
		}
		return invoiceLineGenerator.creates();
	}
}
