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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;

public class SaleOrderCopyProjectServiceImpl implements SaleOrderCopyProjectService {

  protected final AppBusinessProjectService appBusinessProjectService;

  @Inject
  public SaleOrderCopyProjectServiceImpl(AppBusinessProjectService appBusinessProjectService) {
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public void copySaleOrderProjectProcess(SaleOrder copy) {
    if (appBusinessProjectService.isApp("business-project")) {
      copy.setProject(null);
      if (copy.getSaleOrderLineList() != null) {
        for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
          saleOrderLine.setProject(null);
        }
      }
    }
  }
}
