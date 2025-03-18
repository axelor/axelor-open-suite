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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.Map;

public class InvoiceLineSupplierCatalogServiceImpl implements InvoiceLineSupplierCatalogService {

  protected final SupplierCatalogService supplierCatalogService;

  @Inject
  public InvoiceLineSupplierCatalogServiceImpl(SupplierCatalogService supplierCatalogService) {
    this.supplierCatalogService = supplierCatalogService;
  }

  @Override
  public void setSupplierCatalogInfo(
      Invoice invoice, InvoiceLine invoiceLine, Map<String, Object> productInformation)
      throws AxelorException {
    Integer operationType = invoice.getOperationTypeSelect();
    if ((operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)
        && supplierCatalogService.getSupplierCatalog(
                invoiceLine.getProduct(), invoice.getPartner(), invoice.getCompany())
            != null) {
      setSupplierCatalogProductInfo(productInformation, invoice, invoiceLine);
    }
  }

  protected void setSupplierCatalogProductInfo(
      Map<String, Object> productInformation, Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Product product = invoiceLine.getProduct();
    Partner supplierPartner = invoice.getPartner();
    Company company = invoice.getCompany();

    Map<String, String> productSupplierInfos =
        supplierCatalogService.getProductSupplierInfos(supplierPartner, company, product);
    if (productSupplierInfos.get("productName") != null
        && !productSupplierInfos.get("productName").isEmpty()) {
      productInformation.put("productName", productSupplierInfos.get("productName"));
    }
    if (productSupplierInfos.get("productCode") != null
        && !productSupplierInfos.get("productCode").isEmpty()) {
      productInformation.put("productCode", productSupplierInfos.get("productCode"));
    }
    productInformation.put("qty", supplierCatalogService.getQty(product, supplierPartner, company));
    productInformation.put(
        "price",
        supplierCatalogService.getUnitPrice(
            product,
            supplierPartner,
            company,
            invoice.getCurrency(),
            invoice.getInvoiceDate(),
            invoiceLine.getTaxLineSet(),
            false));
    productInformation.put(
        "inTaxPrice",
        supplierCatalogService.getUnitPrice(
            product,
            supplierPartner,
            company,
            invoice.getCurrency(),
            invoice.getInvoiceDate(),
            invoiceLine.getTaxLineSet(),
            true));
  }

  @Override
  public void checkMinQty(
      Invoice invoice, InvoiceLine invoiceLine, ActionRequest request, ActionResponse response)
      throws AxelorException {
    Integer operationType = invoice.getOperationTypeSelect();
    if (operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      supplierCatalogService.checkMinQty(
          invoiceLine.getProduct(),
          invoice.getPartner(),
          invoice.getCompany(),
          invoiceLine.getQty(),
          request,
          response);
    }
  }

  @Override
  public Map<String, Object> updateInfoFromCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    return supplierCatalogService.updateInfoFromCatalog(
        invoiceLine.getProduct(),
        invoiceLine.getQty(),
        invoice.getPartner(),
        invoice.getCurrency(),
        invoice.getInvoiceDate(),
        invoice.getCompany());
  }

  @Override
  public SupplierCatalog getSupplierCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    return supplierCatalogService.getSupplierCatalog(
        invoiceLine.getProduct(), invoice.getPartner(), invoice.getCompany());
  }
}
