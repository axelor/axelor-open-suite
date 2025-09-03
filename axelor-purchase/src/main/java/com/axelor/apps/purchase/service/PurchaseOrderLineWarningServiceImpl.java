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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.google.inject.Inject;

public class PurchaseOrderLineWarningServiceImpl implements PurchaseOrderLineWarningService {

  protected final SupplierCatalogService supplierCatalogService;

  @Inject
  public PurchaseOrderLineWarningServiceImpl(SupplierCatalogService supplierCatalogService) {
    this.supplierCatalogService = supplierCatalogService;
  }

  @Override
  public boolean checkLineIssue(PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    Product product = purchaseOrderLine.getProduct();

    return checkDefaultSupplierPartner(purchaseOrder, product)
        || checkSupplierCatalogUnit(purchaseOrderLine, purchaseOrder);
  }

  @Override
  public boolean checkSupplierCatalogUnit(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {
    Unit unit = purchaseOrderLine.getUnit();
    Product product = purchaseOrderLine.getProduct();
    Partner supplierPartner = purchaseOrder.getSupplierPartner();
    Company company = purchaseOrder.getCompany();
    SupplierCatalog supplierCatalog =
        supplierCatalogService.getSupplierCatalog(product, supplierPartner, company);
    if (supplierCatalog == null) {
      return false;
    }

    Unit supplierCatalogUnit = supplierCatalog.getUnit();
    if (supplierCatalogUnit == null) {
      return false;
    }

    return !unit.equals(supplierCatalogUnit);
  }

  protected boolean checkDefaultSupplierPartner(PurchaseOrder purchaseOrder, Product product) {
    return purchaseOrder != null
        && product != null
        && product.getDefaultSupplierPartner() != null
        && purchaseOrder.getSupplierPartner() != null
        && product.getDefaultSupplierPartner() != purchaseOrder.getSupplierPartner();
  }
}
