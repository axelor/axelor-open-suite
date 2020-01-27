/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PurchaseOrderLineServiceProjectImpl extends PurchaseOrderLineServiceSupplychainImpl
    implements PurchaseOrderLineProjectService {

  @Inject private PurchaseOrderLineRepository purchaseOrderLineRepo;

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    PurchaseOrderLine line = super.createPurchaseOrderLine(purchaseOrder, saleOrderLine);

    if (line != null
        && saleOrderLine != null
        && Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      line.setProject(saleOrderLine.getProject());
    }

    return line;
  }

  @Transactional
  @Override
  public void setProject(List<Long> purchaseOrderLineIds, Project project) {

    if (purchaseOrderLineIds != null) {

      List<PurchaseOrderLine> purchaseOrderLineList =
          purchaseOrderLineRepo.all().filter("self.id in ?1", purchaseOrderLineIds).fetch();

      for (PurchaseOrderLine line : purchaseOrderLineList) {
        line.setProject(project);
        purchaseOrderLineRepo.save(line);
      }
    }
  }
}
