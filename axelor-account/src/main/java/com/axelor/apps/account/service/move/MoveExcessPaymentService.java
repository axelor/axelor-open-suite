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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveExcessPaymentService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineRepository moveLineRepository;
  protected MoveToolService moveToolService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveExcessPaymentService(
      MoveLineRepository moveLineRepository,
      MoveToolService moveToolService,
      MoveLineToolService moveLineToolService) {

    this.moveLineRepository = moveLineRepository;
    this.moveToolService = moveToolService;
    this.moveLineToolService = moveLineToolService;
  }

  /**
   * Méthode permettant de récupérer les trop-perçus et une facture
   *
   * @param invoice Une facture
   * @return
   * @throws AxelorException
   */
  public List<MoveLine> getExcessPayment(Invoice invoice) throws AxelorException {
    Company company = invoice.getCompany();
    AccountConfig accountConfig = Beans.get(AccountConfigService.class).getAccountConfig(company);

    // get advance payments
    List<MoveLine> advancePaymentMoveLines =
        Beans.get(InvoiceService.class).getMoveLinesFromAdvancePayments(invoice);

    MoveLine moveLine = getOrignalInvoiceMoveLine(invoice);

    if (moveLine != null) {
      advancePaymentMoveLines.add(moveLine);
    }

    if (accountConfig.getAutoReconcileOnInvoice()) {
      List<MoveLine> creditMoveLines =
          moveLineToolService.getMoveExcessDueList(
              true, company, invoice.getPartner(), invoice.getId());

      log.debug("Number of overpayment to attribute to the invoice : {}", creditMoveLines.size());
      advancePaymentMoveLines.addAll(creditMoveLines);
    }
    // remove duplicates
    advancePaymentMoveLines =
        advancePaymentMoveLines.stream().distinct().collect(Collectors.toList());
    return advancePaymentMoveLines;
  }

  protected MoveLine getOrignalInvoiceMoveLine(Invoice invoice) {

    Invoice originalInvoice = invoice.getOriginalInvoice();

    if (originalInvoice != null && originalInvoice.getMove() != null) {
      for (MoveLine moveLine : originalInvoice.getMove().getMoveLineList()) {
        if (moveLine.getAccount().getUseForPartnerBalance()
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
          return moveLine;
        }
      }
    }

    return null;
  }

  public List<MoveLine> getAdvancePaymentMoveList(Invoice invoice) {

    List<MoveLine> moveLineList = Lists.newArrayList();

    if (invoice.getInvoicePaymentList() != null) {

      for (InvoicePayment invoicePayment : invoice.getInvoicePaymentList()) {
        if (invoicePayment.getMove() != null
            && invoicePayment.getMove().getMoveLineList() != null) {
          for (MoveLine moveLine : invoicePayment.getMove().getMoveLineList()) {

            if (moveLine.getCredit().compareTo(BigDecimal.ZERO) != 0) {
              moveLineList.add(moveLine);
            }
          }
        }
      }

      return moveToolService.orderListByDate(moveLineList);
    }

    return moveLineList;
  }
}
