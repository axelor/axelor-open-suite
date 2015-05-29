package com.axelor.apps.hr.web.expense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;

public class ExpenseController {
	
	public void editExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user = ?1 AND self.statusSelect = 1",AuthUtils.getUser()).fetch();
		if(expenseList.isEmpty()){
			response.setView(ActionView
									.define("Expense")
									.model(Expense.class.getName())
									.add("form", "expense-form")
									.context("","").map());
		}
		else if(expenseList.size() == 1){
			response.setView(ActionView
					.define("Expense")
					.model(Expense.class.getName())
					.add("form", "expense-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(expenseList.get(0).getId())).map());
		}
		else{
			response.setView(ActionView
					.define("Expense")
					.model(Wizard.class.getName())
					.add("form", "popup-expense-form")
					.param("forceEdit", "true")
					.param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("forceEdit", "true")
					.map());
		}
	}
	
	public void editExpenseSelected(ActionRequest request, ActionResponse response){
		Map expenseMap = (Map)request.getContext().get("expenseSelect");
		Expense expense = Beans.get(ExpenseRepository.class).find(new Long((Integer)expenseMap.get("id")));
		response.setView(ActionView
				.define("Expense")
				.model(Expense.class.getName())
				.add("form", "expense-form")
				.param("forceEdit", "true")
				.domain("self.id = "+expenseMap.get("id"))
				.context("_showRecord", String.valueOf(expense.getId())).map());
	}
	
	public void allExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user = ?1",AuthUtils.getUser()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		
		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}
		
		response.setView(ActionView.define("My Expenses")
				   .model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
	}
	
	public void validateExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.statusSelect = 2",AuthUtils.getUser()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getManager() == null){
			expenseList = Query.of(Expense.class).filter("self.user = ?1 AND self.statusSelect = 2",AuthUtils.getUser()).fetch();
		}
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}
		
		response.setView(ActionView.define("Expenses to Validate")
			   .model(Expense.class.getName())
			   .add("grid","expense-validate-grid")
			   .add("form","expense-form")
			   .domain("self.id in ("+expenseListIdStr+")")
			   .map());
	}
	
	public void historicExpense(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Beans.get(ExpenseRepository.class).all().filter("self.user.employee.manager = ?1 AND self.statusSelect = 3 OR self.statusSelect = 4",AuthUtils.getUser()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			expenseListId.add(expense.getId());
		}
		
		String expenseListIdStr = "-2";
		if(!expenseListId.isEmpty()){
			expenseListIdStr = Joiner.on(",").join(expenseListId);
		}
		
		response.setView(ActionView.define("Colleague Expenses")
				.model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
	}
	
	
	public void showSubordinateExpenses(ActionRequest request, ActionResponse response){
		List<Expense> expenseList = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.statusSelect = 2",AuthUtils.getUser()).fetch();
		List<Long> expenseListId = new ArrayList<Long>();
		for (Expense expense : expenseList) {
			List<Expense> expenseList2 = Query.of(Expense.class).filter("self.user.employee.manager = ?1 AND self.statusSelect = 2",expense.getUser()).fetch();
			for (Expense expense2 : expenseList2) {
				expenseListId.add(expense2.getId());
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
			
			response.setView(ActionView.define("Expenses to be Validated by your subordinates")
				   .model(Expense.class.getName())
				   .add("grid","expense-grid")
				   .add("form","expense-form")
				   .domain("self.id in ("+expenseListIdStr+")")
				   .map());
		}
	}
	
}
