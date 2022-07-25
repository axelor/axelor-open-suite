package com.axelor.apps.account.service.move.control.accounting.moveline;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.move.control.accounting.moveline.account.MoveAccountingMoveLineAccountControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.amount.MoveAccountingMoveLineAmountControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.analytic.MoveAccountingMoveLineAnalyticControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.tax.MoveAccountingMoveLineTaxControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class MoveAccountingMoveLineControlServiceImpl implements MoveAccountingMoveLineControlService{
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected MoveAccountingMoveLineAccountControlService moveLineAccountControlService;
	protected MoveAccountingMoveLineAmountControlService moveLineAmountControlService;
	protected MoveAccountingMoveLineAnalyticControlService moveLineAnalyticControlService;
	protected MoveAccountingMoveLineTaxControlService moveLineTaxControlService;
	
	
	@Inject
	public MoveAccountingMoveLineControlServiceImpl(MoveAccountingMoveLineAccountControlService moveLineAccountControlService, MoveAccountingMoveLineAmountControlService moveLineAmountControlService,
			MoveAccountingMoveLineAnalyticControlService moveLineAnalyticControlService, MoveAccountingMoveLineTaxControlService moveLineTaxControlService) {
		this.moveLineAccountControlService = moveLineAccountControlService;
		this.moveLineAmountControlService = moveLineAmountControlService;
		this.moveLineAnalyticControlService = moveLineAnalyticControlService;
		this.moveLineTaxControlService = moveLineTaxControlService;
	}

	@Override
	public void controlAccounting(MoveLine moveLine) throws AxelorException {
		
		log.debug("Controlling accounintg of moveLine {}", moveLine);
		
		moveLineTaxControlService.checkMandatoryTax(moveLine);
		moveLineAnalyticControlService.checkAuthorizedAnalyticDistributionTemplate(moveLine);
		moveLineAnalyticControlService.checkMandatoryAnalyticDistributionTemplate(moveLine);
		moveLineAmountControlService.checkNotEmpty(moveLine);
		moveLineAccountControlService.checkValidAccount(moveLine);
		
		
	}
	
	

}
