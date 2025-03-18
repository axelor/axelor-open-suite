/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MoveLineMassEntryService {

  void generateTaxLineAndCounterpart(
      Move parentMove, Move childMove, LocalDate dueDate, Integer temporaryMoveNumber)
      throws AxelorException;

  BigDecimal computeCurrentRate(
      BigDecimal currencyRate,
      Company company,
      List<MoveLineMassEntry> moveLineList,
      Currency currency,
      Currency companyCurrency,
      Integer temporaryMoveNumber,
      LocalDate originDate)
      throws AxelorException;

  User getPfpValidatorUserForInTaxAccount(Account account, Company company, Partner partner);

  void setPfpValidatorUserForInTaxAccount(
      List<MoveLineMassEntry> moveLineMassEntryList, Company company, int temporaryMoveNumber);

  MoveLineMassEntry createMoveLineMassEntry(Company company);
}
