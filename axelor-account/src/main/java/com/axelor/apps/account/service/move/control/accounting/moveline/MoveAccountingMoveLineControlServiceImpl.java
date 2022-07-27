package com.axelor.apps.account.service.move.control.accounting.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.move.control.accounting.moveline.analytic.MoveAccountingMoveLineAnalyticControlService;
import com.axelor.apps.account.service.move.control.accounting.moveline.tax.MoveAccountingMoveLineTaxControlService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingMoveLineControlServiceImpl
    implements MoveAccountingMoveLineControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected MoveAccountingMoveLineAnalyticControlService moveLineAnalyticControlService;
  protected MoveAccountingMoveLineTaxControlService moveLineTaxControlService;

  @Inject
  public MoveAccountingMoveLineControlServiceImpl(
      MoveAccountingMoveLineAnalyticControlService moveLineAnalyticControlService,
      MoveAccountingMoveLineTaxControlService moveLineTaxControlService) {
    this.moveLineAnalyticControlService = moveLineAnalyticControlService;
    this.moveLineTaxControlService = moveLineTaxControlService;
  }

  @Override
  public void controlAccounting(MoveLine moveLine) throws AxelorException {

    log.debug("Controlling accounting of moveLine {}", moveLine);

    moveLineTaxControlService.checkMandatoryTax(moveLine);
    moveLineAnalyticControlService.checkAuthorizedAnalyticDistributionTemplate(moveLine);
    moveLineAnalyticControlService.checkMandatoryAnalyticDistributionTemplate(moveLine);
  }
}
