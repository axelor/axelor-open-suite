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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ManufOrderQueryServiceImpl implements ManufOrderQueryService {

  protected final ManufOrderService manufOrderService;

  @Inject
  public ManufOrderQueryServiceImpl(ManufOrderService manufOrderService) {
    this.manufOrderService = manufOrderService;
  }

  @Override
  public List<Long> getAllDescendantManufOrderIds(ManufOrder manufOrder) {
    List<Long> ids = new ArrayList<>();
    collectDescendantIds(manufOrder, ids);
    return ids;
  }

  protected void collectDescendantIds(ManufOrder manufOrder, List<Long> ids) {
    for (ManufOrder child : manufOrderService.getChildrenManufOrder(manufOrder)) {
      ids.add(child.getId());
      collectDescendantIds(child, ids);
    }
  }
}
