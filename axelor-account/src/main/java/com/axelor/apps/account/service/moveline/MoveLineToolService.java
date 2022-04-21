package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineToolService {

  MoveLine getCreditCustomerMoveLine(Invoice invoice);

  List<MoveLine> getCreditCustomerMoveLines(Invoice invoice);

  MoveLine getCreditCustomerMoveLine(Move move);

  List<MoveLine> getCreditCustomerMoveLines(Move move);

  MoveLine getDebitCustomerMoveLine(Invoice invoice);

  List<MoveLine> getDebitCustomerMoveLines(Invoice invoice);

  MoveLine getDebitCustomerMoveLine(Move move);

  List<MoveLine> getDebitCustomerMoveLines(Move move);

  String determineDescriptionMoveLine(Journal journal, String origin, String description);

  List<MoveLine> getReconciliableCreditMoveLines(List<MoveLine> moveLineList);

  List<MoveLine> getReconciliableDebitMoveLines(List<MoveLine> moveLineList);

  TaxLine getTaxLine(MoveLine moveLine) throws AxelorException;

  MoveLine setCurrencyAmount(MoveLine moveLine);

  void checkDateInPeriod(Move move, MoveLine moveLine) throws AxelorException;
}
