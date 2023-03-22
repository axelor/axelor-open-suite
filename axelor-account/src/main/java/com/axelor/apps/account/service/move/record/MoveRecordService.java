package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;

public interface MoveRecordService {

	/**
	 * Set the payment mode of move.
	 * 
	 * Note: This method can set paymentMode to null even if it was not before.
	 * @param move
	 * @return Modified move
	 */
	Move setPaymentMode(Move move);

	/**
	 * Set the paymentCondition of move.
	 * 
	 * Note: This method can set paymentCondition to null even if it was not before.
	 * @param move
	 * @return Modified move
	 */
	Move setPaymentCondition(Move move);
	
}
