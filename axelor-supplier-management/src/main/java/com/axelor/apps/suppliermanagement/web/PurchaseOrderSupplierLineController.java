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
package com.axelor.apps.suppliermanagement.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.suppliermanagement.db.PurchaseOrderSupplierLine;
import com.axelor.apps.suppliermanagement.db.repo.PurchaseOrderSupplierLineRepository;
import com.axelor.apps.suppliermanagement.service.PurchaseOrderSupplierLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

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
    PurchaseOrderLine purchaseOrderLine = getPurchaseOrderLine(request);
    PurchaseOrder purchaseOrder = getPurchaseOrder(request, purchaseOrderLine);

    if (purchaseOrderLine == null || purchaseOrder == null) {
      response.setAttr("supplierPartner", "domain", "self.id = 0");
      return;
    }

    Company company = purchaseOrder.getCompany();

    String domain = "";
    Boolean manageSupplierCatalog =
        Beans.get(AppPurchaseService.class).getAppPurchase().getManageSupplierCatalog();
    List<SupplierCatalog> supplierCatalogList =
        Optional.ofNullable(purchaseOrderLine.getProduct())
            .map(Product::getSupplierCatalogList)
            .orElse(List.of());
    if (manageSupplierCatalog && CollectionUtils.isNotEmpty(supplierCatalogList)) {
      domain +=
          "self.id != "
              + company.getPartner().getId()
              + " AND self.id IN "
              + supplierCatalogList.stream()
                  .map(SupplierCatalog::getSupplierPartner)
                  .map(Partner::getId)
                  .map(String::valueOf)
                  .collect(Collectors.joining(",", "(", ")"));

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

  private PurchaseOrderLine getPurchaseOrderLine(ActionRequest request) {
    PurchaseOrderSupplierLine purchaseOrderSupplierLine =
        request.getContext().asType(PurchaseOrderSupplierLine.class);

    PurchaseOrderLine purchaseOrderLine = purchaseOrderSupplierLine.getPurchaseOrderLine();
    Context parent = request.getContext().getParent();
    if (purchaseOrderLine == null
        && parent != null
        && parent.getContextClass() == PurchaseOrderLine.class) {
      purchaseOrderLine = parent.asType(PurchaseOrderLine.class);
    }
    return purchaseOrderLine;
  }

  private PurchaseOrder getPurchaseOrder(
      ActionRequest request, PurchaseOrderLine purchaseOrderLine) {

    PurchaseOrder purchaseOrder = null;

    Context parentContext =
        Optional.ofNullable(request.getContext())
            .map(Context::getParent)
            .map(Context::getParent)
            .orElse(null);

    if (parentContext != null && parentContext.getContextClass() == PurchaseOrder.class) {
      purchaseOrder = parentContext.asType(PurchaseOrder.class);
    }

    if (purchaseOrderLine != null
        && purchaseOrderLine.getPurchaseOrder() != null
        && purchaseOrderLine.getPurchaseOrder().getId() != null) {
      purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    }

    return purchaseOrder;
  }
}
