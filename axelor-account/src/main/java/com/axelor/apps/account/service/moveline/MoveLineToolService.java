/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.rpc.Context;
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

  boolean checkCutOffDates(MoveLine moveLine);

  boolean isEqualTaxMoveLine(
      Account account, TaxLine taxLine, Integer vatSystem, Long id, MoveLine ml);

  void checkDateInPeriod(Move move, MoveLine moveLine) throws AxelorException;

  void setAmountRemainingReconciliableMoveLines(Context context);

  List<MoveLine> getMoveExcessDueList(
      boolean excessPayment, Company company, Partner partner, Long invoiceId);
}
