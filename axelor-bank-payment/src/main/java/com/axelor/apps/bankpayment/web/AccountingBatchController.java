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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.service.batch.BillOfExchangeInvoiceTermQueryService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccountingBatchController {

  public void searchInvoicesToBeProcessed(ActionRequest request, ActionResponse response) {

    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Invoices to be processed"));
      StringBuilder sb = new StringBuilder();
      sb.append(
          "self.operationTypeSelect = :operationTypeSelectSale "
              + "AND self.amountRemaining > 0 "
              + "AND self.statusSelect = :statusSelect "
              + "AND self.company = :company "
              + "AND self.hasPendingPayments = false "
              + "AND self.paymentMode = :paymentMode "
              + "AND self.lcrAccounted = false");
      if (accountingBatch.getDueDate() != null) {
        sb.append(" AND self.dueDate <= :dueDate");
        actionViewBuilder.context("dueDate", accountingBatch.getDueDate());
      }
      if (accountingBatch.getBankDetails() != null) {
        ArrayList<Long> bankDetailsIds = new ArrayList<>();
        bankDetailsIds.add(accountingBatch.getBankDetails().getId());

        if (Beans.get(AppAccountService.class).getAppBase().getManageMultiBanks()
            && accountingBatch.getIncludeOtherBankAccounts()) {
          bankDetailsIds.addAll(
              accountingBatch.getCompany().getBankDetailsList().stream()
                  .map(bd -> bd.getId())
                  .collect(Collectors.toList()));
        }
        sb.append(" AND self.companyBankDetails IN (" + Joiner.on(",").join(bankDetailsIds) + ") ");
      }
      actionViewBuilder.context("statusSelect", InvoiceRepository.STATUS_VENTILATED);
      actionViewBuilder.context(
          "operationTypeSelectSale", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
      actionViewBuilder.context("company", accountingBatch.getCompany());
      actionViewBuilder.context("paymentMode", accountingBatch.getPaymentMode());

      // Also fetch related refund
      actionViewBuilder.context(
          "operationTypeSelectRefund", InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
      actionViewBuilder.context(
          "partnersIdList", getPartnersIdList(sb.toString(), accountingBatch));
      sb.append(
          " OR (self.operationTypeSelect = :operationTypeSelectRefund"
              + " AND self.partner.id IN (:partnersIdList)"
              + " AND self.amountRemaining > 0"
              + " AND self.statusSelect = :statusSelect) ");

      actionViewBuilder.model(Invoice.class.getName());
      actionViewBuilder.add("grid", "invoice-bill-of-exchange-batch-grid");
      actionViewBuilder.add("form", "invoice-form");
      actionViewBuilder.domain(sb.toString());

      response.setReload(true);
      response.setView(actionViewBuilder.map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void searchInvoiceTermsToBeProcessed(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      List<Long> invoiceTermIds = fetchEligibleInvoiceTermIds(accountingBatch);

      ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Invoice terms to be processed"));
      actionViewBuilder.model(InvoiceTerm.class.getName());
      actionViewBuilder.add("grid", "invoice-term-grid");
      actionViewBuilder.add("form", "invoice-term-form");
      actionViewBuilder.domain(invoiceTermIds.isEmpty() ? "self.id IN (0)" : "self.id IN (:ids)");
      if (!invoiceTermIds.isEmpty()) {
        actionViewBuilder.context("ids", invoiceTermIds);
      }
      response.setReload(true);
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected List<Long> fetchEligibleInvoiceTermIds(AccountingBatch accountingBatch) {
    List<Long> anomalyList = Lists.newArrayList(0L);
    List<Long> invoiceTermIds = new ArrayList<>();
    Query<InvoiceTerm> query =
        Beans.get(BillOfExchangeInvoiceTermQueryService.class)
            .buildOrderedQueryFetchEligibleInvoiceTerms(accountingBatch, anomalyList);

    List<InvoiceTerm> invoiceTermList;
    while (!(invoiceTermList =
            query.bind("anomalyList", anomalyList).fetch(AbstractBatch.FETCH_LIMIT))
        .isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        invoiceTermIds.add(invoiceTerm.getId());
        anomalyList.add(invoiceTerm.getId());
      }
      JPA.clear();
    }
    return invoiceTermIds;
  }

  protected List<Long> getPartnersIdList(String query, AccountingBatch accountingBatch) {
    return Beans.get(InvoiceRepository.class)
        .all()
        .filter(query)
        .bind("dueDate", accountingBatch.getDueDate())
        .bind("operationTypeSelectSale", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
        .bind("company", accountingBatch.getCompany())
        .bind("paymentMode", accountingBatch.getPaymentMode())
        .bind("statusSelect", InvoiceRepository.STATUS_VENTILATED)
        .fetchStream()
        .map(Invoice::getPartner)
        .map(Partner::getId)
        .collect(Collectors.toList());
  }
}
