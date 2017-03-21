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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.codec.binary.Base64;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeAdvanceUsage;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExpenseServiceImpl implements ExpenseService  {

	protected MoveService moveService;
	protected ExpenseRepository expenseRepository;
	protected MoveLineService moveLineService;
	protected AccountManagementServiceAccountImpl accountManagementService;
	protected AppAccountService appAccountService;
	protected AccountConfigHRService accountConfigService;
	protected AnalyticMoveLineService analyticMoveLineService;
	protected HRConfigService  hrConfigService;
	protected TemplateMessageService  templateMessageService;
	
	@Inject
	public ExpenseServiceImpl(MoveService moveService, ExpenseRepository expenseRepository, MoveLineService moveLineService,
			AccountManagementServiceAccountImpl accountManagementService, AppAccountService appAccountService,
			AccountConfigHRService accountConfigService, AnalyticMoveLineService analyticMoveLineService,
			HRConfigService  hrConfigService, TemplateMessageService  templateMessageService)  {
		
		this.moveService = moveService;
		this.expenseRepository = expenseRepository;
		this.moveLineService = moveLineService;
		this.accountManagementService = accountManagementService;
		this.appAccountService = appAccountService;
		this.accountConfigService = accountConfigService;
		this.analyticMoveLineService = analyticMoveLineService;
		this.hrConfigService = hrConfigService;
		this.templateMessageService = templateMessageService;
		
	}
	
	public ExpenseLine computeAnalyticDistribution(ExpenseLine expenseLine) throws AxelorException{
		
		if(appAccountService.getAppAccount().getAnalyticDistributionTypeSelect() == AppAccountRepository.DISTRIBUTION_TYPE_FREE)  {  return expenseLine;  }
		
		Expense expense = expenseLine.getExpense();
		List<AnalyticMoveLine> analyticMoveLineList = expenseLine.getAnalyticMoveLineList();
		if((analyticMoveLineList == null || analyticMoveLineList.isEmpty()))  {
			analyticMoveLineList = analyticMoveLineService.generateLines(expenseLine.getUser().getPartner(), expenseLine.getExpenseProduct(), expense.getCompany(), expenseLine.getUntaxedAmount());
			expenseLine.setAnalyticMoveLineList(analyticMoveLineList);
		}
		if(analyticMoveLineList != null)  {
			for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
				this.updateAnalyticMoveLine(analyticMoveLine, expenseLine);
			}
		}
		return expenseLine;
	}
	
	public void updateAnalyticMoveLine(AnalyticMoveLine analyticMoveLine, ExpenseLine expenseLine)  {
		
		analyticMoveLine.setExpenseLine(expenseLine);
		analyticMoveLine.setAmount(
				analyticMoveLine.getPercentage().multiply(analyticMoveLine.getExpenseLine().getUntaxedAmount()
				.divide(new BigDecimal(100),2,RoundingMode.HALF_UP)));
		analyticMoveLine.setDate(appAccountService.getTodayDate());
		analyticMoveLine.setStatusSelect(AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE);
		
	}
	
	public ExpenseLine createAnalyticDistributionWithTemplate(ExpenseLine expenseLine) throws AxelorException{
		List<AnalyticMoveLine> analyticMoveLineList = null;
		analyticMoveLineList = analyticMoveLineService.generateLinesWithTemplate(expenseLine.getAnalyticDistributionTemplate(), expenseLine.getUntaxedAmount());
		if(analyticMoveLineList != null)  {
			for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList)  {
				analyticMoveLine.setExpenseLine(expenseLine);
			}
		}
		expenseLine.setAnalyticMoveLineList(analyticMoveLineList);
		return expenseLine;
	}
	
	
	
	
	public Expense compute (Expense expense){

		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal taxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		List<ExpenseLine> expenseLineList = expense.getExpenseLineList();
		
		if(expenseLineList != null)  {
			for (ExpenseLine expenseLine : expenseLineList) {
				exTaxTotal = exTaxTotal.add(expenseLine.getUntaxedAmount());
				taxTotal = taxTotal.add(expenseLine.getTotalTax());
				inTaxTotal = inTaxTotal.add(expenseLine.getTotalAmount());
			}
		}
		expense.setExTaxTotal(exTaxTotal);
		expense.setTaxTotal(taxTotal);
		expense.setInTaxTotal(inTaxTotal);
		return expense;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirm(Expense expense) throws AxelorException  {
				
		expense.setStatusSelect(ExpenseRepository.STATUS_CONFIRMED);
		expense.setSentDate(appAccountService.getTodayDate());
		
		expenseRepository.save(expense);
		
	}
	
	
	public Message sendConfirmationEmail(Expense expense) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
		
		if(hrConfig.getExpenseMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(expense, hrConfigService.getSentExpenseTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Expense expense) throws AxelorException  {
		
		if (expense.getUser().getEmployee() == null){
			throw new AxelorException( String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE), expense.getUser().getFullName())  , IException.CONFIGURATION_ERROR);
		}
		
		if (expense.getKilometricExpenseLineList() != null && !expense.getKilometricExpenseLineList().isEmpty()){
			for (ExpenseLine line : expense.getKilometricExpenseLineList()){
				BigDecimal amount = Beans.get(KilometricService.class).computeKilometricExpense(line, expense.getUser().getEmployee());
				line.setTotalAmount(amount);
				line.setUntaxedAmount(amount);
				
				Beans.get(KilometricService.class).updateKilometricLog(line, expense.getUser().getEmployee());
			}
			compute(expense);
		}
		
		Beans.get(EmployeeAdvanceService.class).fillExpenseWithAdvances(expense);
		expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
		expense.setValidatedBy(AuthUtils.getUser());
		expense.setValidationDate(appAccountService.getTodayDate());
		
		expenseRepository.save(expense);
		
	}
	
	
	public Message sendValidationEmail(Expense expense) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
		
		if(hrConfig.getExpenseMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(expense, hrConfigService.getValidatedExpenseTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void refuse(Expense expense) throws AxelorException  {
		
		expense.setStatusSelect(ExpenseRepository.STATUS_REFUSED);
		expense.setRefusedBy(AuthUtils.getUser());
		expense.setRefusalDate(appAccountService.getTodayDate());
		
		expenseRepository.save(expense);
		
	}
	
	public Message sendRefusalEmail(Expense expense) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, MessagingException, IOException  {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
		
		if(hrConfig.getExpenseMailNotification())  {
				
			return templateMessageService.generateAndSendMessage(expense, hrConfigService.getRefusedExpenseTemplate(hrConfig));
				
		}
		
		return null;
		
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move ventilate(Expense expense) throws AxelorException{

		LocalDate moveDate = expense.getMoveDate();
		
		if(moveDate == null){
			moveDate = appAccountService.getTodayDate();
		}

		Account account = null;
		AccountConfig accountConfig= accountConfigService.getAccountConfig(expense.getCompany());

		if(expense.getUser().getPartner() == null){
			throw new AxelorException(String.format(I18n.get(com.axelor.apps.account.exception.IExceptionMessage.USER_PARTNER),expense.getUser().getName()), IException.CONFIGURATION_ERROR);
		}

		Move move = moveService.getMoveCreateService().createMove(accountConfigService.getExpenseJournal(accountConfig), accountConfig.getCompany(), null, expense.getUser().getPartner(), moveDate, expense.getUser().getPartner().getPaymentMode(), MoveRepository.AUTOMATIC);

		List<MoveLine> moveLines = new ArrayList<MoveLine>();

		AccountManagement accountManagement = null;
		Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();
		BigDecimal exTaxTotal = null;

		int moveLineId = 1;
		int expenseLineId = 1;
		moveLines.add( moveLineService.createMoveLine(move, expense.getUser().getPartner(), accountConfigService.getExpenseEmployeeAccount(accountConfig), expense.getInTaxTotal(), false, moveDate, moveDate, moveLineId++, ""));

		for(ExpenseLine expenseLine : expense.getExpenseLineList()){
			analyticAccounts.clear();
			Product product = expenseLine.getExpenseProduct();
			accountManagement = accountManagementService.getAccountManagement(product, expense.getCompany());

			account = accountManagementService.getProductAccount(accountManagement, true);

			if(account == null)  {
				throw new AxelorException(String.format(I18n.get(com.axelor.apps.account.exception.IExceptionMessage.MOVE_LINE_4),
						 expenseLineId,expense.getCompany().getName()), IException.CONFIGURATION_ERROR);
			}

			exTaxTotal = expenseLine.getUntaxedAmount();
			MoveLine moveLine = moveLineService.createMoveLine(move, expense.getUser().getPartner(), account, exTaxTotal, true, moveDate, moveDate, moveLineId++, "");
			for (AnalyticMoveLine analyticDistributionLineIt : expenseLine.getAnalyticMoveLineList()) {
				AnalyticMoveLine analyticDistributionLine = Beans.get(AnalyticMoveLineRepository.class).copy(analyticDistributionLineIt, false);
				analyticDistributionLine.setExpenseLine(null);
				moveLine.addAnalyticMoveLineListItem(analyticDistributionLine);
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
			expense.setStatusSelect(ExpenseRepository.STATUS_CANCELED);
			expenseRepository.save(expense);
			return;
		}
		Beans.get(PeriodService.class).testOpenPeriod(move.getPeriod());
		try{
			Beans.get(MoveRepository.class).remove(move);
			expense.setMove(null);
			expense.setVentilated(false);
			expense.setStatusSelect(ExpenseRepository.STATUS_CANCELED);
		}
		catch(Exception e){
			throw new AxelorException(String.format(I18n.get(com.axelor.apps.account.exception.IExceptionMessage.EXPENSE_CANCEL_MOVE)), IException.CONFIGURATION_ERROR);
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

		Product product = expenseLine.getExpenseProduct();
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
			response.setTotal(dataList.size());
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}
	
	@Transactional
	public void insertExpenseLine(ActionRequest request, ActionResponse response){
		User user = AuthUtils.getUser();
		Project project = Beans.get(ProjectRepository.class).find(new Long(request.getData().get("project").toString()));
		Product product = Beans.get(ProductRepository.class).find(new Long(request.getData().get("expenseType").toString()));
		if(user != null){
			Expense expense = Beans.get(ExpenseRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
			if(expense == null){
				expense = new Expense();
				expense.setUser(user);
				expense.setCompany(user.getActiveCompany());
				expense.setStatusSelect(ExpenseRepository.STATUS_DRAFT);
			}
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setExpenseDate(LocalDate.parse(request.getData().get("date").toString(), DateTimeFormatter.ISO_DATE));
			expenseLine.setComments(request.getData().get("comments").toString());
			expenseLine.setExpenseProduct(product);
			expenseLine.setToInvoice(new Boolean(request.getData().get("toInvoice").toString()));
			expenseLine.setProject(project);
			expenseLine.setUser(user);
			expenseLine.setTotalAmount(new BigDecimal(request.getData().get("unTaxTotal").toString()));
			expenseLine.setTotalTax(new BigDecimal(request.getData().get("taxTotal").toString()));
			expenseLine.setUntaxedAmount(expenseLine.getTotalAmount().subtract(expenseLine.getTotalTax()));
			String justification  = (String) request.getData().get("justification");
			if (!Strings.isNullOrEmpty(justification)) {
				expenseLine.setJustification(Base64.decodeBase64(justification));
			}
			expense.addExpenseLineListItem(expenseLine);
			
			Beans.get(ExpenseRepository.class).save(expense);
			response.setTotal(1);
		}
	}
	
	public BigDecimal computePersonalExpenseAmount(Expense expense){
		
		BigDecimal personalExpenseAmount = new BigDecimal("0.00");
		
		if (expense.getExpenseLineList() != null && !expense.getExpenseLineList().isEmpty()){
			for (ExpenseLine expenseLine : expense.getExpenseLineList()) {
				if (expenseLine.getExpenseProduct() != null && expenseLine.getExpenseProduct().getPersonalExpense() ){
					personalExpenseAmount = personalExpenseAmount.add(expenseLine.getTotalAmount());
				}
			}
		}
		return personalExpenseAmount;
	}
	
	
	public BigDecimal computeAdvanceAmount(Expense expense){
		
		BigDecimal advanceAmount = new BigDecimal("0.00");
		
		if (expense.getEmployeeAdvanceUsageList() != null && !expense.getEmployeeAdvanceUsageList().isEmpty()){
			for (EmployeeAdvanceUsage advanceLine : expense.getEmployeeAdvanceUsageList() ) {
				advanceAmount = advanceAmount.add(advanceLine.getUsedAmount());
			}
		}
		
		return advanceAmount;
	}
	
	@Transactional
	public void insertKMExpenses(ActionRequest request, ActionResponse response) throws AxelorException{
	 	User user = AuthUtils.getUser();
	 	if(user != null){
	 		Expense expense = Beans.get(ExpenseRepository.class).all().filter("self.statusSelect = 1 AND self.user.id = ?1", user.getId()).order("-id").fetchOne();
	 		if(expense == null){
	 			expense = new Expense();
	 			expense.setUser(user);
	 			expense.setCompany(user.getActiveCompany());
	 			expense.setStatusSelect(ExpenseRepository.STATUS_DRAFT);
	 		}
	 		ExpenseLine expenseLine = new ExpenseLine();
	 		if (request.getData().get("project") != null) {
	 			ProjectRepository projectTaskRepo = Beans.get(ProjectRepository.class);
	 			expenseLine.setProject(projectTaskRepo.find(Long.parseLong(request.getData().get("project").toString())));
	 		}
	 		expenseLine.setDistance(new BigDecimal(request.getData().get("nbrKm").toString()));
	 		expenseLine.setFromCity(request.getData().get("cityFrom").toString());
	 		expenseLine.setToCity(request.getData().get("cityTo").toString());
	 		expenseLine.setKilometricTypeSelect(new Integer(request.getData().get("type").toString()));
	 		expenseLine.setComments(request.getData().get("comments").toString());
	 		expenseLine.setExpenseDate(LocalDate.parse(request.getData().get("date").toString(), DateTimeFormatter.ISO_DATE));
	 		expenseLine.setToInvoice(new Boolean(request.getData().get("toInvoice").toString()));
	 		expenseLine.setExpenseProduct(getKilometricExpenseProduct(expense));
	 		
	 		Employee employee = user.getEmployee();
	 		if(employee != null && employee.getKilometricAllowParam() != null)  {
	 			expenseLine.setKilometricAllowParam(user.getEmployee().getKilometricAllowParam());
	 			expenseLine.setTotalAmount(Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));
	 			expenseLine.setUntaxedAmount(expenseLine.getTotalAmount());
	 		}
	 		
	 		expense.addKilometricExpenseLineListItem(expenseLine);
	 		
	 		Beans.get(ExpenseRepository.class).save(expense);
	 		response.setTotal(1);
	 	}
	}
	
	public Product getKilometricExpenseProduct(Expense expense) throws AxelorException {
		
		HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
		Product expenseProduct = hrConfigService.getKilometricExpenseProduct(hrConfig);
		
		return expenseProduct;
	}
	
}
