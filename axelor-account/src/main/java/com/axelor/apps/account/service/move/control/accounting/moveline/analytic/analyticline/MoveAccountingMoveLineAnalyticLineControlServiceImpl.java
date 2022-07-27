package com.axelor.apps.account.service.move.control.accounting.moveline.analytic.analyticline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingMoveLineAnalyticLineControlServiceImpl
    implements MoveAccountingMoveLineAnalyticLineControlService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkInactiveAnalyticJournal(AnalyticMoveLine analyticMoveLine)
      throws AxelorException {

    log.debug("Checking inactive analytic journal of analyticMoveLine {}", analyticMoveLine);

    if (analyticMoveLine.getAnalyticJournal() != null
        && analyticMoveLine.getAnalyticJournal().getStatusSelect() != null
        && analyticMoveLine.getAnalyticJournal().getStatusSelect()
            != AnalyticJournalRepository.STATUS_ACTIVE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_JOURNAL_FOUND),
          analyticMoveLine.getAnalyticJournal().getName());
    }
  }

  @Override
  public void checkInactiveAnalyticAccount(AnalyticMoveLine analyticMoveLine)
      throws AxelorException {

    log.debug("Checking inactive analytic account of analyticMoveLine {}", analyticMoveLine);

    if (analyticMoveLine.getAnalyticAccount() != null
        && analyticMoveLine.getAnalyticAccount().getStatusSelect() != null
        && analyticMoveLine.getAnalyticAccount().getStatusSelect()
            != AnalyticJournalRepository.STATUS_ACTIVE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_JOURNAL_FOUND),
          analyticMoveLine.getAnalyticAccount().getName());
    }
  }
}
