package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveComputeService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveGroupServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class MoveRecordBudgetServiceImpl extends MoveGroupServiceImpl {

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
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveCounterPartService moveCounterPartService,
      MoveLineControlService moveLineControlService,
      MoveLineTaxService moveLineTaxService,
      PeriodService periodService,
      MoveRepository moveRepository,
      AppAccountService appAccountService,
      MassEntryService massEntryService,
      MassEntryVerificationService massEntryVerificationService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MoveBudgetService moveBudgetService) {
    super(
        moveDefaultService,
        moveAttrsService,
        periodAccountService,
        moveCheckService,
        moveComputeService,
        moveRecordUpdateService,
        moveRecordSetService,
        moveToolService,
        moveInvoiceTermService,
        moveCounterPartService,
        moveLineControlService,
        moveLineTaxService,
        periodService,
        moveRepository,
        appAccountService,
        massEntryService,
        massEntryVerificationService,
        moveLineMassEntryRecordService);
    this.moveBudgetService = moveBudgetService;
  }

  @Override
  public void checkBeforeSave(Move move) throws AxelorException {

    super.checkBeforeSave(move);

    Beans.get(MoveBudgetService.class).getBudgetExceedAlert(move);
  }
}
