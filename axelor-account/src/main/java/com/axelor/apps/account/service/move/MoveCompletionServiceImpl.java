package com.axelor.apps.account.service.move;

import java.util.Objects;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.moveline.MoveLineCompletionService;
import com.axelor.exception.AxelorException;

public class MoveCompletionServiceImpl implements MoveCompletionService{
	
	protected MoveLineCompletionService moveLineCompletionService;
	protected MoveSequenceService moveSequenceService;
	
	public MoveCompletionServiceImpl(MoveLineCompletionService moveLineCompletionService,
			MoveSequenceService moveSequenceService) {
		this.moveLineCompletionService = moveLineCompletionService;
		this.moveSequenceService = moveSequenceService;
	}
	
	@Override
	public void completeMove(Move move) throws AxelorException {
		
		Objects.requireNonNull(move);
		
        if (move.getCurrency() != null) {
          move.setCurrencyCode(move.getCurrency().getCode());
        }
		
        moveSequenceService.setDraftSequence(move);
        
		if (move.getMoveLineList() != null) {
			moveLineCompletionService.completeAnalyticMoveLines(move.getMoveLineList());
		}
	}

	@Override
	public void freezeAccountAndPartnerFieldsOnMoveLines(Move move) {
	    for (MoveLine moveLine : move.getMoveLineList()) {
	        moveLineCompletionService.freezeAccountAndPartnerFields(moveLine);
	      }
	}

}
