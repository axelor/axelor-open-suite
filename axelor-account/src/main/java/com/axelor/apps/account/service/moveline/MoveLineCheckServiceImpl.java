package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MoveLineCheckServiceImpl implements MoveLineCheckService {
  protected AccountService accountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticDistributionTemplateService analyticDistributionTemplateService;

  @Inject
  public MoveLineCheckServiceImpl(
      AccountService accountService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticDistributionTemplateService analyticDistributionTemplateService) {
    this.accountService = accountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticDistributionTemplateService = analyticDistributionTemplateService;
  }

  @Override
  public void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAnalyticDistributionTemplate() != null) {
      analyticMoveLineService.validateLines(
          moveLine.getAnalyticDistributionTemplate().getAnalyticDistributionLineList());

      if (!analyticMoveLineService.validateAnalyticMoveLines(moveLine.getAnalyticMoveLineList())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVALID_ANALYTIC_MOVE_LINE));
      }

      analyticDistributionTemplateService.validateTemplatePercentages(
          moveLine.getAnalyticDistributionTemplate());
    }
  }

  @Override
  public void checkAnalyticAxes(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAccount() != null) {
      accountService.checkAnalyticAxis(
          moveLine.getAccount(), moveLine.getAnalyticDistributionTemplate());
    }
  }
}
