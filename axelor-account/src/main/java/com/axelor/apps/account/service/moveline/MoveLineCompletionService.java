package com.axelor.apps.account.service.moveline;

import java.util.List;

import com.axelor.apps.account.db.MoveLine;

public interface MoveLineCompletionService {

	
	/**
	 * Complete analytic move lines
	 * @param moveLines
	 */
	void completeAnalyticMoveLines(List<MoveLine> moveLines);
	
	
	/**
	 * Complete account and partner related fields of move line
	 * @param moveLine
	 */
	void freezeAccountAndPartnerFields(MoveLine moveLine);
}
