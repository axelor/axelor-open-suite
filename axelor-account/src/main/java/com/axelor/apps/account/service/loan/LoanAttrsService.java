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
import java.util.Map;

public interface LoanAttrsService {

  Map<String, Map<String, Object>> getTotalsAttrsMap(Loan loan);

  Map<String, Map<String, Object>> getConsistencyAttrsMap(Loan loan);

  /**
   * Titles of the shared deferral panel and its buttons, adapted to the loan status: a deferral
   * negociated during the setting phase (Draft) is worded as a "différé", a repayment-phase one as
   * a "report".
   */
  Map<String, Map<String, Object>> getDeferralTitlesAttrsMap(Loan loan);
}
