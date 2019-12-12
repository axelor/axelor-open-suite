/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

  /**
   * Use excess payment between a list of debit move lines and a list of credit move lines. The
   * lists needs to be ordered by date in order to pay the invoices chronologically.
   *
   * @param debitMoveLines = dûs
   * @param creditMoveLines = trop-perçu
   * @throws AxelorException
   */
  void useExcessPaymentOnMoveLines(List<MoveLine> debitMoveLines, List<MoveLine> creditMoveLines)
      throws AxelorException;

  /**
   * Use excess payment between a list of debit move lines and a list of credit move lines. The
   * lists needs to be ordered by date in order to pay the invoices chronologically. This method
   * doesn't throw any exception if a reconciliation fails.
   *
   * @param debitMoveLines
   * @param creditMoveLines
   */
  void useExcessPaymentOnMoveLinesDontThrow(
      List<MoveLine> debitMoveLines, List<MoveLine> creditMoveLines);

  /**
   * Il crée des écritures de trop percu avec des montants exacts pour chaque débitMoveLines avec le
   * compte du débitMoveLines. A la fin, si il reste un trop-percu alors créer un trop-perçu
   * classique.
   *
   * @param debitMoveLines Les lignes d'écriture à payer
   * @param remainingPaidAmount Le montant restant à payer
   * @param move Une écriture
   * @param moveLineNo Un numéro de ligne d'écriture
   * @return
   * @throws AxelorException
   */
  int createExcessPaymentWithAmount(
      List<MoveLine> debitMoveLines,
      BigDecimal remainingPaidAmount,
      Move move,
      int moveLineNo,
      Partner partner,
      Company company,
      PayVoucherElementToPay payVoucherElementToPay,
      Account account,
      LocalDate paymentDate)
      throws AxelorException;

  int useExcessPaymentWithAmountConsolidated(
      List<MoveLine> creditMoveLines,
      BigDecimal remainingPaidAmount,
      Move move,
      int moveLineNo,
      Partner partner,
      Company company,
      Account account,
      LocalDate date,
      LocalDate dueDate)
      throws AxelorException;

  BigDecimal getAmountRemainingFromPaymentMove(PaymentScheduleLine psl);

  BigDecimal getAmountRemainingFromPaymentMove(Invoice invoice);

  void useExcessPaymentOnMoveLinesDontThrowWithCacheManagement(
      List<MoveLine> companyPartnerDebitMoveLineList,
      List<MoveLine> companyPartnerCreditMoveLineList);
}
