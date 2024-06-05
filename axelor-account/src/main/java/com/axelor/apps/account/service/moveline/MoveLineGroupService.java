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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.time.LocalDate;
import java.util.Map;

public interface MoveLineGroupService {
  Map<String, Object> getOnNewValuesMap(MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadMoveAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getDebitCreditInvoiceTermOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException;

  Map<String, Object> getAccountOnChangeValuesMap(
      MoveLine moveLine,
      Move move,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate,
      LocalDate dueDate)
      throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAnalyticAxisOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticAxisOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getDateOnChangeValuesMap(MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getCurrencyAmountRateOnChangeValuesMap(
      MoveLine moveLine, Move move, LocalDate dueDate) throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnSelectAttrsMap(Journal journal, Company company);

  Map<String, Map<String, Object>> getPartnerOnSelectAttrsMap(MoveLine moveLine, Move move);

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnSelectAttrsMap(Move move);

  Map<String, Object> getOnLoadAnalyticDistributionValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAnalyticDistributionAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getDebitOnChangeValuesMap(MoveLine moveLine, Move move, LocalDate dueDate)
      throws AxelorException;

  Map<String, Object> getCreditOnChangeValuesMap(MoveLine moveLine, Move move, LocalDate dueDate)
      throws AxelorException;

  Map<String, Object> getPartnerOnChangeValuesMap(MoveLine moveLine);

  Map<String, Object> getAnalyticDistributionTemplateOnChangeLightValuesMap(MoveLine moveLine);

  Map<String, Object> getAnalyticMoveLineOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticMoveLineOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  void computeDateOnChangeValues(MoveLine moveLine, Move move) throws AxelorException;
}
