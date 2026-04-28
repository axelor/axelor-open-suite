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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceCategoryServiceImpl implements InvoiceCategoryService {

  protected InvoiceRepository invoiceRepository;
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceCategoryServiceImpl(
      InvoiceRepository invoiceRepository, AccountConfigService accountConfigService) {
    this.invoiceRepository = invoiceRepository;
    this.accountConfigService = accountConfigService;
  }

  @Override
  @Transactional
  public void setInvoiceCategory(Invoice invoice) throws AxelorException {
    String invoiceCategory = computeInvoiceCategorySelect(invoice);
    invoice.setInvoiceCategorySelect(invoiceCategory);
    invoiceRepository.save(invoice);
  }

  protected String computeInvoiceCategorySelect(Invoice invoice) throws AxelorException {
    int operationType = invoice.getOperationTypeSelect();
    int operationSubType = invoice.getOperationSubTypeSelect();

    if ((operationType != InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            && operationType != InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
        || (operationSubType != InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT
            && operationSubType != InvoiceRepository.OPERATION_SUB_TYPE_BALANCE)) {
      return null;
    }

    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return getDefaultInvoiceCategorySelect(invoice);
    }

    Set<String> productTypes = new HashSet<>();

    for (InvoiceLine invoiceLine : invoiceLineList) {
      Product product = invoiceLine.getProduct();

      if (product == null || Strings.isNullOrEmpty(product.getProductTypeSelect())) {
        return getDefaultInvoiceCategorySelect(invoice);
      }

      productTypes.add(product.getProductTypeSelect());
    }

    if (productTypes.size() == 1) {
      String type = productTypes.iterator().next();

      if (ProductRepository.PRODUCT_TYPE_STORABLE.equals(type)) {
        return "goods";
      }

      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(type)) {
        return "services";
      }
    }

    return getDefaultInvoiceCategorySelect(invoice);
  }

  protected String getDefaultInvoiceCategorySelect(Invoice invoice) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    return accountConfig.getDefaultInvoiceCategorySelect();
  }
}
