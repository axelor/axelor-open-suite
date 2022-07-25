package com.axelor.apps.account.service.move.control.moveline;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.move.control.moveline.date.MoveLineDateControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class MoveLinePreSaveControlServiceImpl implements MoveLinePreSaveControlService{
	
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected MoveLineDateControlService moveLineDateControlService;
	
	@Inject
	public MoveLinePreSaveControlServiceImpl(MoveLineDateControlService moveLineDateControlService) {
		this.moveLineDateControlService = moveLineDateControlService;
	}

	@Override
	public void checkValidity(MoveLine moveLine) throws AxelorException {
		
		log.debug("Checking validity of moveLine {}", moveLine);
		moveLineDateControlService.checkDateInPeriod(moveLine);
		
	}

	
}
