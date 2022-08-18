package com.axelor.apps.account.service.move.control.accounting.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Splitter;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingJournalControlServiceImpl
    implements MoveAccountingJournalControlService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkInactiveJournal(Move move) throws AxelorException {
    log.debug("Checking inactive journal of move {}", move);
    if (move.getJournal() != null
        && move.getJournal().getStatusSelect() != JournalRepository.STATUS_ACTIVE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INACTIVE_JOURNAL_FOUND),
          move.getJournal().getName());
    }
  }

  @Override
  public void checkAuthorizedFunctionalOrigin(Move move) throws AxelorException {

    log.debug(
        "Checking authorized function origin of move {} with journal {}", move, move.getJournal());

    Integer functionalOriginSelect = move.getFunctionalOriginSelect();

    if (functionalOriginSelect == null || functionalOriginSelect == 0) {
      return;
    }

    Journal journal = move.getJournal();
    String authorizedFunctionalOriginSelect = journal.getAuthorizedFunctionalOriginSelect();

    if (authorizedFunctionalOriginSelect != null
        && !(Splitter.on(",")
            .splitToList(authorizedFunctionalOriginSelect)
            .contains(functionalOriginSelect.toString()))) {

      Option selectionItem =
          MetaStore.getSelectionItem(
              "iaccount.move.functional.origin.select", functionalOriginSelect.toString());
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_14),
          selectionItem.getLocalizedTitle(),
          move.getReference(),
          journal.getName(),
          journal.getCode());
    }
  }
}
