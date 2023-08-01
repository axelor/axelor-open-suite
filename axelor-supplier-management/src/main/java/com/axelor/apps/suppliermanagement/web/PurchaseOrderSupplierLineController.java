/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.suppliermanagement.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.suppliermanagement.db.PurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.repo.PurchaseOrderSupplierLineRepository;
import com.axelor.apps.suppliermanagement.service.PurchaseOrderSupplierLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.util.stream.Collectors;

@Singleton
public class PurchaseOrderSupplierLineController {

  public void accept(ActionRequest request, ActionResponse response) {

    PurchaseOrderSupplierLine purchaseOrderSupplierLine =
        Beans.get(PurchaseOrderSupplierLineRepository.class)
            .find(request.getContext().asType(PurchaseOrderSupplierLine.class).getId());

    if (purchaseOrderSupplierLine.getPurchaseOrderLine() == null
        && request.getContext().getParent() != null) {
      purchaseOrderSupplierLine.setPurchaseOrderLine(
          Beans.get(PurchaseOrderLineRepository.class)
              .find(request.getContext().getParent().asType(PurchaseOrderLine.class).getId()));
    }

    try {
      Beans.get(PurchaseOrderSupplierLineService.class).accept(purchaseOrderSupplierLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on supplier partner select. Set the domain for the field supplierPartner
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDomain(ActionRequest request, ActionResponse response) {
    PurchaseOrderSupplierLine purchaseOrderSupplierLine =
        request.getContext().asType(PurchaseOrderSupplierLine.class);

    PurchaseOrderLine purchaseOrderLine = purchaseOrderSupplierLine.getPurchaseOrderLine();
    if (purchaseOrderLine == null) {
      purchaseOrderLine = request.getContext().getParent().asType(PurchaseOrderLine.class);
    }

    PurchaseOrder purchaseOrder =
        request.getContext().getParent().getParent().asType(PurchaseOrder.class);
    if (purchaseOrder.getId() != null) {
      purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    }
    Company company = purchaseOrder.getCompany();

    String domain = "";
    if (Beans.get(AppPurchaseService.class).getAppPurchase().getManageSupplierCatalog()
        && purchaseOrderLine.getProduct() != null
        && !purchaseOrderLine.getProduct().getSupplierCatalogList().isEmpty()) {
      domain +=
          "self.id != "
              + company.getPartner().getId()
              + " AND self.id IN "
              + purchaseOrderLine.getProduct().getSupplierCatalogList().stream()
                  .map(s -> s.getSupplierPartner().getId())
                  .collect(Collectors.toList())
                  .toString()
                  .replace('[', '(')
                  .replace(']', ')');

      String blockedPartnerQuery =
          Beans.get(BlockingService.class)
              .listOfBlockedPartner(company, BlockingRepository.PURCHASE_BLOCKING);

      if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
        domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
      }
    } else {
      domain += "self.id = 0";
    }

    domain += " AND " + company.getId() + " in (SELECT id FROM self.companySet)";

    response.setAttr("supplierPartner", "domain", domain);
  }
}
