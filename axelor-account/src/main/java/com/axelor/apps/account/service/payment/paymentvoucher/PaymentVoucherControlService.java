/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PaymentVoucherControlService {

  protected PaymentVoucherSequenceService paymentVoucherSequenceService;
  protected AppBaseService appBaseService;
  protected AccountManagementRepository accountManagementRepo;

  @Inject
  public PaymentVoucherControlService(
      PaymentVoucherSequenceService paymentVoucherSequenceService,
      AppBaseService appBaseService,
      AccountManagementRepository accountManagementRepo) {
    this.paymentVoucherSequenceService = paymentVoucherSequenceService;
    this.appBaseService = appBaseService;
    this.accountManagementRepo = accountManagementRepo;
  }

  /**
   * Procédure permettant de vérifier le remplissage et le bon contenu des champs de la saisie
   * paiement et de la société
   *
   * @param paymentVoucher Une saisie paiement
   * @param company Une société
   * @param paymentModeAccount Le compte de trésoreie du mode de règlement
   * @throws AxelorException
   */
  public void checkPaymentVoucherField(
      PaymentVoucher paymentVoucher, Company company, Account paymentModeAccount, Journal journal)
      throws AxelorException {

    if (paymentVoucher.getPaidAmount().compareTo(BigDecimal.ZERO) < 1) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CONTROL_PAID_AMOUNT),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          paymentVoucher.getRef());
    }

    if (paymentVoucher.getRemainingAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CONTROL_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          paymentVoucher.getRef());
    }

    if (journal == null || paymentModeAccount == null) {
      throw new AxelorException(
          paymentVoucher,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_CONTROL_3),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    if (journal.getEditReceiptOk()) {
      paymentVoucherSequenceService.checkReceipt(paymentVoucher);
    }
  }

  /**
   * Fonction vérifiant si l'ensemble des lignes à payer ont le même compte et que ce compte est le
   * même que celui du trop-perçu
   *
   * @param payVoucherElementToPayList La liste des lignes à payer
   * @param moveLine Le trop-perçu à utiliser
   * @return
   */
  public boolean checkIfSameAccount(
      List<PayVoucherElementToPay> payVoucherElementToPayList, MoveLine moveLine) {
    if (moveLine != null) {
      Account account = moveLine.getAccount();
      for (PayVoucherElementToPay payVoucherElementToPay : payVoucherElementToPayList) {
        if (!payVoucherElementToPay.getMoveLine().getAccount().equals(account)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public boolean controlMoveAmounts(PaymentVoucher paymentVoucher) {
    if (!CollectionUtils.isEmpty(paymentVoucher.getPayVoucherElementToPayList())) {
      for (PayVoucherElementToPay elementToPay : paymentVoucher.getPayVoucherElementToPayList()) {
        BigDecimal remainingAmountToPay = elementToPay.getRemainingAmount();
        BigDecimal remainingAmountMoveLine;
        if (elementToPay.getFinancialDiscount() == null) {
          remainingAmountMoveLine = elementToPay.getMoveLine().getAmountRemaining();
        } else {
          remainingAmountMoveLine = elementToPay.getMoveLine().getRemainingAmountAfterFinDiscount();
        }
        if (!remainingAmountToPay.equals(remainingAmountMoveLine)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isReceiptDisplayed(PaymentVoucher paymentVoucher) {
    if (paymentVoucher.getStatusSelect() != PaymentVoucherRepository.STATUS_CONFIRMED
        && paymentVoucher.getStatusSelect() != PaymentVoucherRepository.STATUS_CANCELED) {
      return false;
    }

    boolean isMultiBanks =
        appBaseService.getAppBase().getManageMultiBanks()
            && paymentVoucher.getCompanyBankDetails() != null;
    String query = "self.company = :company AND self.paymentMode = :paymentMode";

    if (isMultiBanks) {
      query += " AND self.bankDetails = :bankDetails";
    }

    Query<AccountManagement> accountManagementQuery =
        accountManagementRepo
            .all()
            .filter(query)
            .bind("company", paymentVoucher.getCompany())
            .bind("paymentMode", paymentVoucher.getPaymentMode());

    if (isMultiBanks) {
      accountManagementQuery =
          accountManagementQuery.bind("bankDetails", paymentVoucher.getCompanyBankDetails());
    }

    AccountManagement accountManagement = accountManagementQuery.fetchOne();

    return accountManagement != null
        && accountManagement.getJournal() != null
        && accountManagement.getJournal().getEditReceiptOk();
  }
}
