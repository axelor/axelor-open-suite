/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherToolService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class ImportPaymentVoucher {

  @Inject PaymentVoucherLoadService paymentVoucherLoadService;

  @Inject PaymentVoucherToolService paymentVoucherToolService;

  @Inject PaymentVoucherConfirmService paymentVoucherConfirmService;

  @SuppressWarnings("rawtypes")
  public Object importPaymentVoucher(Object bean, Map values) {
    assert bean instanceof PaymentVoucher;
    try {
      PaymentVoucher paymentVoucher = (PaymentVoucher) bean;

      Invoice invoiceToPay = getInvoice((String) values.get("orderImport"));

      MoveLine moveLineToPay = this.getMoveLineToPay(paymentVoucher, invoiceToPay);

      if (moveLineToPay != null) {
        PayVoucherDueElement payVoucherDueElement =
            paymentVoucherLoadService.createPayVoucherDueElement(moveLineToPay);
        paymentVoucher.addPayVoucherElementToPayListItem(
            paymentVoucherLoadService.createPayVoucherElementToPay(payVoucherDueElement, 1));
      }

      if (paymentVoucher.getStatusSelect() == PaymentVoucherRepository.STATUS_CONFIRMED) {

        paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher);
      }
      return paymentVoucher;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bean;
  }

  public Invoice getInvoice(String orderType_orderImportId) {
    if (!Strings.isNullOrEmpty(orderType_orderImportId)) {
      String orderType = orderType_orderImportId.split("_")[0];
      String orderImportId = orderType_orderImportId.split("_")[1];

      String filter;
      if (orderType.equals("S")) {
        filter = "self.saleOrder.importId = ?";
      } else {
        filter = "self.purchaseOrder.importId = ?";
      }

      return Beans.get(InvoiceRepository.class).all().filter(filter, orderImportId).fetchOne();
    }
    return null;
  }

  /**
   * Fonction permettant de récupérer la ligne d'écriture à payer
   *
   * @param paymentVoucher Une saisie paiement
   * @return Une écriture à payer
   * @throws AxelorException
   */
  public MoveLine getMoveLineToPay(PaymentVoucher paymentVoucher, Invoice invoice)
      throws AxelorException {
    if (invoice != null) {
      if (paymentVoucherToolService.isDebitToPay(paymentVoucher)) {
        return this.getInvoiceDebitMoveline(invoice);
      } else {
        return this.getInvoiceCreditMoveline(invoice);
      }
    }
    return null;
  }

  /**
   * According to the passed invoice, get the debit line to pay
   *
   * @param invoice
   * @return moveLine a moveLine
   */
  public MoveLine getInvoiceDebitMoveline(Invoice invoice) {
    if (invoice.getMove() != null && invoice.getMove().getMoveLineList() != null) {
      for (MoveLine moveLine : invoice.getMove().getMoveLineList()) {
        if ((moveLine.getAccount().equals(invoice.getPartnerAccount()))
            && moveLine.getAccount().getUseForPartnerBalance()
            && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
          return moveLine;
        }
      }
    }
    return null;
  }

  /**
   * According to the passed invoice, get the debit line to pay
   *
   * @param invoice
   * @return moveLine a moveLine
   */
  public MoveLine getInvoiceCreditMoveline(Invoice invoice) {
    if (invoice.getMove() != null && invoice.getMove().getMoveLineList() != null) {
      for (MoveLine moveLine : invoice.getMove().getMoveLineList()) {
        if ((moveLine.getAccount().equals(invoice.getPartnerAccount()))
            && moveLine.getAccount().getUseForPartnerBalance()
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
          return moveLine;
        }
      }
    }
    return null;
  }
}
