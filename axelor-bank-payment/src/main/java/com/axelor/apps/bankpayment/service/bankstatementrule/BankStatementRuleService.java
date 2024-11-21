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
package com.axelor.apps.bankpayment.service.bankstatementrule;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.util.Optional;

public interface BankStatementRuleService {

  /**
   * Method to get the partner from bankstatementrule. bankReconciliationLine.bankStatementLine is
   * used for the context of the formula.
   *
   * @param bankStatementRule: can not be null
   * @param bankReconciliationLine: can not be null
   * @return Optional of partner : {@link Partner}
   * @throws AxelorException if the formula can not be eval to a partner
   */
  Optional<Partner> getPartner(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine)
      throws AxelorException;

  /**
   * Method to get the MoveLine from bankstatementrule. bankReconciliationLine.bankStatementLine is
   * used for the context of the formula.
   *
   * @param bankStatementRule: can not be null
   * @param bankReconciliationLine: can not be null
   * @param move : will be usable in context, can not be null
   * @return Optional of MoveLine : {@link MoveLine}
   * @throws AxelorException if the formula can not be eval to a MoveLine
   */
  Optional<MoveLine> getMoveLine(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine, Move move)
      throws AxelorException;
}
