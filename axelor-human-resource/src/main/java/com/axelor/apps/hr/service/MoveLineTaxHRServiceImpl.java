package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;

public class MoveLineTaxHRServiceImpl extends MoveLineTaxServiceImpl {

  @Inject
  public MoveLineTaxHRServiceImpl(
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AppBaseService appBaseService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository,
      TaxAccountToolService taxAccountToolService,
      MoveLineToolService moveLineToolService,
      TaxService taxService) {
    super(
        moveLineRepository,
        taxPaymentMoveLineService,
        appBaseService,
        moveLineCreateService,
        moveRepository,
        taxAccountToolService,
        moveLineToolService,
        taxService);
  }

  @Override
  public boolean isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect) {

    if (moveLine.getMove() == null) {
      return super.isMoveLineTaxAccountRequired(moveLine, functionalOriginSelect);
    }

    return moveLine.getMove().getExpense() == null
        && super.isMoveLineTaxAccountRequired(moveLine, functionalOriginSelect);
  }
}
