package com.axelor.apps.account.service.move.control.accounting;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingNullControlService {

	/**
	 * Check null or empty model
	 * @param model
	 * @throws AxelorException
	 */
	void checkNullFields(Move model) throws AxelorException;
}
