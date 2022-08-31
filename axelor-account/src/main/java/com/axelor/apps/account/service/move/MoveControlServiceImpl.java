package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class MoveControlServiceImpl implements MoveControlService {

  @Override
  public void checkSameCompany(Move move) throws AxelorException {

    Journal journal = move.getJournal();
    Company company = move.getCompany();

    if (journal != null && company != null && !company.equals(journal.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_INCONSISTENCY_DETECTED_JOURNAL_COMPANY),
          move.getReference(),
          journal.getName());
    }
  }
}
