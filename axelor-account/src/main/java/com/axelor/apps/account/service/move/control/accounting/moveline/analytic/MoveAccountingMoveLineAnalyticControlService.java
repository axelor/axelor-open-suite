package com.axelor.apps.account.service.move.control.accounting.moveline.analytic;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveAccountingMoveLineAnalyticControlService {
	
	/**
	 * Method that checks if the analytic distribution template is setted when it is mandatory.
	 * 
	 * @param moveLine
	 * @throws AxelorException
	 */
	void checkMandatoryAnalyticDistributionTemplate(MoveLine moveLine) throws AxelorException;
	
	/**
	 * Method that checks the the analytic distribution template is setted and if it is authorized.
	 * 
	 * @param moveLine
	 * @throws AxelorException
	 */
	void checkAuthorizedAnalyticDistributionTemplate(MoveLine moveLine)  throws AxelorException;

}
