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
package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface MoveTemplateTaxService {

  /**
   * Checks if a move template line is a tax account line.
   *
   * @param line the move template line to check
   * @return true if the line is on a tax account, false otherwise
   */
  boolean isTaxAccountLine(MoveTemplateLine line);

  /**
   * Computes base amount excluding tax if computeTaxAtCreation is enabled.
   *
   * @param moveTemplateLine the template line containing tax configuration
   * @param moveDate the move date for tax line lookup
   * @param amount the original amount (TTC)
   * @param hasExplicitTaxLines whether the template has explicit tax lines
   * @return the computed base amount (HT if computeTaxAtCreation, unchanged otherwise)
   * @throws AxelorException if tax line lookup fails
   */
  BigDecimal computeBaseAmountExcludingTax(
      MoveTemplateLine moveTemplateLine,
      LocalDate moveDate,
      BigDecimal amount,
      boolean hasExplicitTaxLines)
      throws AxelorException;

  /**
   * Sets tax information on a base MoveLine.
   *
   * @param move the Move
   * @param moveLine the MoveLine to configure
   * @param moveTemplateLine the template line containing tax configuration
   * @param moveDate the move date for tax line lookup
   * @param linesToSetTaxLineAfterAutoGen map to store lines needing taxLineSet after auto
   *     generation
   * @throws AxelorException if tax line lookup fails
   */
  void setTaxInfoOnMoveLine(
      Move move,
      MoveLine moveLine,
      MoveTemplateLine moveTemplateLine,
      LocalDate moveDate,
      Map<MoveLine, TaxLine> linesToSetTaxLineAfterAutoGen)
      throws AxelorException;

  /**
   * Creates or updates a tax MoveLine from a tax template line.
   *
   * @param move the Move
   * @param moveTemplate the MoveTemplate
   * @param taxTemplateLine the tax template line
   * @param moveDate the move date
   * @param amount the tax amount
   * @param isDebit whether this is a debit line
   * @param counter the current line counter
   * @param origin the origin string for the MoveLine
   * @return the updated counter value
   * @throws AxelorException if tax line creation fails
   */
  int createOrUpdateTaxMoveLine(
      Move move,
      MoveTemplate moveTemplate,
      MoveTemplateLine taxTemplateLine,
      LocalDate moveDate,
      BigDecimal amount,
      boolean isDebit,
      int counter,
      String origin)
      throws AxelorException;
}
