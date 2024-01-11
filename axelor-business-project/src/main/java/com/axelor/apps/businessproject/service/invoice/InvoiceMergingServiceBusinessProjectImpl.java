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
package com.axelor.apps.businessproject.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class InvoiceMergingServiceBusinessProjectImpl extends InvoiceMergingServiceSupplychainImpl {

  private final PurchaseOrderInvoiceProjectServiceImpl purchaseOrderInvoiceProjectServiceImpl;
  private final SaleOrderInvoiceProjectServiceImpl saleOrderInvoiceProjectServiceImpl;

  @Inject
  public InvoiceMergingServiceBusinessProjectImpl(
      InvoiceService invoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceProjectServiceImpl purchaseOrderInvoiceProjectServiceImpl,
      SaleOrderInvoiceProjectServiceImpl saleOrderInvoiceProjectServiceImpl) {
    super(invoiceService, purchaseOrderInvoiceService, saleOrderInvoiceService);
    this.purchaseOrderInvoiceProjectServiceImpl = purchaseOrderInvoiceProjectServiceImpl;
    this.saleOrderInvoiceProjectServiceImpl = saleOrderInvoiceProjectServiceImpl;
  }

  protected static class CommonFieldsBusinessProjectImpl extends CommonFieldsSupplychainImpl {
    private Project commonProject;

    public Project getCommonProject() {
      return commonProject;
    }

    public void setCommonProject(Project commonProject) {
      this.commonProject = commonProject;
    }
  }

  protected static class ChecksBusinessProjectImpl extends ChecksSupplychainImpl {
    private boolean existProjectDiff = false;

    public boolean isExistProjectDiff() {
      return existProjectDiff;
    }

    public void setExistProjectDiff(boolean existProjectDiff) {
      this.existProjectDiff = existProjectDiff;
    }
  }

  protected static class InvoiceMergingResultBusinessProjectImpl
      extends InvoiceMergingResultSupplychainImpl {
    private final CommonFieldsBusinessProjectImpl commonFields;
    private final ChecksBusinessProjectImpl checks;

    public InvoiceMergingResultBusinessProjectImpl() {
      super();
      this.commonFields = new CommonFieldsBusinessProjectImpl();
      this.checks = new ChecksBusinessProjectImpl();
    }
  }

  @Override
  public InvoiceMergingResultBusinessProjectImpl create() {
    return new InvoiceMergingResultBusinessProjectImpl();
  }

  @Override
  public CommonFieldsBusinessProjectImpl getCommonFields(InvoiceMergingResult result) {
    return ((InvoiceMergingResultBusinessProjectImpl) result).commonFields;
  }

  @Override
  public ChecksBusinessProjectImpl getChecks(InvoiceMergingResult result) {
    return ((InvoiceMergingResultBusinessProjectImpl) result).checks;
  }

  @Override
  protected void extractFirstNonNullCommonFields(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) {
    super.extractFirstNonNullCommonFields(invoicesToMerge, result);
    invoicesToMerge.stream()
        .map(Invoice::getProject)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(it -> getCommonFields(result).setCommonProject(it));
  }

  @Override
  protected void fillCommonFields(Invoice invoice, InvoiceMergingResult result) {
    super.fillCommonFields(invoice, result);
    if (getCommonFields(result).getCommonProject() != null
        && !getCommonFields(result).getCommonProject().equals(invoice.getProject())) {
      getCommonFields(result).setCommonProject(null);
      getChecks(result).setExistProjectDiff(true);
    }
  }

  @Override
  protected void checkErrors(StringJoiner fieldErrors, InvoiceMergingResult result)
      throws AxelorException {
    super.checkErrors(fieldErrors, result);
    if (getCommonFields(result).getCommonProject() == null
        && getChecks(result).isExistProjectDiff()) {
      fieldErrors.add(I18n.get(AccountExceptionMessage.INVOICE_MERGE_ERROR_PROJECT));
    }
  }

  @Override
  protected Invoice generateMergedInvoice(
      List<Invoice> invoicesToMerge, InvoiceMergingResult result) throws AxelorException {
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      return purchaseOrderInvoiceProjectServiceImpl.mergeInvoice(
          invoicesToMerge,
          getCommonFields(result).getCommonCompany(),
          getCommonFields(result).getCommonCurrency(),
          getCommonFields(result).getCommonPartner(),
          getCommonFields(result).getCommonContactPartner(),
          getCommonFields(result).getCommonPriceList(),
          getCommonFields(result).getCommonPaymentMode(),
          getCommonFields(result).getCommonPaymentCondition(),
          getCommonFields(result).getCommonTradingName(),
          getCommonFields(result).getCommonFiscalPosition(),
          getCommonFields(result).getCommonSupplierInvoiceNb(),
          getCommonFields(result).getCommonOriginDate(),
          getCommonFields(result).getCommonPurchaseOrder(),
          getCommonFields(result).getCommonProject());
    }
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)) {
      return saleOrderInvoiceProjectServiceImpl.mergeInvoice(
          invoicesToMerge,
          getCommonFields(result).getCommonCompany(),
          getCommonFields(result).getCommonCurrency(),
          getCommonFields(result).getCommonPartner(),
          getCommonFields(result).getCommonContactPartner(),
          getCommonFields(result).getCommonPriceList(),
          getCommonFields(result).getCommonPaymentMode(),
          getCommonFields(result).getCommonPaymentCondition(),
          getCommonFields(result).getCommonTradingName(),
          getCommonFields(result).getCommonFiscalPosition(),
          getCommonFields(result).getCommonSaleOrder(),
          getCommonFields(result).getCommonProject());
    }
    return null;
  }
}
