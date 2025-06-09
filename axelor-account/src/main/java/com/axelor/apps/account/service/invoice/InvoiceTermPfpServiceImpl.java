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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermPfpServiceImpl implements InvoiceTermPfpService {
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected MoveRepository moveRepo;
  protected PfpService pfpService;
  protected AppBaseService appBaseService;
  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected InvoiceTermToolService invoiceTermToolService;

  @Inject
  public InvoiceTermPfpServiceImpl(
      InvoiceTermService invoiceTermService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      MoveRepository moveRepo,
      PfpService pfpService,
      AppBaseService appBaseService,
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermToolService invoiceTermToolService) {
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.moveRepo = moveRepo;
    this.pfpService = pfpService;
    this.appBaseService = appBaseService;
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermToolService = invoiceTermToolService;
  }

  @Override
  public Integer massRefusePfp(
      List<Long> invoiceTermIds,
      CancelReason reasonOfRefusalToPay,
      String reasonOfRefusalToPayStr) {
    List<InvoiceTerm> invoiceTermList = invoiceTermToolService.getInvoiceTerms(invoiceTermIds);
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      boolean invoiceTermCheck =
          ObjectUtils.notEmpty(invoiceTerm.getCompany())
              && ObjectUtils.notEmpty(reasonOfRefusalToPay);

      if (invoiceTermCheck
          && invoiceTermPfpToolService.canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        refusalToPay(invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr);
        updatedRecords++;
      }
    }

    return updatedRecords;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr) {
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_LITIGATION);
    invoiceTerm.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(invoiceTerm.getCompany()).toLocalDateTime());
    invoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    invoiceTerm.setRemainingPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    invoiceTerm.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoiceTerm.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    refreshInvoicePfpStatus(invoiceTerm.getInvoice());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal grantedAmount,
      PfpPartialReason partialReason)
      throws AxelorException {
    BigDecimal amount = invoiceAmount.subtract(grantedAmount);
    Invoice invoice = originalInvoiceTerm.getInvoice();
    originalInvoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    InvoiceTerm newInvoiceTerm = this.createPfpInvoiceTerm(originalInvoiceTerm, invoice, amount);
    this.updateOriginalTerm(
        originalInvoiceTerm, newInvoiceTerm, grantedAmount, partialReason, amount, invoice);

    invoiceTermService.initInvoiceTermsSequence(invoice);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public InvoiceTerm createPfpInvoiceTerm(
      InvoiceTerm originalInvoiceTerm, Invoice invoice, BigDecimal amount) throws AxelorException {
    BigDecimal total;
    int sequence;

    if (invoice != null) {
      total = invoice.getInTaxTotal();
      sequence = invoice.getInvoiceTermList().size() + 1;
    } else {
      total =
          originalInvoiceTerm
              .getMoveLine()
              .getCredit()
              .max(originalInvoiceTerm.getMoveLine().getDebit());
      sequence = originalInvoiceTerm.getMoveLine().getInvoiceTermList().size() + 1;
    }

    BigDecimal percentage = invoiceTermToolService.computeCustomizedPercentage(amount, total);

    InvoiceTerm invoiceTerm =
        invoiceTermService.createInvoiceTerm(
            invoice,
            originalInvoiceTerm.getMoveLine() != null
                    && originalInvoiceTerm.getMoveLine().getMove() != null
                ? originalInvoiceTerm.getMoveLine().getMove()
                : null,
            originalInvoiceTerm.getMoveLine(),
            originalInvoiceTerm.getBankDetails(),
            originalInvoiceTerm.getPfpValidatorUser(),
            originalInvoiceTerm.getPaymentMode(),
            originalInvoiceTerm.getDueDate(),
            originalInvoiceTerm.getEstimatedPaymentDate(),
            amount,
            percentage,
            sequence,
            originalInvoiceTerm.getIsHoldBack());

    if (originalInvoiceTerm.getApplyFinancialDiscount()) {
      invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);
    }

    if (invoice != null) {
      invoice.addInvoiceTermListItem(invoiceTerm);
    }

    invoiceTerm.setOriginInvoiceTerm(originalInvoiceTerm);
    invoiceTerm.setIsCustomized(true);
    invoiceTermRepo.save(invoiceTerm);
    return invoiceTerm;
  }

  @Transactional
  protected void updateOriginalTerm(
      InvoiceTerm originalInvoiceTerm,
      InvoiceTerm newInvoiceTerm,
      BigDecimal grantedAmount,
      PfpPartialReason partialReason,
      BigDecimal amount,
      Invoice invoice) {
    originalInvoiceTerm.setIsCustomized(true);
    originalInvoiceTerm.setIsPaid(false);
    originalInvoiceTerm.setInitialPfpAmount(originalInvoiceTerm.getAmount());
    originalInvoiceTerm.setPercentage(
        invoiceTermToolService.computeCustomizedPercentage(
            grantedAmount,
            invoice != null
                ? invoice.getInTaxTotal()
                : originalInvoiceTerm
                    .getMoveLine()
                    .getCredit()
                    .max(originalInvoiceTerm.getMoveLine().getDebit())));
    originalInvoiceTerm.setAmount(grantedAmount);
    originalInvoiceTerm.setAmountRemaining(grantedAmount);
    originalInvoiceTerm.setPfpValidateStatusSelect(
        InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    originalInvoiceTerm.setRemainingPfpAmount(amount);
    originalInvoiceTerm.setDecisionPfpTakenDateTime(LocalDateTime.now());
    originalInvoiceTerm.setPfpPartialReason(partialReason);
    originalInvoiceTerm.setCompanyAmount(
        originalInvoiceTerm.getCompanyAmount().subtract(newInvoiceTerm.getCompanyAmount()));
    originalInvoiceTerm.setCompanyAmountRemaining(
        originalInvoiceTerm
            .getCompanyAmountRemaining()
            .subtract(newInvoiceTerm.getCompanyAmountRemaining()));
  }

  @Override
  public Integer checkOtherInvoiceTerms(List<InvoiceTerm> invoiceTermList) {
    if (CollectionUtils.isEmpty(invoiceTermList)) {
      return null;
    }
    InvoiceTerm firstInvoiceTerm = invoiceTermList.get(0);
    int pfpStatus = invoiceTermPfpToolService.getPfpValidateStatusSelect(firstInvoiceTerm);
    int otherPfpStatus;
    for (InvoiceTerm otherInvoiceTerm : invoiceTermList) {
      if (otherInvoiceTerm.getId() != null
          && firstInvoiceTerm.getId() != null
          && !otherInvoiceTerm.getId().equals(firstInvoiceTerm.getId())) {
        otherPfpStatus = invoiceTermPfpToolService.getPfpValidateStatusSelect(otherInvoiceTerm);

        if (otherPfpStatus != pfpStatus) {
          pfpStatus = InvoiceTermRepository.PFP_STATUS_AWAITING;
          break;
        }
      }
    }

    return pfpStatus;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refreshInvoicePfpStatus(Invoice invoice) {
    if (invoice == null || ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
      return;
    }

    Integer pfpStatus = checkOtherInvoiceTerms(invoice.getInvoiceTermList());

    if (pfpStatus != null && pfpStatus != invoice.getPfpValidateStatusSelect()) {
      invoice.setPfpValidateStatusSelect(pfpStatus);
      invoiceRepo.save(invoice);
    }
  }

  @Override
  public boolean getUserCondition(User pfpValidatorUser, User user) {
    return user.equals(pfpValidatorUser) || user.getIsSuperPfpUser();
  }

  @Override
  public boolean getInvoiceTermsCondition(List<InvoiceTerm> invoiceTermList) {
    return CollectionUtils.isNotEmpty(invoiceTermList)
        && invoiceTermList.stream()
            .allMatch(
                it -> it.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_AWAITING);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean generateInvoiceTermsAfterPfpPartial(List<InvoiceTerm> invoiceTermList)
      throws AxelorException {
    List<InvoiceTerm> itList =
        invoiceTermList.stream()
            .filter(InvoiceTerm::getPfpfPartialValidationOk)
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(itList)) {
      return false;
    }

    for (InvoiceTerm it : itList) {
      it = invoiceTermRepo.find(it.getId());
      BigDecimal amount = it.getAmount();
      it.setAmount(it.getPfpPartialValidationAmount());
      generateInvoiceTerm(it, it.getAmount(), amount, it.getPfpPartialReason());
      it.setPfpfPartialValidationOk(false);
      it.setPfpPartialValidationAmount(BigDecimal.ZERO);
      invoiceTermRepo.save(it);
    }
    return true;
  }

  @Override
  public void validatePfpValidatedAmount(
      MoveLine debitMoveLine, MoveLine creditMoveLine, BigDecimal amount, Company reconcileCompany)
      throws AxelorException {
    if (debitMoveLine == null
        || creditMoveLine == null
        || isSupplierRefundRelated(debitMoveLine, creditMoveLine)
        || !pfpService.isManagePassedForPayment(reconcileCompany)) {
      return;
    }

    this.validateInvoiceTermAmount(debitMoveLine, amount);
    this.validateInvoiceTermAmount(creditMoveLine, amount);
  }

  protected void validateInvoiceTermAmount(MoveLine moveLine, BigDecimal amount)
      throws AxelorException {
    if (!ObjectUtils.isEmpty(moveLine.getInvoiceTermList())
        && moveLine.getMove() != null
        && MoveRepository.PFP_STATUS_AWAITING == moveLine.getMove().getPfpValidateStatusSelect()) {
      BigDecimal debitAmount =
          moveLine.getInvoiceTermList().stream()
              .filter(
                  it ->
                      Arrays.asList(
                              InvoiceTermRepository.PFP_STATUS_NO_PFP,
                              InvoiceTermRepository.PFP_STATUS_AWAITING,
                              InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED,
                              InvoiceTermRepository.PFP_STATUS_VALIDATED)
                          .contains(it.getPfpValidateStatusSelect()))
              .map(InvoiceTerm::getCompanyAmountRemaining)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (amount.compareTo(debitAmount) > 0) {
        throw new AxelorException(
            moveLine.getMove(),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.RECONCILE_PFP_AMOUNT_MISSING),
            moveLine.getMove().getReference(),
            moveLine.getAccount().getCode());
      }
    }
  }

  protected boolean isSupplierRefundRelated(MoveLine debitMoveLine, MoveLine creditMoveLine) {
    Invoice debitInvoice =
        Optional.of(debitMoveLine).map(MoveLine::getMove).map(Move::getInvoice).orElse(null);
    Invoice creditInvoice =
        Optional.of(creditMoveLine).map(MoveLine::getMove).map(Move::getInvoice).orElse(null);
    if (debitInvoice == null || creditInvoice == null) {
      return false;
    }

    if (InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND == creditInvoice.getOperationTypeSelect()
        && InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            == debitInvoice.getOperationTypeSelect()) {
      return Objects.equals(creditInvoice.getOriginalInvoice(), debitInvoice);
    }
    if (InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND == debitInvoice.getOperationTypeSelect()
        && InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            == creditInvoice.getOperationTypeSelect()) {
      return Objects.equals(debitInvoice.getOriginalInvoice(), creditInvoice);
    }

    return false;
  }
}
