/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
