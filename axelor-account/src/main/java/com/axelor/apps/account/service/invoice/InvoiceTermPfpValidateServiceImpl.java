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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceTermPfpValidateServiceImpl implements InvoiceTermPfpValidateService {

  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected AppBaseService appBaseService;
  protected InvoiceTermRepository invoiceTermRepository;
  protected InvoiceTermPfpService invoiceTermPfpService;

  @Inject
  public InvoiceTermPfpValidateServiceImpl(
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      AppBaseService appBaseService,
      InvoiceTermRepository invoiceTermRepository,
      InvoiceTermPfpService invoiceTermPfpService) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.appBaseService = appBaseService;
    this.invoiceTermRepository = invoiceTermRepository;
    this.invoiceTermPfpService = invoiceTermPfpService;
  }

  @Override
  @Transactional
  public Integer massValidatePfp(List<Long> invoiceTermIds) {
    List<InvoiceTerm> invoiceTermList = invoiceTermToolService.getInvoiceTerms(invoiceTermIds);
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (invoiceTermPfpToolService.canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        this.validatePfp(invoiceTerm, currentUser);
        updatedRecords++;
      }
    }

    return updatedRecords;
  }

  @Override
  public void validatePfp(InvoiceTerm invoiceTerm, User currentUser) {
    if (invoiceTermPfpToolService
        .getAlreadyValidatedStatusList()
        .contains(invoiceTerm.getPfpValidateStatusSelect())) {
      return;
    }

    Company company = invoiceTerm.getCompany();

    invoiceTerm.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(company).toLocalDateTime());
    invoiceTerm.setInitialPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
    if (currentUser != null) {
      invoiceTerm.setPfpValidatorUser(currentUser);
    }

    if (!ObjectUtils.isEmpty(invoiceTerm.getReasonOfRefusalToPay())
        && !ObjectUtils.isEmpty(invoiceTerm.getReasonOfRefusalToPayStr())) {
      invoiceTerm.setReasonOfRefusalToPay(null);
      invoiceTerm.setReasonOfRefusalToPayStr(null);
    }

    invoiceTermPfpService.refreshInvoicePfpStatus(invoiceTerm.getInvoice());
  }

  @Override
  @Transactional
  public void initPftPartialValidation(
      InvoiceTerm originalInvoiceTerm, BigDecimal grantedAmount, PfpPartialReason partialReason) {
    originalInvoiceTerm.setPfpfPartialValidationOk(true);
    originalInvoiceTerm.setPfpPartialValidationAmount(originalInvoiceTerm.getAmount());
    originalInvoiceTerm.setAmount(grantedAmount);
    originalInvoiceTerm.setPfpPartialReason(partialReason);
    originalInvoiceTerm.setPfpValidateStatusSelect(
        InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    originalInvoiceTerm.setInitialPfpAmount(originalInvoiceTerm.getAmountRemaining());
    originalInvoiceTerm.setAmountRemaining(grantedAmount);
    originalInvoiceTerm.setRemainingPfpAmount(
        originalInvoiceTerm.getInitialPfpAmount().subtract(grantedAmount));
    originalInvoiceTerm.setPercentage(
        invoiceTermToolService.computeCustomizedPercentage(
            grantedAmount,
            originalInvoiceTerm.getInvoice() != null
                ? originalInvoiceTerm.getInvoice().getInTaxTotal()
                : originalInvoiceTerm
                    .getMoveLine()
                    .getCredit()
                    .max(originalInvoiceTerm.getMoveLine().getDebit())));

    if (originalInvoiceTerm.getApplyFinancialDiscount()) {
      invoiceTermFinancialDiscountService.computeFinancialDiscount(originalInvoiceTerm);
    }

    invoiceTermRepository.save(originalInvoiceTerm);
  }
}
