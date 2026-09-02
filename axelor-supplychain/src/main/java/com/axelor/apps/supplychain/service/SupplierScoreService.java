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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import java.math.BigDecimal;

public interface SupplierScoreService {

  /**
   * Recompute the automatic supplier indicators of the partner over the rolling period configured
   * in the Supply Chain app, then its global supplier score, and save the partner. Does nothing
   * when the feature is disabled or when the partner is not a supplier.
   */
  void computeAndSave(Partner partner);

  /**
   * Compute the global supplier score from the indicators currently held by the partner and the
   * weights configured in the Supply Chain app, without persisting anything.
   *
   * @return the score between 0 and 100, or null when no indicator is available.
   */
  BigDecimal computeGlobalScore(Partner partner);
}
