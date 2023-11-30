package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class InvoiceBankPaymentServiceImpl implements InvoiceBankPaymentService {

  protected ReconcileService reconcileService;
  protected MoveReverseService moveReverseService;
  protected InvoiceRepository invoiceRepository;

  @Inject
  public InvoiceBankPaymentServiceImpl(
      ReconcileService reconcileService,
      MoveReverseService moveReverseService,
      InvoiceRepository invoiceRepository) {
    this.reconcileService = reconcileService;
    this.moveReverseService = moveReverseService;
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelLcr(Invoice invoice) throws AxelorException {
    if (invoice == null
        || !invoice.getLcrAccounted()
        || invoice.getAmountRemaining().signum() == 0
        || invoice.getOldMove() == null) {
      return;
    }

    invoice = invoiceRepository.find(invoice.getId());

    Move lcrMove = invoice.getMove();
    Move oldMove = invoice.getOldMove();

    MoveLine lcrCreditMoveLine =
        lcrMove.getMoveLineList().stream()
            .filter(ml -> ml.getCredit().signum() > 0)
            .findFirst()
            .orElse(null);

    // Déréconcilier les deux écritures sur la ligne qu'on retrouve du lcrMove
    /*if (lcrCreditMoveLine != null && ObjectUtils.isEmpty(lcrCreditMoveLine.getCreditReconcileList()) && lcrCreditMoveLine.getCreditReconcileList().size() == 1){
        Reconcile reconcile = lcrCreditMoveLine.getCreditReconcileList().get(0);
        reconcileService.unreconcile(reconcile);
    }*/
    resetInvoiceBeforeLcrCancellation(invoice, oldMove, lcrMove);
    Move reverseMove =
        moveReverseService.generateReverse(lcrMove, true, true, true, lcrMove.getDate());
    // Faire une nouvelle écriture qui va compenser le lcrMove sur les deux comptes (regarder
    // l'extourne)

    // Remettre lcrAccounted a false, remettre oldMove a move et vérifier les montants

  }

  protected void resetInvoiceBeforeLcrCancellation(Invoice invoice, Move oldMove, Move lcrMove) {
    if (lcrMove == null || oldMove == null) {
      return;
    }

    List<InvoiceTerm> oldInvoiceTermList = new ArrayList<>();
    List<InvoiceTerm> lcrInvoiceTermList = new ArrayList<>();

    MoveLine lcrDebitMoveLine =
        lcrMove.getMoveLineList().stream()
            .filter(ml -> ml.getDebit().signum() != 0)
            .findFirst()
            .orElse(null);
    if (lcrDebitMoveLine == null) {
      return;
    }
    lcrInvoiceTermList.addAll(lcrDebitMoveLine.getInvoiceTermList());

    MoveLine debitMoveLine =
        oldMove.getMoveLineList().stream()
            .filter(ml -> ml.getDebit().signum() != 0)
            .findFirst()
            .orElse(null);
    if (debitMoveLine == null) {
      return;
    }
    oldInvoiceTermList.addAll(debitMoveLine.getInvoiceTermList());

    invoice.setLcrAccounted(false);
    invoice.setOldMove(null);
    invoice.setMove(oldMove);

    resetInvoiceTermAmounts(invoice, oldInvoiceTermList);
    replaceInvoiceTerms(invoice, oldInvoiceTermList, lcrInvoiceTermList);
  }

  protected void resetInvoiceTermAmounts(Invoice invoice, List<InvoiceTerm> oldInvoiceTermList) {
    for (InvoiceTerm invoiceTerm : oldInvoiceTermList) {
      if (!invoice.getInvoiceTermList().contains(invoiceTerm)) {
        invoiceTerm.setAmountRemaining(invoiceTerm.getAmount());
        invoiceTerm.setCompanyAmountRemaining(invoiceTerm.getCompanyAmountRemaining());
        invoiceTerm.setIsPaid(false);
      }
    }
  }

  // Look for the one in InvoiceTermReplaceServiceImpl after the merge of the 72187
  @Transactional(rollbackOn = {Exception.class})
  protected void replaceInvoiceTerms(
      Invoice invoice,
      List<InvoiceTerm> newInvoiceTermList,
      List<InvoiceTerm> invoiceTermListToRemove) {
    if (ObjectUtils.isEmpty(newInvoiceTermList) || ObjectUtils.isEmpty(invoiceTermListToRemove)) {
      return;
    }

    for (InvoiceTerm invoiceTerm : newInvoiceTermList) {
      invoice.addInvoiceTermListItem(invoiceTerm);
    }

    for (InvoiceTerm invoiceTerm : invoiceTermListToRemove) {
      invoice.removeInvoiceTermListItem(invoiceTerm);
      invoiceTerm.setInvoice(null);
    }

    invoiceRepository.save(invoice);
  }
}
