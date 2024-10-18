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

  @Inject
  public InvoiceTermPfpValidateServiceImpl(
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      AppBaseService appBaseService,
      InvoiceTermRepository invoiceTermRepository) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.appBaseService = appBaseService;
    this.invoiceTermRepository = invoiceTermRepository;
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
