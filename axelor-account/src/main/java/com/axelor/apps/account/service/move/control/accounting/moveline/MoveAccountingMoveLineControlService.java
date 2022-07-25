package com.axelor.apps.account.service.move.control.accounting.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveAccountingMoveLineControlService {

	/**
	 *  This method will checks if moveLine matches all the pre-conditions necessary in order to
	 *  account it.
	 *  If not, this method will throw a exception.
	 * @param moveLine: MoveLine
	 */
	void controlAccounting(MoveLine moveLine) throws AxelorException;
}
