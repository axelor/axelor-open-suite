package com.axelor.apps.account.service.move.control.accounting.moveline.tax;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveAccountingMoveLineTaxControlService {
	
	/**
	 * Method that checks the viability of the tax of the move line.
	 * 
	 * @param moveLine
	 * @throws AxelorException
	 */
	void checkMandatoryTax(MoveLine moveLine) throws AxelorException;

}
