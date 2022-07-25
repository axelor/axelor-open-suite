package com.axelor.apps.account.service.move.control.accounting;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingControlService {

	/**
	 *  This method will checks if move matches all the pre-conditions necessary in order to
	 *  account it.
	 *  If not, this method will throw a exception.
	 * @param move
	 */
	void controlAccounting(Move move) throws AxelorException;
}
