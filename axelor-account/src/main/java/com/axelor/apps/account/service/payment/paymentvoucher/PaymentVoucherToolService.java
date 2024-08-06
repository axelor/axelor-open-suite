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

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import java.util.Objects;

public class PaymentVoucherToolService {

  /**
   * @param paymentVoucher : Une saisie Paiement
   *     <p>OperationTypeSelect 1 : Achat fournisseur 2 : Avoir fournisseur 3 : Vente client 4 :
   *     Avoir client
   * @return
   * @throws AxelorException
   */
  public boolean isDebitToPay(PaymentVoucher paymentVoucher) throws AxelorException {
    boolean isDebitToPay;

    switch (paymentVoucher.getOperationTypeSelect()) {
      case PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        isDebitToPay = false;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        isDebitToPay = true;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_CLIENT_SALE:
        isDebitToPay = true;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_CLIENT_REFUND:
        isDebitToPay = false;
        break;

      default:
        throw new AxelorException(
            paymentVoucher,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_TOOL_1),
            paymentVoucher.getRef());
    }

    return isDebitToPay;
  }

  /**
   * @param paymentVoucher : Une saisie Paiement
   *     <p>OperationTypeSelect 1 : Achat fournisseur 2 : Avoir fournisseur 3 : Vente client 4 :
   *     Avoir client
   * @return
   * @throws AxelorException
   */
  public boolean isPurchase(PaymentVoucher paymentVoucher) throws AxelorException {

    boolean isPurchase;

    switch (paymentVoucher.getOperationTypeSelect()) {
      case PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        isPurchase = true;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        isPurchase = true;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_CLIENT_SALE:
        isPurchase = false;
        break;
      case PaymentVoucherRepository.OPERATION_TYPE_CLIENT_REFUND:
        isPurchase = false;
        break;

      default:
        throw new AxelorException(
            paymentVoucher,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_TOOL_1),
            paymentVoucher.getRef());
    }

    return isPurchase;
  }

  @CallMethod
  public boolean isMultiCurrency(PaymentVoucher paymentVoucher) {
    return !Objects.equals(paymentVoucher.getCurrency(), paymentVoucher.getCompany().getCurrency());
  }
}
