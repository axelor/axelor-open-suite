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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Year;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public interface AccountingCloseAnnualService {

  public List<Move> generateCloseAndOpenAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean closeYear,
      boolean openYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException;

  public List<Move> generateCloseAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean closeYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException;

  public List<Move> generateOpenAnnualAccount(
      Year year,
      Account account,
      Partner partner,
      LocalDate endOfYearDate,
      LocalDate reportedBalanceDate,
      String origin,
      String moveDescription,
      boolean openYear,
      boolean allocatePerPartner,
      boolean isSimulatedMove)
      throws AxelorException;

  public List<Long> getAllAccountOfYear(Set<Account> accountSet, Year year);

  public List<Pair<Long, Long>> assignPartner(
      List<Long> accountIdList, Year year, boolean allocatePerPartner);

  void generateResultMove(
      Company company,
      LocalDate date,
      String description,
      BankDetails bankDetails,
      boolean simulateGeneratedMoves,
      BigDecimal amount)
      throws AxelorException;
}
