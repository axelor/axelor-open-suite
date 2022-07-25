package com.axelor.apps.account.service.move.update;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveUpdateService {

	/**
	 * Update the move only this move is in day book mode
	 * @param move
	 * @throws AxelorException
	 */
	void updateInDayBookMode(Move move) throws AxelorException;
}
