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
package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;

public class QIIdentificationRequest extends RequestStructure {

  protected Long supplierPartnerId;
  protected Long supplierPurchaseOrderId;
  protected Long supplierPurchaseOrderLineId;
  protected Long customerPartnerId;
  protected Long customerSaleOrderId;
  protected Long customerSaleOrderLineId;
  protected Long manufOrderId;
  protected Long operationOrderId;
  protected Long productId;
  protected BigDecimal nonConformingQuantity;

  public Long getSupplierPartnerId() {
    return supplierPartnerId;
  }

  public void setSupplierPartnerId(Long supplierPartnerId) {
    this.supplierPartnerId = supplierPartnerId;
  }

  public Long getSupplierPurchaseOrderId() {
    return supplierPurchaseOrderId;
  }

  public void setSupplierPurchaseOrderId(Long supplierPurchaseOrderId) {
    this.supplierPurchaseOrderId = supplierPurchaseOrderId;
  }

  public Long getSupplierPurchaseOrderLineId() {
    return supplierPurchaseOrderLineId;
  }

  public void setSupplierPurchaseOrderLineId(Long supplierPurchaseOrderLineId) {
    this.supplierPurchaseOrderLineId = supplierPurchaseOrderLineId;
  }

  public Long getCustomerPartnerId() {
    return customerPartnerId;
  }

  public void setCustomerPartnerId(Long customerPartnerId) {
    this.customerPartnerId = customerPartnerId;
  }

  public Long getCustomerSaleOrderId() {
    return customerSaleOrderId;
  }

  public void setCustomerSaleOrderId(Long customerSaleOrderId) {
    this.customerSaleOrderId = customerSaleOrderId;
  }

  public Long getCustomerSaleOrderLineId() {
    return customerSaleOrderLineId;
  }

  public void setCustomerSaleOrderLineId(Long customerSaleOrderLineId) {
    this.customerSaleOrderLineId = customerSaleOrderLineId;
  }

  public Long getManufOrderId() {
    return manufOrderId;
  }

  public void setManufOrderId(Long manufOrderId) {
    this.manufOrderId = manufOrderId;
  }

  public Long getOperationOrderId() {
    return operationOrderId;
  }

  public void setOperationOrderId(Long operationOrderId) {
    this.operationOrderId = operationOrderId;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public BigDecimal getNonConformingQuantity() {
    return nonConformingQuantity;
  }

  public void setNonConformingQuantity(BigDecimal nonConformingQuantity) {
    this.nonConformingQuantity = nonConformingQuantity;
  }

  public Partner fetchSupplierPartner() {
    if (supplierPartnerId == null || supplierPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, supplierPartnerId, ObjectFinder.NO_VERSION);
  }

  public PurchaseOrder fetchSupplierPurchaseOrder() {
    if (supplierPurchaseOrderId == null || supplierPurchaseOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(PurchaseOrder.class, supplierPurchaseOrderId, ObjectFinder.NO_VERSION);
  }

  public PurchaseOrderLine fetchSupplierPurchaseOrderLine() {
    if (supplierPurchaseOrderLineId == null || supplierPurchaseOrderLineId == 0L) {
      return null;
    }
    return ObjectFinder.find(
        PurchaseOrderLine.class, supplierPurchaseOrderLineId, ObjectFinder.NO_VERSION);
  }

  public Partner fetchCustomerPartner() {
    if (customerPartnerId == null || customerPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, customerPartnerId, ObjectFinder.NO_VERSION);
  }

  public SaleOrder fetchCustomerSaleOrder() {
    if (customerSaleOrderId == null || customerSaleOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(SaleOrder.class, customerSaleOrderId, ObjectFinder.NO_VERSION);
  }

  public SaleOrderLine fetchCustomerSaleOrderLine() {
    if (customerSaleOrderLineId == null || customerSaleOrderLineId == 0L) {
      return null;
    }
    return ObjectFinder.find(SaleOrderLine.class, customerSaleOrderLineId, ObjectFinder.NO_VERSION);
  }

  public ManufOrder fetchManufOrder() {
    if (manufOrderId == null || manufOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(ManufOrder.class, manufOrderId, ObjectFinder.NO_VERSION);
  }

  public OperationOrder fetchOperationOrder() {
    if (operationOrderId == null || operationOrderId == 0L) {
      return null;
    }
    return ObjectFinder.find(OperationOrder.class, operationOrderId, ObjectFinder.NO_VERSION);
  }

  public Product fetchProduct() {
    if (productId == null || productId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }
}
