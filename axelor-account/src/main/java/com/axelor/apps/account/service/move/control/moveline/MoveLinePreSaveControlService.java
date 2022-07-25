package com.axelor.apps.account.service.move.control.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLinePreSaveControlService {

	/**
	 * Control the validity of the move
	 * @param move
	 * @throws AxelorException
	 */
	void checkValidity(MoveLine moveLine) throws AxelorException;
}
