/**
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
package com.axelor.apps.hr.web.expense;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExpenseController {

	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	@Inject
	private Provider<HRMenuTagService> hrMenuTagServiceProvider;
	@Inject
	private Provider<HRConfigService> hrConfigServiceProvider;
	@Inject
	private Provider<ExpenseService> expenseServiceProvider;
	@Inject
	private Provider<GeneralService> generalServiceProvider;
	@Inject
	private Provider<ExpenseRepository> expenseRepositoryProvider;
	@Inject
	private Provider<ExpenseLineRepository> expenseLineRepositoryProvider;
	
	public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		Expense expense = expenseLine.getExpense();
		if(expense == null){
			expense = request.getContext().getParentContext().asType(Expense.class);
			expenseLine.setExpense(expense);
		}
		if(expenseLine.getAnalyticDistributionTemplate() != null){
			expenseLine = expenseServiceProvider.get().createAnalyticDistributionWithTemplate(expenseLine);
			response.setValue("analyticMoveLineList", expenseLine.getAnalyticMoveLineList());
		}
		else{
			throw new AxelorException(I18n.get("No template selected"), IException.CONFIGURATION_ERROR);
		}
	}
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) throws AxelorException{
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		Expense expense = expenseLine.getExpense();
		if(expense == null){
			expense = request.getContext().getParentContext().asType(Expense.class);
			expenseLine.setExpense(expense);
		}
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			expenseLine = expenseServiceProvider.get().computeAnalyticDistribution(expenseLine);
			response.setValue("analyticMoveLineList", expenseLine.getAnalyticMoveLineList());
		}
	}
	
	public void editExpense(ActionRequest request, ActionResponse response)  {
		
		User user = AuthUtils.getUser();
		Company activeCompany = user.getActiveCompany();
		
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1 AND (self.multipleUsers is false OR self.multipleUsers is null)", user, activeCompany).fetch();
		if(expenseList.isEmpty()){
			response.setView(ActionView
									.define(I18n.get("Expense"))
									.model(Expense.class.getName())
									.add("form", "expense-form")
									.map());
		}
		else if(expenseList.size() == 1){
			response.setView(ActionView
					.define(I18n.get("Expense"))
					.model(Expense.class.getName())
					.add("form", "expense-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(expenseList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define(I18n.get("Expense"))
					.model(Wizard.class.getName())
					.add("form", "popup-expense-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
			  		.param("popup-save", "false")
					.map());
		}
	}

	@SuppressWarnings("unchecked")
	public void editExpenseSelected(ActionRequest request, ActionResponse response){
		Map<String, Object> expenseMap = (Map<String, Object>) request.getContext().get("expenseSelect");
		Long expenseId = new Long((Integer) expenseMap.get("id"));
		response.setView(ActionView
				.define(I18n.get("Expense"))
				.model(Expense.class.getName())
				.add("form", "expense-form")
				.param("forceEdit", "true")
				.domain("self.id = " + expenseId)
				.context("_showRecord", expenseId).map());
	}

	public void validateExpense(ActionRequest request, ActionResponse response) throws AxelorException{
		
		User user = AuthUtils.getUser();
		Employee employee = user.getEmployee();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Expenses to Validate"))
				.model(Expense.class.getName())
				.add("grid","expense-validate-grid")
				.add("form","expense-form");

		Beans.get(HRMenuValidateService.class).createValidateDomain(user, employee, actionView);

		response.setView(actionView.map());
	}

	public void historicExpense(ActionRequest request, ActionResponse response){
		
		User user = AuthUtils.getUser();
		Employee employee = user.getEmployee();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Historic colleague Expenses"))
					.model(Expense.class.getName())
					.add("grid","expense-grid")
					.add("form","expense-form");

		actionView.domain("self.company = :_activeCompany AND (self.statusSelect = 3 OR self.statusSelect = 4)")
		.context("_activeCompany", user.getActiveCompany());
	
		if(employee == null || !employee.getHrManager())  {
			actionView.domain(actionView.get().getDomain() + " AND self.user.employee.manager = :_user")
			.context("_user", user);
		}
		
		response.setView(actionView.map());
	}


	public void showSubordinateExpenses(ActionRequest request, ActionResponse response){
		
		User user = AuthUtils.getUser();
		Company activeCompany = user.getActiveCompany();
		
		ActionViewBuilder actionView = ActionView.define(I18n.get("Expenses to be Validated by your subordinates"))
				   	.model(Expense.class.getName())
				   	.add("grid","expense-grid")
				   	.add("form","expense-form");
		
		String domain = "self.user.employee.manager.employee.manager = :_user AND self.company = :_activeCompany AND self.statusSelect = 2";

		long nbExpenses =  Query.of(ExtraHours.class).filter(domain).bind("_user", user).bind("_activeCompany", activeCompany).count();
		
		if(nbExpenses == 0)  {
			response.setNotify(I18n.get("No expense to be validated by your subordinates"));
		}
		else  {
			response.setView(actionView.domain(domain).context("_user", user).context("_activeCompany", activeCompany).map());
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response){
		Expense expense = request.getContext().asType(Expense.class);
		expense = expenseServiceProvider.get().compute(expense);
		response.setValues(expense);
	}

	public void ventilate(ActionRequest request, ActionResponse response) throws AxelorException {
		try {
			Expense expense = request.getContext().asType(Expense.class);
			expense = Beans.get(ExpenseRepository.class).find(expense.getId());
			Move move = expenseServiceProvider.get().ventilate(expense);
			response.setReload(true);
			response.setView(ActionView.define(I18n.get("Move"))
					   .model(Move.class.getName())
					   .add("grid","move-grid")
					   .add("form","move-form")
					   .context("_showRecord", String.valueOf(move.getId()))
					   .map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void validateDates(ActionRequest request, ActionResponse response) throws AxelorException{
		Expense expense = request.getContext().asType(Expense.class);
		if(expense.getExpenseLineList()!= null){
			List<ExpenseLine> expenseLineList = expense.getExpenseLineList();
			List<Integer> expenseLineId = new ArrayList<>();
			int compt = 0;
			for (ExpenseLine expenseLine : expenseLineList) {
				compt++;
				if(expenseLine.getExpenseDate().isAfter(generalServiceProvider.get().getTodayDate())){
					expenseLineId.add(compt);
				}
			}
			if(!expenseLineId.isEmpty()){
				String ids =  Joiner.on(",").join(expenseLineId);
				throw new AxelorException(String.format(I18n.get("Date problem for line(s) : "+ids)), IException.CONFIGURATION_ERROR);
			}
		}
	}
	
	public void printExpense(ActionRequest request, ActionResponse response) throws AxelorException {
		
		Expense expense = request.getContext().asType(Expense.class);
		
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		String name = I18n.get("Expense") + " " + expense.getFullName()
												.replace("/", "-");
		
		String fileLink = ReportFactory.createReport(IReport.EXPENSE, name)
				.addParam("ExpenseId", expense.getId())
				.addParam("Locale", language)
				.toAttach(expense)
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}
	
	/* Count Tags displayed on the menu items */
	
	public String expenseValidateMenuTag() {
		
		return hrMenuTagServiceProvider.get().countRecordsTag(Expense.class, ExpenseRepository.STATUS_CONFIRMED);
		
	}
	
	public String expenseVentilateMenuTag() {
		Long total = JPA.all(Expense.class).filter("self.statusSelect = 3 AND self.ventilated = false").count();
		
		return String.format("%s", total);
	}
	
	public void cancel(ActionRequest request, ActionResponse response) throws AxelorException{
		try {
			Expense expense = request.getContext().asType(Expense.class);
			expense = expenseRepositoryProvider.get().find(expense.getId());
			ExpenseService expenseService = expenseServiceProvider.get();
			
			expenseService.cancel(expense);

			Message message = expenseService.sendCancellationEmail(expense);
			if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			}
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		} finally {
			response.setReload(true);
		}
	}
	
	public void addPayment(ActionRequest request, ActionResponse response) {
		Expense expense = request.getContext().asType(Expense.class);
		expense = Beans.get(ExpenseRepository.class).find(expense.getId());
		try {
			expenseServiceProvider.get().addPayment(expense);
			response.setReload(true);
		} catch (Exception e) {
			TraceBackService.trace(e);
			response.setException(e);
		}
	}

	/**
	 * Called on clicking cancelPaymentButton, call {@link ExpenseService#cancelPayment(Expense)}.
	 * @param request
	 * @param response
	 */
	public void cancelPayment(ActionRequest request, ActionResponse response) {
		Expense expense = request.getContext().asType(Expense.class);
		expense = Beans.get(ExpenseRepository.class).find(expense.getId());
		try {
			expenseServiceProvider.get().cancelPayment(expense);
			response.setReload(true);
		} catch (Exception e) {
			TraceBackService.trace(e);
		}
	}
	
	//sending expense and sending mail to manager
	public void send(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			Expense expense = request.getContext().asType(Expense.class);
			expense = expenseRepositoryProvider.get().find(expense.getId());
			ExpenseService expenseService = expenseServiceProvider.get();

			expenseService.confirm(expense);

			Message message = expenseService.sendConfirmationEmail(expense);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}

	}
	
	public void newExpense(ActionResponse response)  {
		
		response.setView(ActionView
				.define(I18n.get("Expense"))
				.model(Expense.class.getName())
				.add("form", "expense-form")
				.map());
	}
	
	
	//validating expense and sending mail to applicant
	public void valid(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			Expense expense = request.getContext().asType(Expense.class);
			expense = expenseRepositoryProvider.get().find(expense.getId());
			ExpenseService expenseService = expenseServiceProvider.get();
			
			expenseService.validate(expense);
			
			Message message = expenseService.sendValidationEmail(expense);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}
	}
	
	//refusing expense and sending mail to applicant
	public void refuse(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try{
			Expense expense = request.getContext().asType(Expense.class);
			expense = expenseRepositoryProvider.get().find(expense.getId());
			ExpenseService expenseService = expenseServiceProvider.get();
				
			expenseService.refuse(expense);

			Message message = expenseService.sendRefusalEmail(expense);
			if(message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
				response.setFlash(String.format(I18n.get("Email sent to %s"), Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
			} 
			
		}  catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
		finally {
			response.setReload(true);
		}

	}
	
	public void fillKilometricExpenseProduct(ActionRequest request, ActionResponse response) throws AxelorException{
		
		try  {
			Expense expense = request.getContext().getParentContext().asType(Expense.class);
			HRConfigService hrConfigService = hrConfigServiceProvider.get();
			HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
			Product expenseProduct = hrConfigService.getKilometricExpenseProduct(hrConfig);
			logger.debug("Get Kilometric expense product : {}", expenseProduct);
			response.setValue("expenseProduct",expenseProduct);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	@Transactional
	 public void insertKMExpenses(ActionRequest request, ActionResponse response) throws AxelorException{
	 	User user = AuthUtils.getUser();
	 	if(user != null){
	 		Expense expense = expenseServiceProvider.get().getOrCreateExpense(user);
	 		ExpenseLine expenseLine = new ExpenseLine();
	 		expenseLine.setDistance(new BigDecimal(request.getData().get("kmNumber").toString()));
	 		expenseLine.setFromCity(request.getData().get("locationFrom").toString());
	 		expenseLine.setToCity(request.getData().get("locationTo").toString());
	 		expenseLine.setKilometricTypeSelect(new Integer(request.getData().get("allowanceTypeSelect").toString()));
	 		expenseLine.setComments(request.getData().get("comments").toString());
	 		expenseLine.setExpenseDate(new LocalDate(request.getData().get("date").toString()));
	 		
	 		Employee employee = user.getEmployee();
	 		if(employee != null)  {
	 			expenseLine.setKilometricAllowParam(expenseServiceProvider.get().getListOfKilometricAllowParamVehicleFilter(expenseLine).get(0));
	 			expenseLine.setTotalAmount(Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));
	 			expenseLine.setUntaxedAmount(expenseLine.getTotalAmount());
	 		}
	 		
	 		expense.addExpenseLineListItem(expenseLine);
	 		
	 		Beans.get(ExpenseRepository.class).save(expense);
	 	}
	 }
	
	public void computeAmounts(ActionRequest request, ActionResponse response){
		
		Expense expense = request.getContext().asType(Expense.class);
		
		ExpenseService expenseService = expenseServiceProvider.get();

		response.setValue("personalExpenseAmount", expenseService.computePersonalExpenseAmount(expense) );
		response.setValue("advanceAmount", expenseService.computeAdvanceAmount(expense) );

		
		if( expense.getKilometricExpenseLineList() != null && !expense.getKilometricExpenseLineList().isEmpty()){
			for (ExpenseLine kilometricLine : expense.getKilometricExpenseLineList()) {
				kilometricLine.setExpense(expense);
			}
			response.setValue("kilometricExpenseLineList", expense.getKilometricExpenseLineList() );
		}
		
		
	}
	
	public void removeLines(ActionRequest request, ActionResponse response) {

		Expense expense = request.getContext().asType(Expense.class);

		List<ExpenseLine> expenseLineList = expense.getExpenseLineList();

		try {
			if (expenseLineList != null && !expenseLineList.isEmpty()) {
				Iterator<ExpenseLine> expenseLineIter = expenseLineList.iterator();
				while (expenseLineIter.hasNext()) {
					ExpenseLine generalExpenseLine = expenseLineIter.next();

					if (generalExpenseLine.getKilometricExpense() != null
							&& (expense.getKilometricExpenseLineList() != null
									&& !expense.getKilometricExpenseLineList().contains(generalExpenseLine)
									|| expense.getKilometricExpenseLineList() == null)) {

						expenseLineIter.remove();
					}
				}
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		response.setValue("expenseLineList", expenseLineList);
	}
	
	public void computeKilometricExpense(ActionRequest request, ActionResponse response) throws AxelorException {
		
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		
		if (expenseLine.getKilometricAllowParam() == null || expenseLine.getDistance().compareTo(BigDecimal.ZERO) == 0
				|| expenseLine.getExpenseDate() == null || expenseLine.getKilometricTypeSelect() == 0) {
			return;
		}
		
		String userId = null;
		String userName = null;
		if (expenseLine.getExpense() != null && expenseLine.getUser() != null){
			userId = expenseLine.getExpense().getUser().getId().toString();
			userName = expenseLine.getExpense().getUser().getFullName();
		}else{
			userId = request.getContext().getParentContext().asType(Expense.class).getUser().getId().toString() ;
			userName = request.getContext().getParentContext().asType(Expense.class).getUser().getFullName() ;
		}
		Employee employee = Beans.get(EmployeeRepository.class).all().filter("self.user.id = ?1", userId).fetchOne();
		
		if (employee == null){
			throw new AxelorException( String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE), userName)  , IException.CONFIGURATION_ERROR);
		}
		
		BigDecimal amount = BigDecimal.ZERO;
		try{
			amount = Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee);
		}catch(AxelorException e){
			TraceBackService.trace(response, e);
		}
		
		
		response.setValue("totalAmount", amount);
		response.setValue("untaxedAmount", amount);
	}

	public void updateKAPOfKilometricAllowance(ActionRequest request, ActionResponse response) throws AxelorException {
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		
		if (expenseLine.getExpense() == null) {
			setExpense(request, expenseLine);
		}
		
		try {
			List<KilometricAllowParam> kilometricAllowParamList = expenseServiceProvider.get().getListOfKilometricAllowParamVehicleFilter(expenseLine);
			if (kilometricAllowParamList == null || kilometricAllowParamList.isEmpty()) {
				response.setAttr("kilometricAllowParam", "domain", "self.id IN (0)");
			} else {
				response.setAttr("kilometricAllowParam", "domain", "self.id IN (" + StringTool.getIdFromCollection(kilometricAllowParamList) + ")");
			}

			KilometricAllowParam currentKilometricAllowParam = expenseLine.getKilometricAllowParam();
			boolean vehicleOk = false;

			if (kilometricAllowParamList != null && kilometricAllowParamList.size() == 1) {
				response.setValue("kilometricAllowParam", kilometricAllowParamList.get(0));
			} else if (kilometricAllowParamList != null) {
				for (KilometricAllowParam kilometricAllowParam : kilometricAllowParamList) {
					if (currentKilometricAllowParam != null && currentKilometricAllowParam.equals(kilometricAllowParam)) {
						expenseLine.setKilometricAllowParam(kilometricAllowParam);
						vehicleOk = true;
						break;
					}
				}
				if (!vehicleOk) {
					response.setValue("kilometricAllowParam", null);
				} else {
					response.setValue("kilometricAllowParam", expenseLine.getKilometricAllowParam());
				}
			}
			
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	private void setExpense(ActionRequest request, ExpenseLine expenseLine) {
		
		Context parent = request.getContext().getParentContext();
		
		if (parent != null && parent.get("_model").equals(Expense.class.getName())) {
			expenseLine.setExpense(parent.asType(Expense.class));
		}
		
	}

	public void domainOnSelectOnKAP(ActionRequest request, ActionResponse response) throws AxelorException {
		
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		
		if (expenseLine.getExpense() == null) {
			setExpense(request, expenseLine);
		}
		
		try {
			List<KilometricAllowParam> kilometricAllowParamList = expenseServiceProvider.get().getListOfKilometricAllowParamVehicleFilter(expenseLine);
			response.setAttr("kilometricAllowParam","domain","self.id IN (" + StringTool.getIdFromCollection(kilometricAllowParamList)+ ")");
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
}
