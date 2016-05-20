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
package com.axelor.apps.hr.web.expense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ExpenseController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private ExpenseService expenseService;

	@Inject
	protected GeneralService generalService;
	
	public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
		Expense expense = expenseLine.getExpense();
		if(expense == null){
			expense = request.getContext().getParentContext().asType(Expense.class);
			expenseLine.setExpense(expense);
		}
		if(expenseLine.getAnalyticDistributionTemplate() != null){
			expenseLine = expenseService.createAnalyticDistributionWithTemplate(expenseLine);
			response.setValue("analyticDistributionLineList", expenseLine.getAnalyticDistributionLineList());
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
			expenseLine = expenseService.computeAnalyticDistribution(expenseLine);
			response.setValue("analyticDistributionLineList", expenseLine.getAnalyticDistributionLineList());
		}
	}
	
	public void editExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 1",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
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

	public void editExpenseSelected(ActionRequest request, ActionResponse response){
		Map expenseMap = (Map)request.getContext().get("expenseSelect");
		Expense expense = Beans.get(ExpenseRepository.class).find(new Long((Integer)expenseMap.get("id")));
		response.setView(ActionView
				.define(I18n.get("Expense"))
				.model(Expense.class.getName())
				.add("form", "expense-form")
				.param("forceEdit", "true")
				.domain("self.id = "+expenseMap.get("id"))
				.context("_showRecord", String.valueOf(expense.getId())).map());
	}

	public void allExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user = ?1 AND self.company = ?2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}

		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}

		response.setView(ActionView.define(I18n.get("My Expenses"))
				   .model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
	}

	public void validateExpense(ActionRequest request, ActionResponse response) throws AxelorException{
		
		List<Expense> expenseList = Lists.newArrayList();
		
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			expenseList = Query.of(Expense.class).filter("self.company = ?1 AND  self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			expenseList = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null && !AuthUtils.getUser().getEmployee().getHrManager()){
			expenseList = Query.of(Expense.class).filter("self.user = ?1 AND self.company = ?2 AND self.statusSelect = 2 ",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}

		response.setView(ActionView.define(I18n.get("Expenses to Validate"))
			   .model(Expense.class.getName())
			   .add("grid","expense-validate-grid")
			   .add("form","expense-form")
			   .domain("self.id in ("+expenseListIdStr+")")
			   .map());
	}

	public void historicExpense(ActionRequest request, ActionResponse response){
		
		List<Expense> expenseList = Lists.newArrayList();
		
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			expenseList = Query.of(Expense.class).filter("self.company = ?1 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser().getActiveCompany()).fetch();
		}else{
			expenseList = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND (self.statusSelect = 3 OR self.statusSelect = 4)",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).fetch();
		}
		
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}

		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}

		response.setView(ActionView.define(I18n.get("Historic colleague Expenses"))
				.model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
	}


	public void showSubordinateExpenses(ActionRequest request, ActionResponse response){
		List<User> userList = Query.of(User.class).filter("self.employee.manager = ?1",AuthUtils.getUser()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (User user : userList) {
			List<Expense> expenseList = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND self.statusSelect = 2",user,AuthUtils.getUser().getActiveCompany()).fetch();
			for (Expense expense : expenseList) {
				expenseListId.add(expense.getId());
			}
		}
		if(expenseListId.isEmpty()){
			response.setNotify(I18n.get("No expense to be validated by your subordinates"));
		}
		else{
			String expenseListIdStr = "-2";
			if(!expenseListId.isEmpty()){
				expenseListIdStr = Joiner.on(",").join(expenseListId);
			}

			response.setView(ActionView.define(I18n.get("Expenses to be Validated by your subordinates"))
				   .model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
		}
	}

	public void compute(ActionRequest request, ActionResponse response){
		Expense expense = request.getContext().asType(Expense.class);
		expense = expenseService.compute(expense);
		response.setValues(expense);
	}

	public void ventilate(ActionRequest request, ActionResponse response) throws AxelorException{
		Expense expense = request.getContext().asType(Expense.class);
		expense = Beans.get(ExpenseRepository.class).find(expense.getId());
		Move move = expenseService.ventilate(expense);
		response.setReload(true);
		response.setView(ActionView.define(I18n.get("Move"))
				   .model(Move.class.getName())
				   .add("grid","move-grid")
				   .add("form","move-form")
				   .context("_showRecord", String.valueOf(move.getId()))
				   .map());
	}

	public void cancelExpense(ActionRequest request, ActionResponse response) throws AxelorException{
		Expense expense = request.getContext().asType(Expense.class);
		expense = Beans.get(ExpenseRepository.class).find(expense.getId());
		expenseService.cancel(expense);
		response.setReload(true);
	}

	public void validateDates(ActionRequest request, ActionResponse response) throws AxelorException{
		Expense expense = request.getContext().asType(Expense.class);
		if(expense.getExpenseLineList()!= null){
			List<ExpenseLine> expenseLineList = expense.getExpenseLineList();
			List<Integer> expenseLineId = new ArrayList<Integer>();
			int compt = 0;
			for (ExpenseLine expenseLine : expenseLineList) {
				compt++;
				if(expenseLine.getExpenseDate().isAfter(generalService.getTodayDate())){
					expenseLineId.add(compt);
				}
			}
			if(!expenseLineId.isEmpty()){
				String ids =  Joiner.on(",").join(expenseLineId);
				throw new AxelorException(String.format(I18n.get("Problème de date pour la (les) ligne(s) : "+ids)), IException.CONFIGURATION_ERROR);
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
				.addModel(expense)
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}
	

}
