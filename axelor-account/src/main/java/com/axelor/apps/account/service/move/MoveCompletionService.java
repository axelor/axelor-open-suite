package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveCompletionService {

	/**
	 * Complete the move
	 * @param move
	 * @throws AxelorException 
	 */
	void completeMove(Move move) throws AxelorException;
	
	
	  /**
	   * Method that freeze the account and partner fields on move lines
	   *
	   * @param move
	   */
	void freezeAccountAndPartnerFieldsOnMoveLines(Move move);
}
