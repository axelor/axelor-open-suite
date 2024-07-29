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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

public class TaxPaymentMoveLineServiceImpl implements TaxPaymentMoveLineService {

  @Override
  public TaxPaymentMoveLine computeTaxAmount(TaxPaymentMoveLine taxPaymentMoveLine)
      throws AxelorException {
    BigDecimal taxRate = taxPaymentMoveLine.getTaxRate().divide(new BigDecimal(100));
    BigDecimal base = taxPaymentMoveLine.getDetailPaymentAmount();
    taxPaymentMoveLine.setTaxAmount(
        base.multiply(taxRate)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
    return taxPaymentMoveLine;
  }

  @Override
  public TaxPaymentMoveLine getReverseTaxPaymentMoveLine(TaxPaymentMoveLine taxPaymentMoveLine)
      throws AxelorException {
    TaxPaymentMoveLine reversetaxPaymentMoveLine =
        new TaxPaymentMoveLine(
            taxPaymentMoveLine.getMoveLine(),
            taxPaymentMoveLine.getOriginTaxLine(),
            taxPaymentMoveLine.getReconcile(),
            taxPaymentMoveLine.getTaxRate(),
            taxPaymentMoveLine.getDetailPaymentAmount().negate(),
            taxPaymentMoveLine.getDate());
    reversetaxPaymentMoveLine.setTaxAmount(taxPaymentMoveLine.getTaxAmount().negate());
    reversetaxPaymentMoveLine.setIsAlreadyReverse(true);
    reversetaxPaymentMoveLine.setVatSystemSelect(taxPaymentMoveLine.getVatSystemSelect());
    taxPaymentMoveLine.setIsAlreadyReverse(true);
    return reversetaxPaymentMoveLine;
  }

  @Override
  public TaxPaymentMoveLine createTaxPaymentMoveLineWithFixedAmount(
      Invoice invoice,
      BigDecimal paymentRatio,
      int vatSystemSelect,
      MoveLine invoiceMoveLine,
      TaxLine taxLine,
      MoveLine customerPaymentMoveLine,
      Reconcile reconcile) {
    if (invoice == null
        || paymentRatio == null
        || reconcile == null
        || ObjectUtils.isEmpty(invoice.getInvoiceLineTaxList())
        || !Optional.ofNullable(taxLine)
            .map(TaxLine::getTax)
            .map(Tax::getManageByAmount)
            .orElse(false)) {
      return null;
    }

    InvoiceLineTax invoiceLineTax =
        invoice.getInvoiceLineTaxList().stream()
            .filter(
                tax ->
                    tax.getVatSystemSelect() == vatSystemSelect
                        && Objects.equals(invoiceMoveLine.getAccount(), tax.getImputedAccount())
                        && Objects.equals(taxLine, tax.getTaxLine()))
            .findFirst()
            .orElse(null);

    if (invoiceLineTax == null
        || invoiceLineTax.getTaxTotal().compareTo(invoiceLineTax.getPercentageTaxTotal()) == 0) {
      return null;
    }

    TaxPaymentMoveLine taxPaymentMoveLine =
        new TaxPaymentMoveLine(
            customerPaymentMoveLine,
            taxLine,
            reconcile,
            taxLine.getValue(),
            invoiceLineTax
                .getCompanyExTaxBase()
                .multiply(paymentRatio)
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP),
            reconcile.getEffectiveDate());

    taxPaymentMoveLine.setTaxAmount(
        invoiceLineTax
            .getCompanyTaxTotal()
            .multiply(paymentRatio)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));

    return taxPaymentMoveLine;
  }
}
