/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.IExpense;
import com.axelor.apps.hr.db.KilometricAllowance;
import com.axelor.apps.hr.db.KilometricAllowanceRate;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.KilometricAllowanceRateRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExpenseService  {

	protected MoveService moveService;
	protected ExpenseRepository expenseRepository;
	protected MoveLineService moveLineService;
	protected AccountManagementServiceAccountImpl accountManagementService;
	protected GeneralService generalService;
	protected AccountConfigHRService accountConfigService;
	protected AnalyticDistributionLineService analyticDistributionLineService;
	
	@Inject
	public ExpenseService(MoveService moveService, ExpenseRepository expenseRepository, MoveLineService moveLineService,
			AccountManagementServiceAccountImpl accountManagementService, GeneralService generalService,
			AccountConfigHRService accountConfigService, AnalyticDistributionLineService analyticDistributionLineService)  {
		
		this.moveService = moveService;
		this.expenseRepository = expenseRepository;
		this.moveLineService = moveLineService;
		this.accountManagementService = accountManagementService;
		this.generalService = generalService;
		this.accountConfigService = accountConfigService;
		this.analyticDistributionLineService = analyticDistributionLineService;
	}
	
	public ExpenseLine createAnalyticDistributionWithTemplate(ExpenseLine expenseLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = null;
		analyticDistributionLineList = analyticDistributionLineService.generateLinesWithTemplate(expenseLine.getAnalyticDistributionTemplate(), expenseLine.getUntaxedAmount());
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setExpenseLine(expenseLine);
			}
		}
		expenseLine.setAnalyticDistributionLineList(analyticDistributionLineList);
		return expenseLine;
	}
	
	public ExpenseLine computeAnalyticDistribution(ExpenseLine expenseLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = expenseLine.getAnalyticDistributionLineList();
		if((analyticDistributionLineList == null || analyticDistributionLineList.isEmpty()) && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			analyticDistributionLineList = analyticDistributionLineService.generateLines(expenseLine.getUser().getPartner(), expenseLine.getExpenseType(), expenseLine.getExpense().getCompany(), expenseLine.getUntaxedAmount());
			if(analyticDistributionLineList != null){
				for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
					analyticDistributionLine.setExpenseLine(expenseLine);
					analyticDistributionLine.setAmount(
							analyticDistributionLine.getPercentage().multiply(analyticDistributionLine.getExpenseLine().getUntaxedAmount()
							.divide(new BigDecimal(100),2,RoundingMode.HALF_UP)));
					analyticDistributionLine.setDate(generalService.getTodayDate());
				}
				expenseLine.setAnalyticDistributionLineList(analyticDistributionLineList);
			}
		}
		if(analyticDistributionLineList != null && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setExpenseLine(expenseLine);
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
		return expenseLine;
	}
	
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

		LocalDate moveDate = generalService.getTodayDate();
		if(expense.getMoveDate()!=null){
			moveDate = expense.getMoveDate();
		}

		Account account = null;
		AccountConfig accountConfig= accountConfigService.getAccountConfig(expense.getCompany());

		if(expense.getUser().getPartner() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.USER_PARTNER),expense.getUser().getName()), IException.CONFIGURATION_ERROR);
		}

		Move move = moveService.getMoveCreateService().createMove(accountConfigService.getExpenseJournal(accountConfig), accountConfig.getCompany(), null, expense.getUser().getPartner(), moveDate, expense.getUser().getPartner().getPaymentMode());

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

			exTaxTotal = expenseLine.getUntaxedAmount();
			MoveLine moveLine = moveLineService.createMoveLine(move, expense.getUser().getPartner(), account, exTaxTotal, true, moveDate, moveDate, moveLineId++, "");
			for (AnalyticDistributionLine analyticDistributionLineIt : expenseLine.getAnalyticDistributionLineList()) {
				AnalyticDistributionLine analyticDistributionLine = Beans.get(AnalyticDistributionLineRepository.class).copy(analyticDistributionLineIt, false);
				analyticDistributionLine.setExpenseLine(null);
				moveLine.addAnalyticDistributionLineListItem(analyticDistributionLine);
			}
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

		moveService.getMoveValidateService().validateMove(move);

		expense.setMove(move);
		expense.setVentilated(true);
		expenseRepository.save(expense);

		return move;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel (Expense expense) throws AxelorException{
		Move move = expense.getMove();
		if(move == null)
		{
			expense.setStatusSelect(IExpense.STATUS_CANCELED);
			expenseRepository.save(expense);
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

		expenseRepository.save(expense);
	}

	public List<InvoiceLine> createInvoiceLines(Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException  {

		List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
		int count = 0;
		for(ExpenseLine expenseLine : expenseLineList)  {

			invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine, priority*100+count));
			count++;
			expenseLine.setInvoiced(true);

		}

		return invoiceLineList;

	}

	public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine, int priority) throws AxelorException  {

		Product product = expenseLine.getExpenseType();
		InvoiceLineGenerator invoiceLineGenerator = null;
		Integer atiChoice = invoice.getCompany().getAccountConfig().getInvoiceInAtiSelect();
		if(atiChoice == 1 || atiChoice == 3){
			invoiceLineGenerator = new InvoiceLineGenerator(invoice, product, product.getName(), expenseLine.getUntaxedAmount(),
					expenseLine.getUntaxedAmount(),expenseLine.getComments(),BigDecimal.ONE,product.getUnit(), null,priority,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					expenseLine.getUntaxedAmount(), expenseLine.getTotalAmount(),false)  {

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
					expenseLine.getTotalAmount(),expenseLine.getComments(),BigDecimal.ONE,product.getUnit(), null,priority,BigDecimal.ZERO,IPriceListLine.AMOUNT_TYPE_NONE,
					expenseLine.getUntaxedAmount(), expenseLine.getTotalAmount(),false)  {

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
	
	public void getExpensesTypes(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			List<Product> productList = Beans.get(ProductRepository.class).all().filter("self.expense = true").fetch();
			for (Product product : productList) {
				Map<String, String> map = new HashMap<String,String>();
				map.put("name", product.getName());
				map.put("id", product.getId().toString());
				dataList.add(map);
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}
	
	@Transactional
	public void insertExpenseLine(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		ProjectTask projectTask = Beans.get(ProjectTaskRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("expenseType").toString()));
		if(user != null){
			Expense expense = Beans.get(ExpenseRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(expense == null){
				expense = new Expense();
				expense.setUser(user);
				expense.setCompany(user.getActiveCompany());
				expense.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
			}
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setExpenseDate(new LocalDate(request.getData().get("date").toString()));
			expenseLine.setComments(request.getData().get("comments").toString());
			expenseLine.setExpenseType(product);
			expenseLine.setToInvoice(new Boolean(request.getData().get("toInvoice").toString()));
			expenseLine.setProjectTask(projectTask);
			expenseLine.setUser(user);
			expenseLine.setUntaxedAmount(new BigDecimal(request.getData().get("amountWithoutVat").toString()));
			expenseLine.setTotalTax(new BigDecimal(request.getData().get("vatAmount").toString()));
			expenseLine.setTotalAmount(expenseLine.getUntaxedAmount().add(expenseLine.getTotalTax()));
			expenseLine.setJustification((byte[])request.getData().get("justification"));
			expense.addExpenseLineListItem(expenseLine);
			
			Beans.get(ExpenseRepository.class).save(expense);
		}
	}
	
	@Transactional
	public void insertKMExpenses(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		if(user != null){
			Expense expense = Beans.get(ExpenseRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(expense == null){
				expense = new Expense();
				expense.setUser(user);
				expense.setCompany(user.getActiveCompany());
				expense.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
			}
			KilometricAllowance kmAllowance = new KilometricAllowance();
			kmAllowance.setDistance(new BigDecimal(request.getData().get("kmNumber").toString()));
			kmAllowance.setCityFrom(request.getData().get("locationFrom").toString());
			kmAllowance.setCityTo(request.getData().get("locationTo").toString());
			kmAllowance.setTypeSelect(new Integer(request.getData().get("allowanceTypeSelect").toString()));
			kmAllowance.setReason(request.getData().get("comments").toString());
			kmAllowance.setDate(new LocalDate(request.getData().get("date").toString()));
			if(user.getEmployee() != null && user.getEmployee().getKilometricAllowParam() != null){
				kmAllowance.setKilometricAllowParam(user.getEmployee().getKilometricAllowParam());
				KilometricAllowanceRate kilometricAllowanceRate = Beans.get(KilometricAllowanceRateRepository.class).findByVehicleKillometricAllowanceParam(user.getEmployee().getKilometricAllowParam());
				if(kilometricAllowanceRate != null){
					BigDecimal rate = kilometricAllowanceRate.getRate();
					if(rate != null){
						kmAllowance.setInTaxTotal(rate.multiply(kmAllowance.getDistance()));
					}
				}
			}
			
			expense.addKilometricAllowanceListItem(kmAllowance);
			
			Beans.get(ExpenseRepository.class).save(expense);
		}
	}
}
