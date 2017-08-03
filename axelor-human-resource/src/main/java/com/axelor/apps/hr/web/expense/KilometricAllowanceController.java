/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.math.BigDecimal;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.expense.KilometricAllowanceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class KilometricAllowanceController {

	private final AppHumanResourceService appHumanResourceService;

	@Inject
	public KilometricAllowanceController(AppHumanResourceService appHumanResourceService,
			KilometricAllowanceService kilometricAllowanceService) {
		this.appHumanResourceService = appHumanResourceService;
	}

	public void computeDistanceAndKilometricExpense(ActionRequest request, ActionResponse response)
			throws AxelorException {

		// Compute distance.

		if (!appHumanResourceService.getAppExpense().getComputeDistanceWithWebService()) {
			return;
		}

		Context context = request.getContext();
		ExpenseLine expenseLine = context.asType(ExpenseLine.class);

		if (Strings.isNullOrEmpty(expenseLine.getFromCity()) || Strings.isNullOrEmpty(expenseLine.getToCity())) {
			return;
		}

		BigDecimal distance = Beans.get(KilometricAllowanceService.class).computeDistance(expenseLine);
		response.setValue("distance", distance);

		// Compute kilometric distance.

		if (expenseLine.getKilometricAllowParam() == null || expenseLine.getExpenseDate() == null
				|| expenseLine.getKilometricTypeSelect() == 0) {
			return;
		}

		Expense expense = expenseLine.getExpense();

		if (expense == null) {
			expense = context.getParent().asType(Expense.class);
		}

		Employee employee = expense.getUser().getEmployee();

		if (employee == null) {
			throw new AxelorException(
					String.format(I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE), expense.getUser().getName()),
					IException.CONFIGURATION_ERROR);
		}

		BigDecimal amount = Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee);
		response.setValue("totalAmount", amount);
		response.setValue("untaxedAmount", amount);
	}

}
