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

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.KilometricAllowanceService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class KilometricAllowanceController {

	private KilometricAllowanceService kilometricAllowanceService;

	@Inject
	public KilometricAllowanceController(KilometricAllowanceService kilometricAllowanceService) {
		this.kilometricAllowanceService = kilometricAllowanceService;
	}

	public void computeDistance(ActionRequest request, ActionResponse response) throws AxelorException {
		ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);

		if (Strings.isNullOrEmpty(expenseLine.getFromCity()) || Strings.isNullOrEmpty(expenseLine.getToCity())
				|| expenseLine.getKilometricTypeSelect() == 0) {
			return;
		}

		BigDecimal distance = kilometricAllowanceService.computeDistance(expenseLine);
		response.setValue("distance", distance);
	}

}
