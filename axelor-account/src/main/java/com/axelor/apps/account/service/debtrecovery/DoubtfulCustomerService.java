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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermReplaceService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
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
  protected InvoiceTermReplaceService invoiceTermReplaceService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;

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
      InvoiceTermRepository invoiceTermRepo,
      InvoiceTermReplaceService invoiceTermReplaceService,
      MoveLineInvoiceTermService moveLineInvoiceTermService) {

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
    this.invoiceTermReplaceService = invoiceTermReplaceService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
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

  public List<MoveLine> getInvoicePartnerMoveLines(Move move, Account doubtfulCustomerAccount) {
    List<MoveLine> moveLineList = new ArrayList<MoveLine>();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount() != null
          && moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getAmountRemaining().signum() > 0
          && !moveLine.getAccount().equals(doubtfulCustomerAccount)
          && moveLine.getDebit().signum() > 0) {
        moveLineList.add(moveLine);
      }
    }

    return moveLineList;
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
            MoveRepository.FUNCTIONAL_ORIGIN_DOUBTFUL_CUSTOMER,
            move.getOrigin(),
            debtPassReason,
            invoice != null ? invoice.getCompanyBankDetails() : move.getCompanyBankDetails());
    newMove.setOriginDate(move.getOriginDate());
    newMove.setInvoice(invoice);
    LocalDate todayDate = appBaseService.getTodayDate(company);

    List<MoveLine> invoicePartnerMoveLines =
        this.getInvoicePartnerMoveLines(move, doubtfulCustomerAccount);

    String origin = "";
    BigDecimal amountRemaining = BigDecimal.ZERO;
    BigDecimal totalCurrencyAmountRemaining = BigDecimal.ZERO;
    BigDecimal currencyRate = BigDecimal.ONE;
    List<MoveLine> creditMoveLines = new ArrayList<>();

    if (invoicePartnerMoveLines != null) {
      for (MoveLine moveLine : invoicePartnerMoveLines) {
        amountRemaining = amountRemaining.add(moveLine.getAmountRemaining().abs());
        BigDecimal currencyAmountRemaining = this.getCurrencyAmountRemaining(moveLine);
        totalCurrencyAmountRemaining = totalCurrencyAmountRemaining.add(currencyAmountRemaining);
        currencyRate = moveLine.getCurrencyRate();

        // Credit move line on partner account
        MoveLine creditMoveLine =
            moveLineCreateService.createMoveLine(
                newMove,
                partner,
                moveLine.getAccount(),
                currencyAmountRemaining,
                moveLine.getAmountRemaining(),
                currencyRate,
                false,
                todayDate,
                todayDate,
                null,
                1,
                move.getOrigin(),
                debtPassReason);
        moveLineInvoiceTermService.generateDefaultInvoiceTerm(newMove, creditMoveLine, false);

        origin = creditMoveLine.getOrigin();
        creditMoveLines.add(creditMoveLine);
      }
    }

    // Debit move line on doubtful customer account
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            newMove,
            partner,
            doubtfulCustomerAccount,
            totalCurrencyAmountRemaining,
            amountRemaining,
            currencyRate,
            true,
            todayDate,
            todayDate,
            null,
            2,
            origin,
            debtPassReason);
    moveLineInvoiceTermService.generateDefaultInvoiceTerm(newMove, debitMoveLine, false);
    debitMoveLine.setPassageReason(debtPassReason);

    newMove.addMoveLineListItem(debitMoveLine);
    creditMoveLines.forEach(newMove::addMoveLineListItem);
    moveValidateService.accounting(newMove);

    for (MoveLine moveLine : invoicePartnerMoveLines) {
      invoiceTermReplaceService.replaceInvoiceTerms(
          invoice, newMove, Collections.singletonList(moveLine), moveLine.getAccount());
    }

    this.invoiceProcess(newMove, doubtfulCustomerAccount, debtPassReason);
  }

  protected BigDecimal getCurrencyAmountRemaining(MoveLine moveLine) {
    if (moveLine.getCurrencyRate().compareTo(BigDecimal.ONE) == 0) {
      return moveLine.getAmountRemaining();
    } else {
      return moveLine.getInvoiceTermList().stream()
          .map(InvoiceTerm::getAmountRemaining)
          .reduce(BigDecimal::add)
          .orElse(BigDecimal.ZERO);
    }
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

    log.debug("Concerned move : {} ", moveLine.getName());
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
            MoveRepository.FUNCTIONAL_ORIGIN_DOUBTFUL_CUSTOMER,
            moveLine.getName(),
            debtPassReason,
            moveLine.getMove().getCompanyBankDetails());

    BigDecimal amountRemaining = moveLine.getAmountRemaining().abs();

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
      reconcileService.confirmReconcile(reconcile, true, true);
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

  public List<Long> getMoveLineIds(
      Company company, Account doubtfulCustomerAccount, int debtMonthNumber, boolean isReject) {
    LocalDate date = appBaseService.getTodayDate(company).minusMonths(debtMonthNumber);

    StringBuilder query =
        new StringBuilder(
            "self.move.company = :company "
                + "AND self.account.useForPartnerBalance IS TRUE "
                + "AND self.amountRemaining > 0.00 "
                + "AND self.debit > 0.00 "
                + "AND self.dueDate < :date "
                + "AND self.account <> :doubtfulCustomerAccount ");

    if (isReject) {
      query.append(
          "AND self.invoiceReject IS NOT NULL AND self.invoiceReject.operationTypeSelect = :operationTypeSale");
    } else {
      query.append("AND self.move.functionalOriginSelect = :functionalOriginSale");
    }

    return moveLineRepo.all().filter(query.toString()).bind("company", company).bind("date", date)
        .bind("doubtfulCustomerAccount", doubtfulCustomerAccount)
        .bind("functionalOriginSale", MoveRepository.FUNCTIONAL_ORIGIN_SALE)
        .bind("operationTypeSale", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE).fetch().stream()
        .map(MoveLine::getId)
        .collect(Collectors.toList());
  }

  public Map<Long, Pair<Integer, Boolean>> getMoveLineMap(
      Company company, Account doubtfulCustomerAccount) {
    Map<Long, Pair<Integer, Boolean>> moveLineMap = new HashMap<>();

    for (Pair<Integer, Boolean> pair : this.getPairList(company.getAccountConfig())) {
      for (long id :
          this.getMoveLineIds(company, doubtfulCustomerAccount, pair.getLeft(), pair.getRight())) {
        moveLineMap.put(id, pair);
      }
    }

    return moveLineMap;
  }

  public List<Pair<Integer, Boolean>> getPairList(AccountConfig accountConfig) {
    int sixMonthDebtMonthNumber = accountConfig.getSixMonthDebtMonthNumber();
    int threeMonthDebtMonthNumber = accountConfig.getThreeMonthDebtMontsNumber();

    return Arrays.asList(
        Pair.of(sixMonthDebtMonthNumber, false),
        Pair.of(threeMonthDebtMonthNumber, true),
        Pair.of(sixMonthDebtMonthNumber, true),
        Pair.of(threeMonthDebtMonthNumber, false));
  }

  @CallMethod
  public List<Long> getDoubtfulCustomerPreviewList(
      Company company, Account doubtfulCustomerAccount) {
    List<Long> idList = new ArrayList<>();
    AccountConfig accountConfig = company.getAccountConfig();

    idList.addAll(
        this.getMoveLineIds(
            company, doubtfulCustomerAccount, accountConfig.getSixMonthDebtMonthNumber(), true));
    idList.addAll(
        this.getMoveLineIds(
            company, doubtfulCustomerAccount, accountConfig.getThreeMonthDebtMontsNumber(), true));
    idList.addAll(
        this.getMoveLineIds(
            company, doubtfulCustomerAccount, accountConfig.getSixMonthDebtMonthNumber(), false));
    idList.addAll(
        this.getMoveLineIds(
            company, doubtfulCustomerAccount, accountConfig.getThreeMonthDebtMontsNumber(), false));

    return idList.stream().distinct().collect(Collectors.toList());
  }
}
