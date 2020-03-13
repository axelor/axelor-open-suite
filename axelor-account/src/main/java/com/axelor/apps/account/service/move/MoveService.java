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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public interface MoveService {

  public MoveLineService getMoveLineService();

  public MoveCreateService getMoveCreateService();

  public MoveValidateService getMoveValidateService();

  public MoveRemoveService getMoveRemoveService();

  public MoveToolService getMoveToolService();

  public ReconcileService getReconcileService();

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Move createMove(Invoice invoice) throws AxelorException;

  /**
   * Méthode permettant d'employer les trop-perçus 2 cas : - le compte des trop-perçus est le même
   * que celui de la facture : alors on lettre directement - le compte n'est pas le même : on créée
   * une O.D. de passage sur le bon compte
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public Move createMoveUseExcessPaymentOrDue(Invoice invoice) throws AxelorException;

  /**
   * Méthode permettant d'employer les dûs sur l'avoir On récupère prioritairement les dûs
   * (factures) selectionné sur l'avoir, puis les autres dûs du tiers
   *
   * <p>2 cas : - le compte des dûs est le même que celui de l'avoir : alors on lettre directement -
   * le compte n'est pas le même : on créée une O.D. de passage sur le bon compte
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  public Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException;

  public void createMoveUseExcessPayment(Invoice invoice) throws AxelorException;

  public Move createMoveUseDebit(
      Invoice invoice, List<MoveLine> debitMoveLines, MoveLine invoiceCustomerMoveLine)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Move generateReverse(Move move) throws AxelorException;

  public MoveLine findMoveLineByAccount(Move move, Account account) throws AxelorException;

  public Map<String, Object> computeTotals(Move move);

  public String filterPartner(Move move);
}
