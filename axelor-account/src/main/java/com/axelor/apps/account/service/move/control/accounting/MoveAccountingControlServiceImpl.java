package com.axelor.apps.account.service.move.control.accounting;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.control.accounting.balance.MoveAccountingBalanceControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.MoveAccountingMoveLineControlService;
import com.axelor.apps.account.service.move.control.accounting.period.MoveAccountingPeriodControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class MoveAccountingControlServiceImpl implements MoveAccountingControlService {
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected MoveAccountingNullControlService moveAccountingNullControlService;
	protected MoveAccountingMoveLineControlService moveAccountingMoveLineControlService;
	protected MoveAccountingBalanceControlService moveAccountingBalanceControlService;
	protected MoveAccountingPeriodControlService moveAccountingPeriodControlService;
	
	@Inject
	public MoveAccountingControlServiceImpl(MoveAccountingMoveLineControlService moveAccountingMoveLineControlService, MoveAccountingNullControlService moveAccountingNullControlService,
			MoveAccountingBalanceControlService moveAccountingBalanceControlService,
			MoveAccountingPeriodControlService moveAccountingPeriodControlService) {
		this.moveAccountingMoveLineControlService = moveAccountingMoveLineControlService;
		this.moveAccountingNullControlService = moveAccountingNullControlService;
		this.moveAccountingBalanceControlService = moveAccountingBalanceControlService;
		this.moveAccountingPeriodControlService = moveAccountingPeriodControlService;
	}
	
	@Override
	public void controlAccounting(Move move) throws AxelorException {
		log.debug("Controlling accounting of move {}", move);
		Objects.requireNonNull(move);
		
		moveAccountingNullControlService.checkNullFields(move);

	    if (move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE
	            && move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_OPENING) {
	    	
	    	if (move.getMoveLineList() != null) {
				for (MoveLine moveLine: move.getMoveLineList()) {
					moveAccountingMoveLineControlService.controlAccounting(moveLine);
				}
	    	}
			moveAccountingBalanceControlService.checkWellBalanced(move);
	    }
	    moveAccountingPeriodControlService.checkClosedPeriod(move);

	}
	

}
