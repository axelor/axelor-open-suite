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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceMergingService.InvoiceMergingResult;
import com.axelor.auth.db.AuditableModel;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InvoiceMergingViewServiceImpl implements InvoiceMergingViewService {

  protected InvoiceMergingService invoiceMergingService;

  @Inject
  public InvoiceMergingViewServiceImpl(InvoiceMergingService invoiceMergingService) {
    this.invoiceMergingService = invoiceMergingService;
  }

  protected String getMergeConfirmFormViewName(InvoiceMergingResult result) {
    if (result.getInvoiceType() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
      return "supplier-invoices-merge-confirm-form";
    }
    return "customer-invoices-merge-confirm-form";
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      InvoiceMergingResult result, List<Invoice> invoicesToMerge) {

    ActionViewBuilder confirmView =
        ActionView.define(I18n.get("Confirm merge invoice"))
            .model(Wizard.class.getName())
            .add("form", getMergeConfirmFormViewName(result))
            .param("popup", Boolean.TRUE.toString())
            .param("show-toolbar", Boolean.FALSE.toString())
            .param("show-confirm", Boolean.FALSE.toString())
            .param("popup-save", Boolean.FALSE.toString())
            .param("forceEdit", Boolean.TRUE.toString());

    if (invoiceMergingService.getChecks(result).isExistContactPartnerDiff()) {
      confirmView.context("contextContactPartnerToCheck", Boolean.TRUE.toString());
      confirmView.context(
          "contextPartnerId",
          Optional.ofNullable(invoiceMergingService.getCommonFields(result).getCommonPartner())
              .map(AuditableModel::getId)
              .map(Objects::toString)
              .orElse(null));
    }
    if (invoiceMergingService.getChecks(result).isExistPriceListDiff()) {
      confirmView.context("contextPriceListToCheck", Boolean.TRUE.toString());
    }
    if (invoiceMergingService.getChecks(result).isExistPaymentModeDiff()) {
      confirmView.context("contextPaymentModeToCheck", Boolean.TRUE.toString());
    }
    if (invoiceMergingService.getChecks(result).isExistPaymentConditionDiff()) {
      confirmView.context("contextPaymentConditionToCheck", Boolean.TRUE.toString());
    }
    if (invoiceMergingService.getChecks(result).isExistTradingNameDiff()) {
      confirmView.context("contextTradingNameToCheck", Boolean.TRUE.toString());
    }
    if (invoiceMergingService.getChecks(result).isExistFiscalPositionDiff()) {
      confirmView.context("contextFiscalPositionToCheck", Boolean.TRUE.toString());
    }
    if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
      if (invoiceMergingService.getChecks(result).isExistSupplierInvoiceNbDiff()) {
        confirmView.context("contextSupplierInvoiceNbToCheck", Boolean.TRUE.toString());
      }
      if (invoiceMergingService.getChecks(result).isExistOriginDateDiff()) {
        confirmView.context("contextOriginDateToCheck", Boolean.TRUE.toString());
      }
    }
    confirmView.context("invoiceToMerge", invoicesToMerge);
    return confirmView;
  }
}
