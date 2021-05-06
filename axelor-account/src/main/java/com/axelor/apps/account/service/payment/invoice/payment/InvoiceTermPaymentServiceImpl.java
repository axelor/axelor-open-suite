/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceTermPaymentServiceImpl implements InvoiceTermPaymentService {

  @Override
  public List<InvoiceTermPayment> initInvoiceTermPayments(
      InvoicePayment invoicePayment, List<InvoiceTerm> invoiceTermsToPay) {

    List<InvoiceTermPayment> invoiceTermPayments = Lists.newArrayList();
    for (InvoiceTerm invoiceTerm : invoiceTermsToPay) {
      invoiceTermPayments.add(
          createInvoiceTermPayment(invoicePayment, invoiceTerm, invoiceTerm.getAmountRemaining()));
    }

    return invoiceTermPayments;
  }

  @Override
  public List<InvoiceTermPayment> initInvoiceTermPaymentsWithAmount(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal availableAmount) {

    List<InvoiceTermPayment> invoiceTermPayments = Lists.newArrayList();

    for (InvoiceTerm invoiceTerm : invoiceTermsToPay) {
      if (availableAmount.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal invoiceTermAmount = invoiceTerm.getAmountRemaining();
        if (invoiceTermAmount.compareTo(availableAmount) >= 0) {
          invoiceTermPayments.add(
              createInvoiceTermPayment(invoicePayment, invoiceTerm, availableAmount));
          availableAmount = BigDecimal.ZERO;
        } else {
          invoiceTermPayments.add(
              createInvoiceTermPayment(invoicePayment, invoiceTerm, invoiceTermAmount));
          availableAmount = availableAmount.subtract(invoiceTermAmount);
        }
      }
    }
    return invoiceTermPayments;
  }

  @Override
  public InvoiceTermPayment createInvoiceTermPayment(
      InvoicePayment invoicePayment, InvoiceTerm invoiceTermToPay, BigDecimal paidAmount) {

    InvoiceTermPayment invoiceTermPayment = new InvoiceTermPayment();
    invoiceTermPayment.setInvoicePayment(invoicePayment);
    invoiceTermPayment.setInvoiceTerm(invoiceTermToPay);
    invoiceTermPayment.setPaidAmount(paidAmount);
    return invoiceTermPayment;
  }

  @Override
  public InvoicePayment updateInvoicePaymentAmount(InvoicePayment invoicePayment) {

    invoicePayment.setAmount(
        computeInvoicePaymentAmount(invoicePayment, invoicePayment.getInvoiceTermPaymentList()));

    return invoicePayment;
  }

  @Override
  public BigDecimal computeInvoicePaymentAmount(
      InvoicePayment invoicePayment, List<InvoiceTermPayment> invoiceTermPayments) {

    BigDecimal sum = BigDecimal.ZERO;
    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPayments) {
      sum = sum.add(invoiceTermPayment.getPaidAmount());
    }
    return sum;
  }
}
