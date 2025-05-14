package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.control.MoveCheckService;
import com.axelor.apps.account.service.move.massentry.MassEntryService;
import com.axelor.apps.account.service.move.massentry.MassEntryVerificationService;
import com.axelor.apps.account.service.move.record.MoveDefaultService;
import com.axelor.apps.account.service.move.record.MoveGroupServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.account.service.period.PeriodCheckService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.budget.service.date.BudgetDateService;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

public class MoveGroupBudgetServiceImpl extends MoveGroupServiceImpl {

  protected BudgetDateService budgetDateService;

  @Inject
  public MoveGroupBudgetServiceImpl(
      MoveDefaultService moveDefaultService,
      MoveAttrsService moveAttrsService,
      PeriodCheckService periodCheckService,
      MoveCheckService moveCheckService,
      MoveCutOffService moveCutOffService,
      MoveRecordUpdateService moveRecordUpdateService,
      MoveRecordSetService moveRecordSetService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveCounterPartService moveCounterPartService,
      MoveLineTaxService moveLineTaxService,
      PeriodService periodService,
      MoveRepository moveRepository,
      MassEntryService massEntryService,
      MassEntryVerificationService massEntryVerificationService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      PfpService pfpService,
      AnalyticAttrsService analyticAttrsService,
      MoveLineRecordService moveLineRecordService,
      BudgetDateService budgetDateService) {
    super(
        moveDefaultService,
        moveAttrsService,
        periodCheckService,
        moveCheckService,
        moveCutOffService,
        moveRecordUpdateService,
        moveRecordSetService,
        moveToolService,
        moveInvoiceTermService,
        moveCounterPartService,
        moveLineTaxService,
        periodService,
        moveRepository,
        massEntryService,
        massEntryVerificationService,
        moveLineMassEntryRecordService,
        pfpService,
        analyticAttrsService,
        moveLineRecordService);
    this.budgetDateService = budgetDateService;
  }

  @Override
  public void checkBeforeSave(Move move) throws AxelorException {
    super.checkBeforeSave(move);

    checkBudgetDates(move);
  }

  protected void checkBudgetDates(Move move) throws AxelorException {
    String error = budgetDateService.checkBudgetDates(move);
    if (StringUtils.isNotEmpty(error)) {
      throw new AxelorException(move, TraceBackRepository.CATEGORY_MISSING_FIELD, error);
    }
  }
}
