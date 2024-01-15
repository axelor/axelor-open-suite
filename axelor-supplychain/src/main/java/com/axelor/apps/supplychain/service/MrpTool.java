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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;

/** Utility class for computing MRP fields. */
public class MrpTool {

  private MrpTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute full name of a given MRP using {@link Mrp#mrpSeq} and {@link Mrp#name}. This method
   * manages the case where these fields are null or empty.
   *
   * @param mrp a MRP
   * @return the computed full name
   * @throws NullPointerException if the given MRP is null
   */
  public static String computeFullName(Mrp mrp) {
    if (mrp.getMrpSeq() == null || "".equals(mrp.getMrpSeq())) {
      return mrp.getName();
    }
    String fullName = mrp.getMrpSeq();
    if (mrp.getName() != null && !"".equals(mrp.getName())) {
      fullName = fullName + " - " + mrp.getName();
    }
    return fullName;
  }
}
