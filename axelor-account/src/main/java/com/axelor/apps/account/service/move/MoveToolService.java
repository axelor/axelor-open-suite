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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface MoveToolService {

  boolean isMinus(Invoice invoice);

  /**
   * @param invoice
   *     <p>OperationTypeSelect 1 : Supplier invoice 2 : Supplier refund 3 : Customer invoice 4 :
   *     Customer refund
   * @return
   * @throws AxelorException
   */
  boolean isDebitCustomer(Invoice invoice, boolean reverseDirectionForNegativeAmount)
      throws AxelorException;

  /**
   * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte
   * client) de la facture Récupération par boucle. A privilégié si les lignes d'écriture sont déjà
   * managées par JPA ou si le nombre de lignes d'écriture n'est pas important (< 100).
   *
   * @param invoice Une facture
   * @return
   * @throws AxelorException
   */
  MoveLine getInvoiceCustomerMoveLineByLoop(Invoice invoice) throws AxelorException;

  /**
   * Method that returns all move lines of an invoice payment that are not completely lettered
   *
   * @param invoicePayment Invoice payment
   * @return
   */
  List<MoveLine> getInvoiceCustomerMoveLines(InvoicePayment invoicePayment);

  /**
   * Method that returns all the move lines of an invoice that are not completely lettered
   *
   * @param invoice Invoice
   * @return
   * @throws AxelorException
   */
  List<MoveLine> getInvoiceCustomerMoveLines(Invoice invoice) throws AxelorException;

  /**
   * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte
   * client) de la facture Récupération par requête. A privilégié si les lignes d'écritures ne sont
   * pas managées par JPA ou si le nombre d'écriture est très important (> 100)
   *
   * @param invoice Une facture
   * @return
   * @throws AxelorException
   */
  MoveLine getInvoiceCustomerMoveLineByQuery(Invoice invoice) throws AxelorException;

  /**
   * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le
   * compte client) de la facture ou du rejet de facture Récupération par boucle. A privilégié si
   * les lignes d'écriture sont déjà managées par JPA ou si le nombre de lignes d'écriture n'est pas
   * important (< 100).
   *
   * @param invoice Une facture
   * @param isInvoiceReject La facture est-elle rejetée?
   * @return
   * @throws AxelorException
   */
  MoveLine getCustomerMoveLineByLoop(Invoice invoice) throws AxelorException;

  /**
   * Fonction permettant de récuperer la ligne d'écriture (en débit et non complétement payée sur le
   * compte client) de la facture ou du rejet de facture Récupération par requête. A privilégié si
   * les lignes d'écritures ne sont pas managées par JPA ou si le nombre d'écriture est très
   * important (> 100)
   *
   * @param invoice Une facture
   * @param isInvoiceReject La facture est-elle rejetée?
   * @return
   * @throws AxelorException
   */
  MoveLine getCustomerMoveLineByQuery(Invoice invoice) throws AxelorException;

  Account getCustomerAccount(Partner partner, Company company, boolean isSupplierAccount)
      throws AxelorException;

  /**
   * Fonction permettant de savoir si toutes les lignes d'écritures utilise le même compte que celui
   * passé en paramètre
   *
   * @param moveLineList Une liste de lignes d'écritures
   * @param account Le compte que l'on souhaite tester
   * @return
   */
  boolean isSameAccount(List<MoveLine> moveLineList, Account account);

  /**
   * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
   *
   * @param creditMoveLineList Une liste de ligne d'écriture au credit
   * @return
   */
  BigDecimal getTotalCreditAmount(List<MoveLine> creditMoveLineList);

  /**
   * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
   *
   * @param creditMoveLineList Une liste de ligne d'écriture au credit
   * @return
   */
  BigDecimal getTotalDebitAmount(List<MoveLine> debitMoveLineList);

  BigDecimal getTotalCurrencyAmount(List<MoveLine> moveLineList);

  /**
   * Compute the balance amount : total debit - total credit
   *
   * @param moveLineList
   * @return
   */
  BigDecimal getBalanceAmount(List<MoveLine> moveLineList);

  /**
   * Compute the balance amount in currency origin : total debit - total credit
   *
   * @param moveLineList
   * @return
   */
  BigDecimal getBalanceCurrencyAmount(List<MoveLine> moveLineList);

  MoveLine getOrignalInvoiceFromRefund(Invoice invoice);

  BigDecimal getInTaxTotalRemaining(Invoice invoice) throws AxelorException;

  /**
   * Methode permettant de récupérer la contrepartie d'une ligne d'écriture
   *
   * @param moveLine Une ligne d'écriture
   * @return
   */
  MoveLine getOppositeMoveLine(MoveLine moveLine);

  List<MoveLine> orderListByDate(List<MoveLine> list);

  boolean isDebitMoveLine(MoveLine moveLine);

  List<MoveLine> getToReconcileCreditMoveLines(Move move);

  List<MoveLine> getToReconcileDebitMoveLines(Move move);

  MoveLine findMoveLineByAccount(Move move, Account account) throws AxelorException;

  void setOriginOnMoveLineList(Move move);

  @CallMethod
  boolean isTemporarilyClosurePeriodManage(Period period, Journal journal, User user)
      throws AxelorException;

  boolean getEditAuthorization(Move move) throws AxelorException;

  boolean checkMoveLinesCutOffDates(Move move);

  List<Move> getMovesWithDuplicatedOrigin(Move move);

  List<Move> findDaybookAndAccountingByYear(Set<Year> yearList);

  @CallMethod
  boolean isSimulatedMovePeriodClosed(Move move);

  void exceptionOnGenerateCounterpart(Move move) throws AxelorException;

  void setDescriptionOnMoveLineList(Move move);

  boolean isMultiCurrency(Move move);
}
