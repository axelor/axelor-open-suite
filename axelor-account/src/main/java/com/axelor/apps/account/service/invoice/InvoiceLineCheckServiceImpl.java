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

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceLineCheckServiceImpl implements InvoiceLineCheckService {

  protected TaxAccountService taxAccountService;

  @Inject
  public InvoiceLineCheckServiceImpl(TaxAccountService taxAccountService) {
    this.taxAccountService = taxAccountService;
  }

  public void checkTaxLinesNotOnlyNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    // split in for loop, catch the exception, and throw another exception with the specific account
    taxAccountService.checkTaxLinesNotOnlyNonDeductibleTaxes(
        invoiceLineList.stream()
            .filter(invoiceLine -> invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return;
    }

    taxAccountService.checkSumOfNonDeductibleTaxesOnTaxLines(
        invoiceLineList.stream()
            .filter(invoiceLine -> invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_NORMAL)
            .map(InvoiceLine::getTaxLineSet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet()));
  }

  public void checkInvoiceLineTaxes(Set<TaxLine> taxLineSet) throws AxelorException {
    if (ObjectUtils.notEmpty(taxLineSet) && taxAccountService.isNonDeductibleTaxesSet(taxLineSet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage.INVOICE_LINE_PRODUCT_WITH_NON_DEDUCTIBLE_TAX_NOT_AUTHORIZED));
    }
  }
}
