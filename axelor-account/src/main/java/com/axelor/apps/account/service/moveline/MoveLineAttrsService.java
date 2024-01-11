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
import java.util.Map;

public interface MoveLineAttrsService {
  void addAnalyticAxisAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addDescriptionRequired(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticAccountRequired(
      MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticDistributionTypeSelect(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addInvoiceTermListPercentageWarningText(
      MoveLine moveLine, Map<String, Map<String, Object>> attrsMap);

  void addReadonly(MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap);

  void addShowTaxAmount(MoveLine moveLine, Map<String, Map<String, Object>> attrsMap);

  void addShowAnalyticDistributionPanel(
      Move move, MoveLine moveLine, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addValidatePeriod(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addPartnerReadonly(MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap);

  void addAccountDomain(
      Journal journal, Company company, Map<String, Map<String, Object>> attrsMap);

  void addPartnerDomain(Move move, Map<String, Map<String, Object>> attrsMap);

  void addAnalyticDistributionTemplateDomain(Move move, Map<String, Map<String, Object>> attrsMap);

  void addTaxLineRequired(Move move, MoveLine moveLine, Map<String, Map<String, Object>> attrsMap);
}
