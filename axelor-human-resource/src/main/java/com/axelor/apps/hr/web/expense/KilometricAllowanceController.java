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
