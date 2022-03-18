/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.*;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubtfulCustomerService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveToolService moveToolService;
  protected MoveRepository moveRepo;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineRepository moveLineRepo;
  protected ReconcileService reconcileService;
  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public DoubtfulCustomerService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveToolService moveToolService,
      MoveRepository moveRepo,
      MoveLineCreateService moveLineCreateService,
      MoveLineRepository moveLineRepo,
      ReconcileService reconcileService,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      InvoiceTermRepository invoiceTermRepo) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveToolService = moveToolService;
    this.moveRepo = moveRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineRepo = moveLineRepo;
    this.reconcileService = reconcileService;
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  /**
   * Procédure permettant de vérifier le remplissage des champs dans la société, nécessaire au
   * traitement du passage en client douteux
   *
   * @param company Une société
   * @throws AxelorException
   */
  public void testCompanyField(Company company) throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    accountConfigService.getDoubtfulCustomerAccount(accountConfig);
    accountConfigService.getAutoMiscOpeJournal(accountConfig);
    accountConfigService.getSixMonthDebtPassReason(accountConfig);
    accountConfigService.getThreeMonthDebtPassReason(accountConfig);
  }

  /**
   * Procédure permettant de créer les écritures de passage en client douteux pour chaque écriture
   * de facture
   *
   * @param moveList Une liste d'écritures de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  public void createDoubtFulCustomerMove(
      List<Move> moveList, Account doubtfulCustomerAccount, String debtPassReason)
      throws AxelorException {

    for (Move move : moveList) {

      this.createDoubtFulCustomerMove(move, doubtfulCustomerAccount, debtPassReason);
    }
  }

  /**
   * Procédure permettant de créer les écritures de passage en client douteux pour chaque écriture
   * de facture
   *
   * @param move Une écritures de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createDoubtFulCustomerMove(
      Move move, Account doubtfulCustomerAccount, String debtPassReason) throws AxelorException {

    log.debug("Concerned account move : {} ", move.getReference());

    Company company = move.getCompany();
    Partner partner = move.getPartner();
    Invoice invoice = move.getInvoice();
    Move newMove =
        moveCreateService.createMove(
            company.getAccountConfig().getAutoMiscOpeJournal(),
            company,
            move.getCurrency(),
            partner,
            move.getPaymentMode(),
            invoice != null
                ? invoice.getFiscalPosition()
                : (partner != null ? partner.getFiscalPosition() : null),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            move.getFunctionalOriginSelect(),
            move.getOrigin(),
            debtPassReason);
    newMove.setInvoice(invoice);
    LocalDate todayDate = appBaseService.getTodayDate(company);

    MoveLine invoicePartnerMoveLine = null;

    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAccount() != doubtfulCustomerAccount
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        invoicePartnerMoveLine = moveLine;
      }
    }

    String origin = "";
    BigDecimal amountRemaining = BigDecimal.ZERO;
    MoveLine creditMoveLine = null;
    if (invoicePartnerMoveLine != null) {
      amountRemaining = invoicePartnerMoveLine.getAmountRemaining();

      // Credit move line on partner account
      creditMoveLine =
          moveLineCreateService.createMoveLine(
              newMove,
              partner,
              invoicePartnerMoveLine.getAccount(),
              amountRemaining,
              false,
              todayDate,
              1,
              move.getOrigin(),
              debtPassReason);

      origin = creditMoveLine.getOrigin();
    }

    // Debit move line on doubtful customer account
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            newMove,
            partner,
            doubtfulCustomerAccount,
            amountRemaining,
            true,
            todayDate,
            2,
            origin,
            debtPassReason);
    debitMoveLine.setPassageReason(debtPassReason);

    // Generate Invoice Term and update old ones
    PaymentMode paymentMode = null;
    BankDetails bankDetails = null;
    User pfpUser = null;
    List<InvoiceTerm> invoiceTermToAdd = Lists.newArrayList();
    List<InvoiceTerm> invoiceTermToRemove = Lists.newArrayList();
    List<InvoiceTerm> invoiceTermToAddToInvoicePartnerMoveLine = Lists.newArrayList();
    for (InvoiceTerm invoiceTerm : invoicePartnerMoveLine.getInvoiceTermList()) {
      paymentMode = invoiceTerm.getPaymentMode();
      bankDetails = invoiceTerm.getBankDetails();
      pfpUser = invoiceTerm.getPfpValidatorUser();
      if (invoiceTerm.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
          && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0) {

        // Remove invoice from old invoice term
        invoiceTermToRemove.add(invoiceTerm);
        // Copy invoice term on put it on new move line
        InvoiceTerm copy = invoiceTermRepo.copy(invoiceTerm, false);
        debitMoveLine.addInvoiceTermListItem(copy);
        invoiceTermToAdd.add(copy);
        log.debug(
            "Montant = Montant restant -> Ajout d'une échéance sur la facture "
                + " et debit move line, Montant {}, Montant restant {}, Pourcentage {}",
            copy.getAmount(),
            copy.getAmountRemaining(),
            copy.getPercentage());
        log.debug(
            "Suppression d'une échéance de la fature "
                + " Montant {}, Montant restant {}, Pourcentage {}",
            invoiceTerm.getAmount(),
            invoiceTerm.getAmountRemaining(),
            invoiceTerm.getPercentage());
      }
      if (invoiceTerm.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
          && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) != 0) {
        log.debug("Montant != Montant restant");
        BigDecimal amount = invoiceTerm.getAmount();
        BigDecimal remainingAmount = invoiceTerm.getAmountRemaining();
        BigDecimal percentage = invoiceTerm.getPercentage();
        // Create new invoice term on new move line with amount remaining
        InvoiceTerm copyOnNewMoveLine = invoiceTermRepo.copy(invoiceTerm, false);
        copyOnNewMoveLine.setAmount(remainingAmount);
        BigDecimal newPercentage =
            remainingAmount
                .multiply(percentage)
                .divide(amount, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        copyOnNewMoveLine.setPercentage(newPercentage);
        debitMoveLine.addInvoiceTermListItem(copyOnNewMoveLine);
        invoiceTermToAdd.add(copyOnNewMoveLine);
        // Update current invoice term
        invoiceTerm.setAmount(amountRemaining);
        invoiceTerm.setPercentage(newPercentage);
        invoiceTermToRemove.add(invoiceTerm);
        // Create a paid invoice term on old move line
        InvoiceTerm copyOnOldMoveLine = invoiceTermRepo.copy(invoiceTerm, false);
        copyOnOldMoveLine.setAmount(amount.subtract(remainingAmount));
        copyOnOldMoveLine.setAmountRemaining(BigDecimal.ZERO);
        copyOnOldMoveLine.setIsPaid(true);
        copyOnOldMoveLine.setPercentage(percentage.subtract(newPercentage));
        invoiceTermToAddToInvoicePartnerMoveLine.add(copyOnOldMoveLine);
        invoiceTermToAdd.add(copyOnOldMoveLine);
      }
    }
    for (InvoiceTerm it : invoiceTermToAddToInvoicePartnerMoveLine) {
      log.debug(
          "Échéance à ajouter à la ligne d'écriture client initiale: Montant {}, Montant Restant {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getPercentage());
      invoicePartnerMoveLine.addInvoiceTermListItem(it);
    }
    for (InvoiceTerm it : invoiceTermToAdd) {
      log.debug(
          "Échéance à ajouter à la facture: Montant {}, Montant Restant {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getPercentage());
      invoice.addInvoiceTermListItem(it);
    }
    for (InvoiceTerm it : invoiceTermToRemove) {
      log.debug(
          "Échéance à supprimerde la facture: Montant {}, Montant Restant {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getPercentage());
      invoice.removeInvoiceTermListItem(it);
      it.setInvoice(null);
    }

    // Create invoice term on new credit move line
    InvoiceTerm newInvoiceTerm = new InvoiceTerm();
    newInvoiceTerm.setIsCustomized(true);
    newInvoiceTerm.setIsPaid(false);
    newInvoiceTerm.setDueDate(todayDate);
    newInvoiceTerm.setIsHoldBack(false);
    newInvoiceTerm.setEstimatedPaymentDate(null);
    newInvoiceTerm.setAmount(amountRemaining);
    newInvoiceTerm.setAmountRemaining(BigDecimal.ZERO);
    newInvoiceTerm.setPaymentMode(paymentMode);
    newInvoiceTerm.setBankDetails(bankDetails);
    newInvoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    newInvoiceTerm.setPfpValidatorUser(pfpUser);
    newInvoiceTerm.setPfpGrantedAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPfpRejectedAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPercentage(BigDecimal.valueOf(100));
    creditMoveLine.addInvoiceTermListItem(newInvoiceTerm);

    log.debug(
        "Debit move Line " + " Debit {}, Credit {}, Montant restant {}",
        debitMoveLine.getDebit(),
        debitMoveLine.getCredit(),
        debitMoveLine.getAmountRemaining());

    log.debug(
        "Credit move Line " + " Debit {}, Credit {}, Montant restant {}",
        creditMoveLine.getDebit(),
        creditMoveLine.getCredit(),
        creditMoveLine.getAmountRemaining());

    log.debug(
        "invoicePartnerMoveLine " + " Debit {}, Credit {}, Montant restant {}",
        invoicePartnerMoveLine.getDebit(),
        invoicePartnerMoveLine.getCredit(),
        invoicePartnerMoveLine.getAmountRemaining());

    log.debug("Invoice partner move line invoice term");
    for (InvoiceTerm it : invoicePartnerMoveLine.getInvoiceTermList()) {
      log.debug(
          "Montant {}, Montant Restant {}, Montant payé {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getAmountPaid(),
          it.getPercentage());
    }

    log.debug("Debit move line invoice term");
    for (InvoiceTerm it : debitMoveLine.getInvoiceTermList()) {
      log.debug(
          "Montant {}, Montant Restant {}, Montant payé {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getAmountPaid(),
          it.getPercentage());
    }
    log.debug("Credit move line invoice term");
    for (InvoiceTerm it : creditMoveLine.getInvoiceTermList()) {
      log.debug(
          "Montant {}, Montant Restant {},Montant payé {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getAmountPaid(),
          it.getPercentage());
    }

    log.debug("Invoice term of invoice");
    for (InvoiceTerm it : invoice.getInvoiceTermList()) {
      log.debug(
          "Montant {}, Montant Restant {}, Montant payé {}, Pourcentage {}",
          it.getAmount(),
          it.getAmountRemaining(),
          it.getAmountPaid(),
          it.getPercentage());
    }

    newMove.getMoveLineList().add(debitMoveLine);
    newMove.getMoveLineList().add(creditMoveLine);

    moveValidateService.accounting(newMove);
    moveRepo.save(newMove);

    if (creditMoveLine != null) {
      Reconcile reconcile =
          reconcileService.createReconcile(
              invoicePartnerMoveLine, creditMoveLine, amountRemaining, false);
      if (reconcile != null) {
        reconcileService.confirmReconcile(reconcile, true);
      }
    }

    this.invoiceProcess(newMove, doubtfulCustomerAccount, debtPassReason);
  }

  public void createDoubtFulCustomerRejectMove(
      List<MoveLine> moveLineList, Account doubtfulCustomerAccount, String debtPassReason)
      throws AxelorException {

    for (MoveLine moveLine : moveLineList) {

      this.createDoubtFulCustomerRejectMove(moveLine, doubtfulCustomerAccount, debtPassReason);
    }
  }

  /**
   * Procédure permettant de créer les écritures de passage en client douteux pour chaque ligne
   * d'écriture de rejet de facture
   *
   * @param moveLine Une ligne d'écritures de rejet de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createDoubtFulCustomerRejectMove(
      MoveLine moveLine, Account doubtfulCustomerAccount, String debtPassReason)
      throws AxelorException {

    log.debug("Ecriture concernée : {} ", moveLine.getName());
    Company company = moveLine.getMove().getCompany();
    Partner partner = moveLine.getPartner();
    LocalDate todayDate = appBaseService.getTodayDate(company);

    Move newMove =
        moveCreateService.createMove(
            company.getAccountConfig().getAutoMiscOpeJournal(),
            company,
            null,
            partner,
            moveLine.getMove().getPaymentMode(),
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            moveLine.getMove().getFunctionalOriginSelect(),
            moveLine.getName(),
            debtPassReason);

    BigDecimal amountRemaining = moveLine.getAmountRemaining();

    // Ecriture au crédit sur le 411
    MoveLine creditMoveLine =
        moveLineCreateService.createMoveLine(
            newMove,
            partner,
            moveLine.getAccount(),
            amountRemaining,
            false,
            todayDate,
            1,
            moveLine.getName(),
            debtPassReason);
    newMove.addMoveLineListItem(creditMoveLine);

    Reconcile reconcile =
        reconcileService.createReconcile(moveLine, creditMoveLine, amountRemaining, false);
    if (reconcile != null) {
      reconcileService.confirmReconcile(reconcile, true);
    }

    // Ecriture au débit sur le 416 (client douteux)
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            newMove,
            newMove.getPartner(),
            doubtfulCustomerAccount,
            amountRemaining,
            true,
            todayDate,
            2,
            moveLine.getName(),
            debtPassReason);
    newMove.getMoveLineList().add(debitMoveLine);

    debitMoveLine.setInvoiceReject(moveLine.getInvoiceReject());
    debitMoveLine.setPassageReason(debtPassReason);

    moveValidateService.accounting(newMove);
    moveRepo.save(newMove);

    this.invoiceRejectProcess(debitMoveLine, doubtfulCustomerAccount, debtPassReason);
  }

  /**
   * Procédure permettant de mettre à jour le motif de passage en client douteux, et créer
   * l'évènement lié.
   *
   * @param moveList Une liste d'éciture de facture
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   */
  public void updateDoubtfulCustomerMove(
      List<Move> moveList, Account doubtfulCustomerAccount, String debtPassReason) {

    for (Move move : moveList) {

      for (MoveLine moveLine : move.getMoveLineList()) {

        if (moveLine.getAccount().equals(doubtfulCustomerAccount)
            && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {

          moveLine.setPassageReason(debtPassReason);
          moveLineRepo.save(moveLine);

          break;
        }
      }
    }
  }

  /**
   * Procédure permettant de mettre à jour les champs de la facture avec la nouvelle écriture de
   * débit sur le compte 416
   *
   * @param move La nouvelle écriture de débit sur le compte 416
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   * @throws AxelorException
   */
  public Invoice invoiceProcess(Move move, Account doubtfulCustomerAccount, String debtPassReason)
      throws AxelorException {

    Invoice invoice = move.getInvoice();

    if (invoice != null) {

      invoice.setOldMove(invoice.getMove());
      invoice.setMove(move);
      FiscalPosition fiscalPosition = invoice.getFiscalPosition();

      if (invoice.getPartner() != null) {
        doubtfulCustomerAccount =
            Beans.get(FiscalPositionAccountService.class)
                .getAccount(fiscalPosition, doubtfulCustomerAccount);
      }
      invoice.setPartnerAccount(doubtfulCustomerAccount);
      invoice.setDoubtfulCustomerOk(true);
      // Recalcule du restant à payer de la facture
      invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    }
    return invoice;
  }

  /**
   * Procédure permettant de mettre à jour les champs d'une facture rejetée avec la nouvelle
   * écriture de débit sur le compte 416
   *
   * @param moveLine La nouvelle ligne d'écriture de débit sur le compte 416
   * @param doubtfulCustomerAccount Un compte client douteux
   * @param debtPassReason Un motif de passage en client douteux
   */
  public Invoice invoiceRejectProcess(
      MoveLine moveLine, Account doubtfulCustomerAccount, String debtPassReason) {

    Invoice invoice = moveLine.getInvoiceReject();

    invoice.setRejectMoveLine(moveLine);
    invoice.setDoubtfulCustomerOk(true);

    return invoice;
  }

  /**
   * Fonction permettant de récupérer les écritures de facture à transférer sur le compte client
   * douteux
   *
   * @param rule Le règle à appliquer :
   *     <ul>
   *       <li>0 = Créance de + 6 mois
   *       <li>1 = Créance de + 3 mois
   *     </ul>
   *
   * @param doubtfulCustomerAccount Le compte client douteux
   * @param company La société
   * @return Les écritures de facture à transférer sur le compte client douteux
   */
  public List<Move> getMove(int rule, Account doubtfulCustomerAccount, Company company) {

    LocalDate date = null;

    switch (rule) {

        // Créance de + 6 mois
      case 0:
        date =
            Beans.get(AppBaseService.class)
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getSixMonthDebtMonthNumber());
        break;

        // Créance de + 3 mois
      case 1:
        date =
            Beans.get(AppBaseService.class)
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getThreeMonthDebtMontsNumber());
        break;

      default:
        break;
    }

    log.debug("Date de créance prise en compte : {} ", date);

    String request =
        "SELECT DISTINCT m "
            + "FROM MoveLine ml "
            + "JOIN ml.move m "
            + "JOIN ml.invoiceTermList invoiceTermList "
            + " WHERE m.company.id = "
            + company.getId()
            + " AND ml.account.useForPartnerBalance = true "
            + " AND m.functionalOriginSelect = "
            + MoveRepository.FUNCTIONAL_ORIGIN_SALE
            + " AND ml.amountRemaining > 0.00 AND ml.debit > 0.00 "
            + " AND ml.account.id != "
            + doubtfulCustomerAccount.getId()
            + " AND invoiceTermList.amountRemaining > 0.00 "
            + " AND invoiceTermList.dueDate < '"
            + date.toString()
            + "'";

    log.debug("Requete : {} ", request);

    Query query = JPA.em().createQuery(request);

    @SuppressWarnings("unchecked")
    List<Move> moveList = query.getResultList();

    return moveList;
  }

  /**
   * Fonction permettant de récupérer les lignes d'écriture de rejet de facture à transférer sur le
   * compte client douteux
   *
   * @param rule Le règle à appliquer :
   *     <ul>
   *       <li>0 = Créance de + 6 mois
   *       <li>1 = Créance de + 3 mois
   *     </ul>
   *
   * @param doubtfulCustomerAccount Le compte client douteux
   * @param company La société
   * @return Les lignes d'écriture de rejet de facture à transférer sur le comtpe client douteux
   */
  public List<? extends MoveLine> getRejectMoveLine(
      int rule, Account doubtfulCustomerAccount, Company company) {

    LocalDate date = null;
    List<? extends MoveLine> moveLineList = null;

    switch (rule) {

        // Créance de + 6 mois
      case 0:
        date =
            Beans.get(AppBaseService.class)
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getSixMonthDebtMonthNumber());
        moveLineList =
            moveLineRepo
                .all()
                .filter(
                    "self.move.company = ?1 AND self.account.useForPartnerBalance = 'true' "
                        + "AND self.invoiceReject IS NOT NULL AND self.amountRemaining > 0.00 AND self.debit > 0.00 AND self.dueDate < ?2 "
                        + "AND self.account != ?3 "
                        + "AND self.invoiceReject.operationTypeSelect = ?4",
                    company,
                    date,
                    doubtfulCustomerAccount,
                    InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
                .fetch();
        break;

        // Créance de + 3 mois
      case 1:
        date =
            Beans.get(AppBaseService.class)
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getThreeMonthDebtMontsNumber());
        moveLineList =
            moveLineRepo
                .all()
                .filter(
                    "self.move.company = ?1 AND self.account.useForPartnerBalance = 'true' "
                        + "AND self.invoiceReject IS NOT NULL AND self.amountRemaining > 0.00 AND self.debit > 0.00 AND self.dueDate < ?2 "
                        + "AND self.account != ?3 "
                        + "AND self.invoiceReject.operationTypeSelect = ?4",
                    company,
                    date,
                    doubtfulCustomerAccount,
                    InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
                .fetch();
        break;

      default:
        break;
    }

    log.debug("Date de créance prise en compte : {} ", date);

    return moveLineList;
  }
}
