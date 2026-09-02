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
package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.ContractLine;
import java.util.List;

public interface ContractLinePackService {

  /**
   * Check whether the list contains at least one "End of pack" line.
   *
   * @param contractLineList the lines to inspect.
   * @return true if an "End of pack" line is present.
   */
  boolean hasEndOfPackTypeLine(List<ContractLine> contractLineList);

  /**
   * Compute the pack sub-totals: every "End of pack" line receives the sum of the standard lines
   * placed between it and the previous "Start of pack" line when its "Show total" option is
   * enabled, zero otherwise. Its quantity is forced to zero.
   *
   * @param contractLineList the lines to compute, ordered by sequence.
   */
  void computePackTotal(List<ContractLine> contractLineList);

  /**
   * Reset every "End of pack" line: totals set to zero and pack display options disabled. Used when
   * the pack management is disabled in the contract app.
   *
   * @param contractLineList the lines to reset.
   */
  void resetPackTotal(List<ContractLine> contractLineList);
}
