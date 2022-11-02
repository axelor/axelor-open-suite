package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceTermPfpServiceImpl implements InvoiceTermPfpService {
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;

  @Inject
  public InvoiceTermPfpServiceImpl(
      InvoiceTermService invoiceTermService,
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo) {
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePfp(InvoiceTerm invoiceTerm, User currentUser) {
    Company company = invoiceTerm.getCompany();

    invoiceTerm.setDecisionPfpTakenDate(Beans.get(AppBaseService.class).getTodayDate(company));
    invoiceTerm.setInitialPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
    invoiceTerm.setPfpValidatorUser(currentUser);
    invoiceTermRepo.save(invoiceTerm);

    this.checkOtherInvoiceTerms(invoiceTerm);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
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
    invoiceTerm.setDecisionPfpTakenDate(
        Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getCompany()));
    invoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    invoiceTerm.setRemainingPfpAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    invoiceTerm.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoiceTerm.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    invoiceTermRepo.save(invoiceTerm);

    this.checkOtherInvoiceTerms(invoiceTerm);
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
    this.createPfpInvoiceTerm(originalInvoiceTerm, invoice, amount);
    this.updateOriginalTerm(originalInvoiceTerm, grantedAmount, partialReason, amount, invoice);

    invoiceTermService.initInvoiceTermsSequence(invoice);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createPfpInvoiceTerm(
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

    invoiceTerm.setOriginInvoiceTerm(originalInvoiceTerm);
    invoiceTerm.setIsCustomized(true);
    invoiceTermRepo.save(invoiceTerm);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updateOriginalTerm(
      InvoiceTerm originalInvoiceTerm,
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
    originalInvoiceTerm.setDecisionPfpTakenDate(LocalDate.now());
    originalInvoiceTerm.setPfpPartialReason(partialReason);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void checkOtherInvoiceTerms(InvoiceTerm invoiceTerm) {
    Invoice invoice = invoiceTerm.getInvoice();
    if (invoice == null) {
      return;
    }
    int pfpStatus = this.getPfpValidateStatusSelect(invoiceTerm);
    int otherPfpStatus;
    for (InvoiceTerm otherInvoiceTerm : invoice.getInvoiceTermList()) {
      if (!otherInvoiceTerm.getId().equals(invoiceTerm.getId())) {
        otherPfpStatus = this.getPfpValidateStatusSelect(otherInvoiceTerm);

        if (otherPfpStatus != pfpStatus) {
          pfpStatus = InvoiceTermRepository.PFP_STATUS_AWAITING;
          break;
        }
      }
    }

    invoice.setPfpValidateStatusSelect(pfpStatus);
    invoiceRepo.save(invoice);
  }

  protected int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getPfpValidateStatusSelect()
        == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED) {
      return InvoiceTermRepository.PFP_STATUS_VALIDATED;
    } else {
      return invoiceTerm.getPfpValidateStatusSelect();
    }
  }
}
