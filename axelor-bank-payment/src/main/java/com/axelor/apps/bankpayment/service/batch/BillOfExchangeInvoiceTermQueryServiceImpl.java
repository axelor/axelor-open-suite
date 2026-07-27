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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.db.Query;
import com.google.common.collect.Sets;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BillOfExchangeInvoiceTermQueryServiceImpl
    implements BillOfExchangeInvoiceTermQueryService {

  protected InvoiceTermRepository invoiceTermRepository;
  protected AppAccountService appAccountService;
  protected PartnerService partnerService;

  @Inject
  public BillOfExchangeInvoiceTermQueryServiceImpl(
      InvoiceTermRepository invoiceTermRepository,
      AppAccountService appAccountService,
      PartnerService partnerService) {
    this.invoiceTermRepository = invoiceTermRepository;
    this.appAccountService = appAccountService;
    this.partnerService = partnerService;
  }

  @Override
  public Query<InvoiceTerm> buildOrderedQueryFetchLcrAccountedInvoiceTerms(
      AccountingBatch accountingBatch) {
    StringBuilder filter = new StringBuilder();
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();
    filter
        .append("self.lcrAccounted = TRUE ")
        .append("AND self.paymentMode = :paymentMode ")
        .append("AND self.isPaid = FALSE AND self.amountRemaining > 0 ")
        .append("AND (self.invoice IS NULL OR self.invoice.hasPendingPayments = FALSE) ");

    Map<String, Object> bindings = new HashMap<>();
    bindings.put("paymentMode", accountingBatch.getPaymentMode());

    if (accountingBatch.getDueDate() != null) {
      filter.append("AND self.dueDate <= :dueDate ");
      bindings.put("dueDate", accountingBatch.getDueDate());
    }
    if (accountingBatch.getCurrency() != null) {
      filter.append("AND self.currency = :currency ");
      bindings.put("currency", accountingBatch.getCurrency());
    }
    if (accountingBatch.getCompany() != null) {
      filter.append("AND self.company = :company ");
      bindings.put("company", accountingBatch.getCompany());
    }
    if (accountingBatch.getBankDetails() != null) {
      filter.append(
          "AND ((self.invoice IS NOT NULL AND self.invoice.companyBankDetails IN (:bankDetailsSet)) "
              + "OR (self.invoice IS NULL AND self.moveLine.move.companyBankDetails IN (:bankDetailsSet))) ");
      Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());
      if (manageMultiBanks && accountingBatch.getIncludeOtherBankAccounts()) {
        bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsList());
      }
      bindings.put("bankDetailsSet", bankDetailsSet);
    }

    return invoiceTermRepository.all().filter(filter.toString()).bind(bindings).order("id");
  }

  @Override
  public Query<InvoiceTerm> buildOrderedQueryFetchEligibleInvoiceTerms(
      AccountingBatch accountingBatch, List<Long> anomalyList) {
    StringBuilder filter = new StringBuilder();
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();
    filter.append(
        "self.isPaid = FALSE "
            + "AND self.amountRemaining > 0 "
            + "AND self.lcrAccounted = FALSE "
            + "AND self.id NOT IN (:anomalyList) "
            + "AND self.paymentMode = :paymentMode "
            + "AND ("
            + "  (self.invoice IS NOT NULL "
            + "    AND self.invoice.operationTypeSelect = :operationTypeSelect "
            + "    AND self.invoice.statusSelect = :statusSelect "
            + "    AND self.invoice.hasPendingPayments = FALSE "
            + "    AND self.invoice.lcrAccounted = FALSE "
            + "    AND (self.invoice.billOfExchangeBlockingOk = FALSE OR (self.invoice.billOfExchangeBlockingOk = TRUE AND self.invoice.billOfExchangeBlockingToDate < :dueDate)) "
            + "  ) OR ("
            + "    self.invoice IS NULL "
            + "    AND self.moveLine.move.functionalOriginSelect = :functionalOriginSale "
            + "    AND self.moveLine.move.statusSelect IN (:daybookStatus, :accountedStatus)"
            + "  )"
            + ") ");

    Map<String, Object> bindings = new HashMap<>();
    bindings.put("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
    bindings.put("statusSelect", InvoiceRepository.STATUS_VENTILATED);
    bindings.put("paymentMode", accountingBatch.getPaymentMode());
    bindings.put("anomalyList", anomalyList);
    bindings.put("dueDate", accountingBatch.getDueDate());
    bindings.put("functionalOriginSale", MoveRepository.FUNCTIONAL_ORIGIN_SALE);
    bindings.put("daybookStatus", MoveRepository.STATUS_DAYBOOK);
    bindings.put("accountedStatus", MoveRepository.STATUS_ACCOUNTED);

    if (accountingBatch.getDueDate() != null) {
      filter.append("AND self.dueDate <= :dueDate ");
    }
    if (accountingBatch.getCurrency() != null) {
      filter.append("AND self.currency = :currency ");
      bindings.put("currency", accountingBatch.getCurrency());
    }
    if (accountingBatch.getCompany() != null) {
      filter.append("AND self.company = :company ");
      bindings.put("company", accountingBatch.getCompany());
    }

    if (accountingBatch.getBankDetails() != null) {
      filter.append(
          "AND ((self.invoice IS NOT NULL AND self.invoice.companyBankDetails IN (:bankDetailsSet)) "
              + "OR (self.invoice IS NULL AND self.moveLine.move.companyBankDetails IN (:bankDetailsSet))) ");
      Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());
      if (manageMultiBanks && accountingBatch.getIncludeOtherBankAccounts()) {
        bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsList());
      }
      bindings.put("bankDetailsSet", bankDetailsSet);
    }

    return invoiceTermRepository.all().filter(filter.toString()).bind(bindings).order("id");
  }

  @Override
  public BankDetails getReceiverBankDetails(InvoiceTerm invoiceTerm) {
    BankDetails bankDetails =
        Optional.of(invoiceTerm)
            .map(InvoiceTerm::getThirdPartyPayerPartner)
            .map(partnerService::getDefaultBankDetails)
            .orElse(invoiceTerm.getBankDetails());
    if (bankDetails == null && invoiceTerm.getInvoice() != null) {
      bankDetails = invoiceTerm.getInvoice().getBankDetails();
    }
    return bankDetails;
  }
}
