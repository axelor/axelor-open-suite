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

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.db.JPA;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class MrpLineTool {

  private MrpLineTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Return a saleOrderLine that's in the origins of a MrpLine. The origins are generally a list of
   * one element only, if this behavior changes think about changing the logic in this method
   *
   * @param mrpLine
   * @return
   */
  public static Optional<SaleOrderLine> getOriginSaleOrderLineInMrpLineOrigin(MrpLine mrpLine) {
    if (mrpLine == null) {
      return Optional.empty();
    }
    List<MrpLineOrigin> mrpLineOrigins = mrpLine.getMrpLineOriginList();

    if (mrpLineOrigins == null || CollectionUtils.isEmpty(mrpLineOrigins)) {
      return Optional.empty();
    }
    return mrpLineOrigins.stream()
        .filter(mlo -> SaleOrderLine.class.getName().equals(mlo.getRelatedToSelect()))
        .map(MrpLineOrigin::getRelatedToSelectId)
        .map(solId -> JPA.find(SaleOrderLine.class, solId))
        .findAny();
  }
}
