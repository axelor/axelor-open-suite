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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
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
      AppBaseService appBaseService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveToolService = moveToolService;
    this.moveRepo = moveRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineRepo = moveLineRepo;
    this.reconcileService = reconcileService;
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
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
            invoice.getCurrency(),
            partner,
            move.getPaymentMode(),
            invoice != null
                ? invoice.getFiscalPosition()
                : (partner != null ? partner.getFiscalPosition() : null),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            move.getFunctionalOriginSelect(),
            move.getInvoice().getInvoiceId(),
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

      // Debit move line on partner account
      creditMoveLine =
          moveLineCreateService.createMoveLine(
              newMove,
              partner,
              invoicePartnerMoveLine.getAccount(),
              amountRemaining,
              false,
              todayDate,
              1,
              move.getInvoice().getInvoiceId(),
              debtPassReason);
      newMove.getMoveLineList().add(creditMoveLine);

      origin = creditMoveLine.getOrigin();
    }

    // Credit move line on partner account
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
    newMove.getMoveLineList().add(debitMoveLine);
    debitMoveLine.setPassageReason(debtPassReason);

    moveValidateService.validate(newMove);
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

    moveValidateService.validate(newMove);
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
            appBaseService
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getSixMonthDebtMonthNumber());
        break;

        // Créance de + 3 mois
      case 1:
        date =
            appBaseService
                .getTodayDate(company)
                .minusMonths(company.getAccountConfig().getThreeMonthDebtMontsNumber());
        break;

      default:
        break;
    }

    log.debug("Date de créance prise en compte : {} ", date);

    String request =
        "SELECT DISTINCT m FROM MoveLine ml, Move m WHERE ml.move = m AND m.company.id = "
            + company.getId()
            + " AND ml.account.useForPartnerBalance = true "
            + "AND m.invoice IS NOT NULL AND ml.amountRemaining > 0.00 AND ml.debit > 0.00 AND ml.dueDate < '"
            + date.toString()
            + "' AND ml.account.id != "
            + doubtfulCustomerAccount.getId()
            + " AND m.invoice.operationTypeSelect = "
            + InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;

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
            appBaseService
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
            appBaseService
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
