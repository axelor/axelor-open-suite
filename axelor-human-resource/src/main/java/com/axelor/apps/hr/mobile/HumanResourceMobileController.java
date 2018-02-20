package com.axelor.apps.hr.mobile;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class HumanResourceMobileController {
	
	@Inject
	private Provider<ExpenseService> expenseServiceProvider;

	/**
	 * This method is used in mobile application.
	 * It was in ExpenseController
	 * @param request
	 * @param response
	 * @throws AxelorException
	 */
	@Transactional
	public void insertKMExpenses(ActionRequest request, ActionResponse response) throws AxelorException {
		User user = AuthUtils.getUser();
		if (user != null) {
			Expense expense = expenseServiceProvider.get().getOrCreateExpense(user);
			// Expense expense = getOrCreateExpense(user);
			ExpenseLine expenseLine = new ExpenseLine();
			expenseLine.setDistance(new BigDecimal(request.getData().get("kmNumber").toString()));
			expenseLine.setFromCity(request.getData().get("locationFrom").toString());
			expenseLine.setToCity(request.getData().get("locationTo").toString());
			expenseLine.setKilometricTypeSelect(new Integer(request.getData().get("allowanceTypeSelect").toString()));
			expenseLine.setComments(request.getData().get("comments").toString());
			expenseLine.setExpenseDate(new LocalDate(request.getData().get("date").toString()));

			Employee employee = user.getEmployee();
			if (employee != null) {
				expenseLine.setKilometricAllowParam(
						expenseServiceProvider.get().getListOfKilometricAllowParamVehicleFilter(expenseLine).get(0));
						//getListOfKilometricAllowParamVehicleFilter(expenseLine).get(0));
				expenseLine.setTotalAmount(
						Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));
				expenseLine.setUntaxedAmount(expenseLine.getTotalAmount());
			}

			expense.addGeneralExpenseLineListItem(expenseLine);

			Beans.get(ExpenseRepository.class).save(expense);
		}
	}

	/**
	 * This method is used in mobile application.
	 * It was in ExpenseController
	 * @param request
	 * @param response
	 * @throws AxelorException
	 */
	public void removeLines(ActionRequest request, ActionResponse response) {

		Expense expense = request.getContext().asType(Expense.class);

		//List<ExpenseLine> expenseLineList = expense.getExpenseLineList();
		List<ExpenseLine> expenseLineList = expenseServiceProvider.get().getExpenseLineList(expense);
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

}