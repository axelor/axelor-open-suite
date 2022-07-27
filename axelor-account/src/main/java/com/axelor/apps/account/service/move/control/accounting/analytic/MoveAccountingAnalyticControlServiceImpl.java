package com.axelor.apps.account.service.move.control.accounting.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingAnalyticControlServiceImpl
    implements MoveAccountingAnalyticControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkInactiveAnalyticJournal(Move move) throws AxelorException {

    log.debug("Checking inactive analytic journal of move {}", move);

    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAnalyticMoveLineList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .map(AnalyticMoveLine::getAnalyticJournal)
              .filter(
                  analyticJournal ->
                      analyticJournal.getStatusSelect() != null
                          && analyticJournal.getStatusSelect()
                              != AnalyticJournalRepository.STATUS_ACTIVE)
              .distinct()
              .map(AnalyticJournal::getName)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_JOURNAL_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_JOURNALS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }

  @Override
  public void checkInactiveAnalyticAccount(Move move) throws AxelorException {
    log.debug("Checking inactive analytic account of move {}", move);
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAnalyticMoveLineList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .map(AnalyticMoveLine::getAnalyticAccount)
              .filter(
                  analyticAccount ->
                      analyticAccount.getStatusSelect() != null
                          && analyticAccount.getStatusSelect()
                              != AnalyticAccountRepository.STATUS_ACTIVE)
              .distinct()
              .map(AnalyticAccount::getCode)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_ACCOUNT_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ANALYTIC_ACCOUNTS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }
}
