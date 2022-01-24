package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceVisibilityServiceImpl implements InvoiceVisibilityService {
  protected InvoiceService invoiceService;

  @Inject
  public InvoiceVisibilityServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Override
  public boolean isPfpButtonVisible(Invoice invoice, User user, boolean litigation) {
    boolean managePfpCondition = this._getManagePfpCondition(invoice);

    boolean validatorUserCondition = this._getUserCondition(invoice, user);

    boolean operationTypeCondition = this._getOperationTypePurchaseCondition(invoice);

    boolean statusCondition = this._getStatusCondition(invoice);

    boolean pfpValidateStatusCondition = this._getPfpValidateStatusCondition(invoice, litigation);

    boolean invoiceTermsCondition = this._getInvoiceTermsCondition(invoice);

    return managePfpCondition
        && validatorUserCondition
        && operationTypeCondition
        && statusCondition
        && pfpValidateStatusCondition
        && invoiceTermsCondition;
  }

  @Override
  public boolean isPaymentButtonVisible(Invoice invoice) {
    boolean statusOperationSubTypeCondition = this._getStatusOperationSubTypeCondition(invoice);

    boolean operationTypeCondition = this._getOperationTypeCondition(invoice);

    boolean otherCondition =
        invoice.getAmountRemaining().signum() > 0 && !invoice.getHasPendingPayments();

    return statusOperationSubTypeCondition && operationTypeCondition && otherCondition;
  }

  @Override
  public boolean isValidatorUserVisible(Invoice invoice) {
    boolean managePfpCondition = this._getManagePfpCondition(invoice);

    boolean operationTypeCondition = this._getOperationTypePurchaseCondition(invoice);

    boolean invoiceTermsCondition = this._getInvoiceTermsCondition(invoice);

    boolean decisionDateCondition = this._getDecisionDateCondition(invoice);

    boolean statusCondition = this._getStatusNotDraftCondition(invoice);

    return managePfpCondition
        && operationTypeCondition
        && (invoiceTermsCondition || decisionDateCondition)
        && statusCondition;
  }

  @Override
  public boolean isDecisionPfpVisible(Invoice invoice) {
    boolean managePfpCondition = this._getManagePfpCondition(invoice);

    boolean operationTypeCondition = this._getOperationTypePurchaseCondition(invoice);

    boolean decisionDateCondition = this._getDecisionDateCondition(invoice);

    return managePfpCondition && operationTypeCondition && decisionDateCondition;
  }

  @Override
  public boolean isSendNotifyVisible(Invoice invoice) {
    boolean managePfpCondition = this._getManagePfpCondition(invoice);

    boolean operationTypeCondition = this._getOperationTypePurchaseCondition(invoice);

    boolean pfpValidateStatusCondition = this._getPfpValidateStatusCondition(invoice, false);

    boolean statusCondition = this._getStatusNotDraftCondition(invoice);

    boolean validatorUserCondition =
        invoiceService.isSelectedPfpValidatorEqualsPartnerPfpValidator(invoice);

    boolean otherCondition = invoice.getPfpValidatorUser() != null;

    return managePfpCondition
        && operationTypeCondition
        && pfpValidateStatusCondition
        && statusCondition
        && validatorUserCondition
        && otherCondition;
  }

  protected boolean _getManagePfpCondition(Invoice invoice) {
    return invoice.getCompany().getAccountConfig().getIsManagePassedForPayment();
  }

  protected boolean _getUserCondition(Invoice invoice, User user) {
    return user.equals(invoice.getPfpValidatorUser()) || user.getIsSuperPfpUser();
  }

  protected boolean _getOperationTypeCondition(Invoice invoice) {
    return invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
        || !this._getManagePfpCondition(invoice)
        || (this._getInvoiceTermsCondition2(invoice)
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || !invoice.getCompany().getAccountConfig().getIsManagePFPInRefund()
                || invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND));
  }

  protected boolean _getOperationTypePurchaseCondition(Invoice invoice) {
    return invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            && invoice.getCompany().getAccountConfig().getIsManagePFPInRefund());
  }

  protected boolean _getStatusCondition(Invoice invoice) {
    return invoice.getStatusSelect() == InvoiceRepository.STATUS_VALIDATED
        || invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED;
  }

  protected boolean _getStatusNotDraftCondition(Invoice invoice) {
    return invoice.getStatusSelect() > InvoiceRepository.STATUS_DRAFT;
  }

  protected boolean _getStatusOperationSubTypeCondition(Invoice invoice) {
    return invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED
        || (invoice.getStatusSelect() == InvoiceRepository.STATUS_VALIDATED
            && invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE);
  }

  protected boolean _getPfpValidateStatusCondition(Invoice invoice, boolean litigation) {
    return invoice.getPfpValidateStatusSelect() == InvoiceRepository.PFP_STATUS_AWAITING
        || (!litigation
            || invoice.getPfpValidateStatusSelect() == InvoiceRepository.PFP_STATUS_LITIGATION);
  }

  protected boolean _getInvoiceTermsCondition(Invoice invoice) {
    return CollectionUtils.isEmpty(invoice.getInvoiceTermList())
        || invoice.getInvoiceTermList().stream()
            .allMatch(
                it -> it.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_AWAITING);
  }

  protected boolean _getInvoiceTermsCondition2(Invoice invoice) {
    return CollectionUtils.isEmpty(invoice.getInvoiceTermList())
        || invoice.getInvoiceTermList().stream()
            .anyMatch(
                it ->
                    it.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_VALIDATED
                        || it.getPfpValidateStatusSelect()
                            == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
  }

  protected boolean _getDecisionDateCondition(Invoice invoice) {
    return invoice.getDecisionPfpTakenDate() != null;
  }
}
