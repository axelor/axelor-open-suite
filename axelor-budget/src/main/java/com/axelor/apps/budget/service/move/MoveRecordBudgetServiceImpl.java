package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveRecordServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveRecordBudgetServiceImpl extends MoveRecordServiceImpl {

  protected MoveBudgetService moveBudgetService;

  @Inject
  public MoveRecordBudgetServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodServiceAccount periodAccountService,
      MoveCheckService moveCheckService,
      MoveComputeService moveComputeService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveRepository moveRepository,
      AppAccountService appAccountService,
      MassEntryService massEntryService,
      MoveLineMassEntryToolService moveLineMassEntryToolService,
      MoveBudgetService moveBudgetService) {
    super(
        moveDefaultService,
        moveAttrsService,
        periodAccountService,
        moveCheckService,
        moveComputeService,
        moveRecordUpdateService,
        moveRecordSetService,
        moveRepository,
        appAccountService,
        massEntryService,
        moveLineMassEntryToolService);
    this.moveBudgetService = moveBudgetService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MoveContext onSaveCheck(Move move) throws AxelorException {
    MoveContext result = super.onSaveCheck(move);

    Beans.get(MoveBudgetService.class).getBudgetExceedAlert(move);

    return result;
  }
}
