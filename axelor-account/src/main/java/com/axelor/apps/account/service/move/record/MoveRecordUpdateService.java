package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MoveRecordUpdateService {

  void updatePartner(Move move);

  MoveContext updateInvoiceTerms(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException;

  void updateRoundInvoiceTermPercentages(Move move);

  void updateDueDate(Move move, LocalDate dueDate);

  void updateInDayBookMode(Move move) throws AxelorException;

  void updateMoveLinesCurrencyRate(Move move, LocalDate dueDate) throws AxelorException;
}
