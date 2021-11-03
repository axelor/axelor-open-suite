package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveCreateFromInvoiceService {

  /**
   * Créer une écriture comptable propre à la facture.
   *
   * @param invoice
   * @param consolidate
   * @return
   * @throws AxelorException
   */
  Move createMove(Invoice invoice) throws AxelorException;

  /**
   * Méthode permettant d'employer les trop-perçus 2 cas : - le compte des trop-perçus est le même
   * que celui de la facture : alors on lettre directement - le compte n'est pas le même : on créée
   * une O.D. de passage sur le bon compte
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  Move createMoveUseExcessPaymentOrDue(Invoice invoice) throws AxelorException;

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
  Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException;

  void createMoveUseExcessPayment(Invoice invoice) throws AxelorException;

  Move createMoveUseDebit(
      Invoice invoice, List<MoveLine> debitMoveLines, MoveLine invoiceCustomerMoveLine)
      throws AxelorException;
}
