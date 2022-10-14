package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MoveInvoiceTermService {
  public void generateInvoiceTerms(Move move) throws AxelorException;

  void roundInvoiceTermPercentages(Move move);

  boolean updateInvoiceTerms(Move move);

  void recreateInvoiceTerms(Move move) throws AxelorException;

  void updateMoveLineDueDates(Move move);

  boolean displayDueDate(Move move);

  LocalDate computeDueDate(Move move, boolean isSingleTerm, boolean isDateChange);

  void updateSingleInvoiceTermDueDate(Move move, LocalDate dueDate);

  void checkIfInvoiceTermInPayment(Move move) throws AxelorException;
}
