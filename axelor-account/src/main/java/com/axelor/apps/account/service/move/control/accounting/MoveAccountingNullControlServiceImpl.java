package com.axelor.apps.account.service.move.control.accounting;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingNullControlServiceImpl implements MoveAccountingNullControlService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkNullFields(Move move) throws AxelorException {
    log.debug("Checking null or empty fields of move {}", move);
    Journal journal = move.getJournal();
    Company company = move.getCompany();

    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(IExceptionMessage.MOVE_3), move.getReference()));
    }

    if (journal == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(IExceptionMessage.MOVE_2), move.getReference()));
    }

    if (move.getPeriod() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(IExceptionMessage.MOVE_4), move.getReference()));
    }

    if (move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(I18n.get(IExceptionMessage.MOVE_8), move.getReference()));
    }

    if (move.getCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(IExceptionMessage.MOVE_12), move.getReference()));
    }

    if (move.getMoveLineList().stream()
        .allMatch(
            moveLine ->
                moveLine.getDebit().add(moveLine.getCredit()).compareTo(BigDecimal.ZERO) == 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(I18n.get(IExceptionMessage.MOVE_8), move.getReference()));
    }
  }
}
