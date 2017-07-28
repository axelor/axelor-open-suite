package com.axelor.apps.hr.service.expense;

import java.math.BigDecimal;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.exception.AxelorException;

public interface KilometricAllowanceService {

	/**
	 * Compute the distance between the from city and the to city in the expense
	 * line.
	 * 
	 * @param expenseLine
	 * @return
	 */
	BigDecimal computeDistance(ExpenseLine expenseLine) throws AxelorException;

}
