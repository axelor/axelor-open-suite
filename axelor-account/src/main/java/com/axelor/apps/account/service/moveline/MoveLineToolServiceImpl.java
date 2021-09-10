package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoveLineToolServiceImpl implements MoveLineToolService {

  protected TaxService taxService;
  protected CurrencyService currencyService;

  @Inject
  public MoveLineToolServiceImpl(TaxService taxService, CurrencyService currencyService) {
    this.taxService = taxService;
    this.currencyService = currencyService;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur
   * le compte client) de la facture
   *
   * @param invoice Une facture
   * @return
   */
  @Override
  public MoveLine getCreditCustomerMoveLine(Invoice invoice) {
    if (invoice.getMove() != null) {
      return this.getCreditCustomerMoveLine(invoice.getMove());
    }
    return null;
  }

  /**
   * Method that returns all credit move lines of an invoice that are not completely lettered
   *
   * @param invoice Invoice
   * @return
   */
  @Override
  public List<MoveLine> getCreditCustomerMoveLines(Invoice invoice) {
    List<MoveLine> moveLines = new ArrayList<MoveLine>();
    if (invoice.getMove() != null) {
      moveLines = getCreditCustomerMoveLines(invoice.getMove());
    }
    return moveLines;
  }
  /**
   * Method that returns all credit move lines of a move that are not completely lettered
   *
   * @param move Invoice move
   * @return
   */
  @Override
  public List<MoveLine> getCreditCustomerMoveLines(Move move) {

    List<MoveLine> moveLines = Lists.newArrayList();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        moveLines.add(moveLine);
      }
    }
    return moveLines;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur
   * le compte client) de l'écriture de facture
   *
   * @param move Une écriture de facture
   * @return
   */
  @Override
  public MoveLine getCreditCustomerMoveLine(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        return moveLine;
      }
    }
    return null;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur
   * le compte client) de la facture
   *
   * @param invoice Une facture
   * @return
   */
  @Override
  public MoveLine getDebitCustomerMoveLine(Invoice invoice) {
    if (invoice.getMove() != null) {
      return this.getDebitCustomerMoveLine(invoice.getMove());
    }
    return null;
  }

  /**
   * Method that returns all debit move lines of an invoice that are not completely lettered
   *
   * @param invoice Invoice
   * @return
   */
  @Override
  public List<MoveLine> getDebitCustomerMoveLines(Invoice invoice) {
    List<MoveLine> moveLines = new ArrayList<MoveLine>();
    if (invoice.getMove() != null) {
      moveLines = this.getDebitCustomerMoveLines(invoice.getMove());
    }
    return moveLines;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur
   * le compte client) de l'écriture de facture
   *
   * @param move Une écriture de facture
   * @return
   */
  @Override
  public MoveLine getDebitCustomerMoveLine(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        return moveLine;
      }
    }
    return null;
  }

  /**
   * Method that returns all debit move lines of a move that are not completely lettered
   *
   * @param move Invoice move
   * @return
   */
  @Override
  public List<MoveLine> getDebitCustomerMoveLines(Move move) {

    List<MoveLine> moveLines = Lists.newArrayList();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        moveLines.add(moveLine);
      }
    }
    return moveLines;
  }

  /**
   * Fonction permettant de générér automatiquement la description des lignes d'écritures
   *
   * @param journal Le journal de l'écriture
   * @param origin Le n° pièce réglée, facture, avoir ou de l'opération rejetée
   * @return
   */
  @Override
  public String determineDescriptionMoveLine(Journal journal, String origin, String description) {
    String descriptionComputed = "";
    if (journal == null) {
      return "";
    }

    if (journal.getDescriptionModel() != null) {
      descriptionComputed += journal.getDescriptionModel();
    }

    if (journal.getDescriptionIdentificationOk() && origin != null) {
      if (!descriptionComputed.isEmpty()) {
        descriptionComputed += " ";
      }
      descriptionComputed += origin;
    }

    if (!journal.getIsInvoiceMoveConsolidated() && description != null) {
      if (!descriptionComputed.isEmpty()) {
        descriptionComputed += " - ";
      }
      descriptionComputed += description;
    }
    return descriptionComputed;
  }

  /**
   * Method used to recover all credit reconciliable move line from a move line list
   *
   * @param moveLineList
   * @return reconciliableCreditMoveLineList
   */
  @Override
  public List<MoveLine> getReconciliableCreditMoveLines(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableCreditMoveLineList = new ArrayList<>();

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getAccount().getReconcileOk()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0) {
        reconciliableCreditMoveLineList.add(moveLine);
      }
    }

    return reconciliableCreditMoveLineList;
  }

  /**
   * Method used to recover all debit reconciliable move line from a move line list
   *
   * @param moveLineList
   * @return reconciliableDebitMoveLineList
   */
  @Override
  public List<MoveLine> getReconciliableDebitMoveLines(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableDebitMoveLineList = new ArrayList<>();

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getAccount().getReconcileOk()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        reconciliableDebitMoveLineList.add(moveLine);
      }
    }

    return reconciliableDebitMoveLineList;
  }

  @Override
  public TaxLine getTaxLine(MoveLine moveLine) throws AxelorException {
    TaxLine taxLine = null;
    LocalDate date = moveLine.getDate();
    if (date == null) {
      date = moveLine.getDueDate();
    }
    if (moveLine.getAccount() != null && moveLine.getAccount().getDefaultTax() != null) {
      taxService.getTaxLine(moveLine.getAccount().getDefaultTax(), date);
    }
    return taxLine;
  }

  @Override
  public MoveLine setCurrencyAmount(MoveLine moveLine) {
    Move move = moveLine.getMove();
    if (move.getMoveLineList().size() == 0) {
      try {
        moveLine.setCurrencyRate(
            currencyService.getCurrencyConversionRate(
                move.getCurrency(), move.getCompany().getCurrency()));
      } catch (AxelorException e1) {
        TraceBackService.trace(e1);
      }
    } else {
      moveLine.setCurrencyRate(move.getMoveLineList().get(0).getCurrencyRate());
    }
    if (!move.getCurrency().equals(move.getCompany().getCurrency())) {
      BigDecimal unratedAmount = moveLine.getDebit().add(moveLine.getCredit());
      moveLine.setCurrencyAmount(
          unratedAmount.divide(moveLine.getCurrencyRate(), MathContext.DECIMAL128));
    }
    return moveLine;
  }
}
