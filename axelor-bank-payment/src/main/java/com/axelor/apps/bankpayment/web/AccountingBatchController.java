package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AccountingBatchController {

  public void searchInvoicesToBeProcessed(ActionRequest request, ActionResponse response) {

    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);

      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Invoices to be processed"));
      StringBuilder sb = new StringBuilder();
      sb.append(
          "self.operationTypeSelect = :operationTypeSelect "
              + "AND self.amountRemaining > 0 "
              + "AND self.company = :company "
              + "AND self.hasPendingPayments = false "
              + "AND self.paymentMode = :paymentMode "
              + "AND self.lcrAccounted = :lcrAccounted ");
      if (accountingBatch.getDueDate() != null) {
        sb.append(" AND self.dueDate < :dueDate");
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
      actionViewBuilder.context(
          "operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
      actionViewBuilder.context("company", accountingBatch.getCompany());
      actionViewBuilder.context("paymentMode", accountingBatch.getPaymentMode());
      actionViewBuilder.context(
          "lcrAccounted",
          accountingBatch.getBillOfExchangeStepBatchSelect()
                  == AccountingBatchRepository.BILL_OF_EXCHANGE_BATCH_STATUS_BOE_GENERATION
              ? false
              : true);
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
}
