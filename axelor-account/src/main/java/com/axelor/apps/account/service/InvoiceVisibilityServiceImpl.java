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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class InvoiceVisibilityServiceImpl implements InvoiceVisibilityService {
  protected InvoiceService invoiceService;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected PfpService pfpService;

  @Inject
  public InvoiceVisibilityServiceImpl(
      InvoiceService invoiceService,
      InvoiceTermPfpService invoiceTermPfpService,
      PfpService pfpService) {
    this.invoiceService = invoiceService;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.pfpService = pfpService;
  }

  @Override
  public boolean isPfpButtonVisible(Invoice invoice, User user, boolean litigation)
      throws AxelorException {

    boolean validatorUserCondition =
        invoiceTermPfpService.getUserCondition(invoice.getPfpValidatorUser(), user);

    boolean statusCondition = this._getStatusCondition(invoice);

    boolean pfpValidateStatusCondition = this._getPfpValidateStatusCondition(invoice, litigation);

    boolean invoiceTermsCondition =
        invoiceTermPfpService.getInvoiceTermsCondition(invoice.getInvoiceTermList());

    return this.getPfpCondition(invoice)
        && validatorUserCondition
        && statusCondition
        && pfpValidateStatusCondition
        && invoiceTermsCondition;
  }

  @Override
  public boolean isPaymentButtonVisible(Invoice invoice) throws AxelorException {
    boolean statusOperationSubTypeCondition = this._getStatusOperationSubTypeCondition(invoice);

    boolean operationTypeCondition = this._getOperationTypeCondition(invoice);

    boolean otherCondition =
        invoice.getAmountRemaining().signum() > 0 && !invoice.getHasPendingPayments();

    return statusOperationSubTypeCondition && operationTypeCondition && otherCondition;
  }

  @Override
  public boolean isValidatorUserVisible(Invoice invoice) throws AxelorException {
    boolean invoiceTermsCondition =
        invoiceTermPfpService.getInvoiceTermsCondition(invoice.getInvoiceTermList());

    boolean decisionDateCondition = this._getDecisionDateCondition(invoice);

    boolean statusCondition = this._getStatusNotDraftCondition(invoice);

    return this.getPfpCondition(invoice)
        && (invoiceTermsCondition || decisionDateCondition)
        && statusCondition;
  }

  @Override
  public boolean isDecisionPfpVisible(Invoice invoice) throws AxelorException {
    boolean decisionDateCondition = this._getDecisionDateCondition(invoice);

    return this.getPfpCondition(invoice) && decisionDateCondition;
  }

  @Override
  public boolean isSendNotifyVisible(Invoice invoice) throws AxelorException {

    boolean pfpValidateStatusCondition = this._getPfpValidateStatusCondition(invoice, false);

    boolean statusCondition = this._getStatusNotDraftCondition(invoice);

    boolean validatorUserCondition =
        invoiceService.isSelectedPfpValidatorEqualsPartnerPfpValidator(invoice);

    boolean otherCondition = invoice.getPfpValidatorUser() != null;

    return this.getPfpCondition(invoice)
        && pfpValidateStatusCondition
        && statusCondition
        && validatorUserCondition
        && otherCondition;
  }

  @Override
  public boolean getOperationTypePurchaseCondition(Invoice invoice) throws AxelorException {
    return invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
            && pfpService.isManagePFPInRefund(invoice.getCompany()));
  }

  @Override
  public boolean getPaymentVouchersStatus(Invoice invoice) throws AxelorException {
    AppAccountService appAccount = Beans.get(AppAccountService.class);

    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      return false;
    }

    return (InvoiceToolService.isPurchase(invoice))
        ? appAccount.getAppAccount().getPaymentVouchersOnSupplierInvoice()
        : appAccount.getAppAccount().getPaymentVouchersOnCustomerInvoice();
  }

  protected boolean _getOperationTypeCondition(Invoice invoice) throws AxelorException {
    return invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND
        || !pfpService.isManagePassedForPayment(invoice.getCompany())
        || (this._getInvoiceTermsCondition2(invoice)
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || !pfpService.isManagePFPInRefund(invoice.getCompany())
                || invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND));
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

  protected boolean _getInvoiceTermsCondition2(Invoice invoice) {
    return CollectionUtils.isEmpty(invoice.getInvoiceTermList())
        || invoice.getInvoiceTermList().stream()
            .anyMatch(
                it ->
                    it.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_NO_PFP
                        || it.getPfpValidateStatusSelect()
                            == InvoiceTermRepository.PFP_STATUS_VALIDATED
                        || it.getPfpValidateStatusSelect()
                            == InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
  }

  protected boolean _getDecisionDateCondition(Invoice invoice) {
    return invoice.getDecisionPfpTakenDateTime() != null;
  }

  @Override
  public boolean getPfpCondition(Invoice invoice) throws AxelorException {
    return pfpService.isManagePassedForPayment(invoice.getCompany())
        && this.getOperationTypePurchaseCondition(invoice);
  }
}
