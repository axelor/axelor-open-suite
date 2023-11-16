/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermPfpServiceImpl implements InvoiceTermPfpService {
  protected InvoiceTermService invoiceTermService;
  protected AccountConfigService accountConfigService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected MoveRepository moveRepo;

  @Inject
  public InvoiceTermPfpServiceImpl(
      InvoiceTermService invoiceTermService,
      AccountConfigService accountConfigService,
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      MoveRepository moveRepo) {
    this.invoiceTermService = invoiceTermService;
    this.accountConfigService = accountConfigService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.moveRepo = moveRepo;
  }

  @Override
  public void validatePfp(InvoiceTerm invoiceTerm, User currentUser) {
    Company company = invoiceTerm.getCompany();

    invoiceTerm.setDecisionPfpTakenDateTime(
        Beans.get(AppBaseService.class).getTodayDateTime(company).toLocalDateTime());
    invoiceTerm.setInitialPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
    if (invoiceTerm.getPfpValidatorUser() == null) {
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
  public Integer massValidatePfp(List<Long> invoiceTermIds) {
    List<InvoiceTerm> invoiceTermList = this.getInvoiceTerms(invoiceTermIds);
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        validatePfp(invoiceTerm, currentUser);
        updatedRecords++;
      }
    }

    return updatedRecords;
  }

  protected List<InvoiceTerm> getInvoiceTerms(List<Long> invoiceTermIds) {
    return invoiceTermRepo
        .all()
        .filter("self.id IN :invoiceTermIds AND self.pfpValidateStatusSelect = :pfpStatusAwaiting")
        .bind("invoiceTermIds", invoiceTermIds)
        .bind("pfpStatusAwaiting", InvoiceRepository.PFP_STATUS_AWAITING)
        .fetch();
  }

  protected boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser =
        currentUser.getIsSuperPfpUser()
            || (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
                && currentUser.equals(invoiceTerm.getPfpValidatorUser()));
    if (isValidUser) {
      return true;
    }
    return validateUser(invoiceTerm, currentUser)
        && (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
            && invoiceTerm
                .getPfpValidatorUser()
                .equals(
                    invoiceTermService.getPfpValidatorUser(
                        invoiceTerm.getPartner(), invoiceTerm.getCompany())))
        && !invoiceTerm.getIsPaid();
  }

  protected boolean validateUser(InvoiceTerm invoiceTerm, User currentUser) {
    if (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())) {
      List<SubstitutePfpValidator> substitutePfpValidatorList =
          invoiceTerm.getPfpValidatorUser().getSubstitutePfpValidatorList();
      LocalDate todayDate =
          Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany());

      for (SubstitutePfpValidator substitutePfpValidator : substitutePfpValidatorList) {
        if (substitutePfpValidator.getSubstitutePfpValidatorUser().equals(currentUser)) {
          LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
          LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();
          if (substituteStartDate == null) {
            if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
              return true;
            }
          } else {
            if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
              return true;
            } else if (substituteStartDate.isBefore(todayDate)
                && substituteEndDate.isAfter(todayDate)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public Integer massRefusePfp(
      List<Long> invoiceTermIds,
      CancelReason reasonOfRefusalToPay,
      String reasonOfRefusalToPayStr) {
    List<InvoiceTerm> invoiceTermList = this.getInvoiceTerms(invoiceTermIds);
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      boolean invoiceTermCheck =
          ObjectUtils.notEmpty(invoiceTerm.getCompany())
              && ObjectUtils.notEmpty(reasonOfRefusalToPay);

      if (invoiceTermCheck && canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
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
        Beans.get(AppBaseService.class)
            .getTodayDateTime(invoiceTerm.getCompany())
            .toLocalDateTime());
    invoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    invoiceTerm.setRemainingPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    invoiceTerm.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoiceTerm.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());
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

  @Transactional(rollbackOn = {Exception.class})
  protected InvoiceTerm createPfpInvoiceTerm(
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

    BigDecimal percentage = invoiceTermService.computeCustomizedPercentage(amount, total);

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
      invoiceTermService.computeFinancialDiscount(invoiceTerm, invoice);
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
        invoiceTermService.computeCustomizedPercentage(
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
    int pfpStatus = this.getPfpValidateStatusSelect(firstInvoiceTerm);
    int otherPfpStatus;
    for (InvoiceTerm otherInvoiceTerm : invoiceTermList) {
      if (otherInvoiceTerm.getId() != null
          && firstInvoiceTerm.getId() != null
          && !otherInvoiceTerm.getId().equals(firstInvoiceTerm.getId())) {
        otherPfpStatus = this.getPfpValidateStatusSelect(otherInvoiceTerm);

        if (otherPfpStatus != pfpStatus) {
          pfpStatus = InvoiceTermRepository.PFP_STATUS_AWAITING;
          break;
        }
      }
    }

    return pfpStatus;
  }

  @Override
  public int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getPfpValidateStatusSelect()
        == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED) {
      return InvoiceTermRepository.PFP_STATUS_VALIDATED;
    } else {
      return invoiceTerm.getPfpValidateStatusSelect();
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
        invoiceTermService.computeCustomizedPercentage(
            grantedAmount,
            originalInvoiceTerm.getInvoice() != null
                ? originalInvoiceTerm.getInvoice().getInTaxTotal()
                : originalInvoiceTerm
                    .getMoveLine()
                    .getCredit()
                    .max(originalInvoiceTerm.getMoveLine().getDebit())));

    if (originalInvoiceTerm.getApplyFinancialDiscount()) {
      invoiceTermService.computeFinancialDiscount(
          originalInvoiceTerm, originalInvoiceTerm.getInvoice());
    }

    invoiceTermRepo.save(originalInvoiceTerm);
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
}
