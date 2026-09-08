/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.base.AxelorException;

public interface LoanService {

  /** Generates the loan reference from the company loan sequence. */
  String generateReference(Loan loan) throws AxelorException;

  /**
   * Copies the journal and the accounts from the loan's selected configuration onto the loan. If no
   * configuration is selected, those fields are cleared.
   */
  void copyManagementConfigToLoan(Loan loan);

  /**
   * Resets the duplicate-specific fields when a loan is copied: status back to draft, reference and
   * computed schedule cleared.
   */
  void resetForCopy(Loan loan);
}
