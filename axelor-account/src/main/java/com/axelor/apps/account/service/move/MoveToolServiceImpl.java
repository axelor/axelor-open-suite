/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveToolServiceImpl implements MoveToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineToolService moveLineToolService;
  protected MoveLineRepository moveLineRepository;
  protected AccountConfigService accountConfigService;
  protected PeriodServiceAccount periodServiceAccount;
  protected MoveRepository moveRepository;

  @Inject
  public MoveToolServiceImpl(
      MoveLineToolService moveLineToolService,
      MoveLineRepository moveLineRepository,
      AccountConfigService accountConfigService,
      PeriodServiceAccount periodServiceAccount,
      MoveRepository moveRepository) {
    this.moveLineToolService = moveLineToolService;
    this.moveLineRepository = moveLineRepository;
    this.accountConfigService = accountConfigService;
    this.periodServiceAccount = periodServiceAccount;
    this.moveRepository = moveRepository;
  }

  @Override
  public boolean isMinus(Invoice invoice) {
    // Si le montant est négatif, alors on doit inverser le signe du montant
    if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param invoice
   *     <p>OperationTypeSelect 1 : Supplier invoice 2 : Supplier refund 3 : Customer invoice 4 :
   *     Customer refund
   * @return
   * @throws AxelorException
   */
  @Override
  public boolean isDebitCustomer(Invoice invoice, boolean reverseDirectionForNegativeAmount)
      throws AxelorException {
    boolean isDebitCustomer;

    switch (invoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE:
        isDebitCustomer = false;
        break;
      case InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND:
        isDebitCustomer = true;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        isDebitCustomer = true;
        break;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        isDebitCustomer = false;
        break;

      default:
        throw new AxelorException(
            invoice,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.MOVE_1),
            invoice.getInvoiceId());
    }

    // Si le montant est négatif, alors on inverse le sens
    if (reverseDirectionForNegativeAmount && this.isMinus(invoice)) {
      isDebitCustomer = !isDebitCustomer;
    }

    return isDebitCustomer;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte
   * client) de la facture Récupération par boucle. A privilégié si les lignes d'écriture sont déjà
   * managées par JPA ou si le nombre de lignes d'écriture n'est pas important (< 100).
   *
   * @param invoice Une facture
   * @return
   * @throws AxelorException
   */
  @Override
  public MoveLine getInvoiceCustomerMoveLineByLoop(Invoice invoice) throws AxelorException {
    if (this.isDebitCustomer(invoice, true)) {
      return moveLineToolService.getDebitCustomerMoveLine(invoice);
    } else {
      return moveLineToolService.getCreditCustomerMoveLine(invoice);
    }
  }

  /**
   * Method that returns all move lines of an invoice payment that are not completely lettered
   *
   * @param invoicePayment Invoice payment
   * @return
   * @throws AxelorException
   */
  @Override
  public List<MoveLine> getInvoiceCustomerMoveLines(InvoicePayment invoicePayment) {
    List<MoveLine> moveLines = new ArrayList<>();
    if (!CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
        if (invoiceTermPayment.getInvoiceTerm().getMoveLine() != null
            && !moveLines.contains(invoiceTermPayment.getInvoiceTerm().getMoveLine())) {
          moveLines.add(invoiceTermPayment.getInvoiceTerm().getMoveLine());
        }
      }
    }
    return moveLines;
  }

  /**
   * Method that returns all the move lines of an invoice that are not completely lettered
   *
   * @param invoice Invoice
   * @return
   * @throws AxelorException
   */
  @Override
  public List<MoveLine> getInvoiceCustomerMoveLines(Invoice invoice) throws AxelorException {
    if (this.isDebitCustomer(invoice, true)) {
      return moveLineToolService.getDebitCustomerMoveLines(invoice);
    } else {
      return moveLineToolService.getCreditCustomerMoveLines(invoice);
    }
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (non complétement lettrée sur le compte
   * client) de la facture Récupération par requête. A privilégié si les lignes d'écritures ne sont
   * pas managées par JPA ou si le nombre d'écriture est très important (> 100)
   *
   * @param invoice Une facture
   * @return
   * @throws AxelorException
   */
  @Override
  public MoveLine getInvoiceCustomerMoveLineByQuery(Invoice invoice) throws AxelorException {

    if (this.isDebitCustomer(invoice, true)) {
      return moveLineRepository
          .all()
          .filter(
              "self.move = ?1 AND self.account = ?2 AND self.debit > 0 AND self.amountRemaining > 0",
              invoice.getMove(),
              invoice.getPartnerAccount())
          .fetchOne();
    } else {
      return moveLineRepository
          .all()
          .filter(
              "self.move = ?1 AND self.account = ?2 AND self.credit > 0 AND self.amountRemaining != 0",
              invoice.getMove(),
              invoice.getPartnerAccount())
          .fetchOne();
    }
  }

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
  @Override
  public MoveLine getCustomerMoveLineByLoop(Invoice invoice) throws AxelorException {
    if (invoice.getRejectMoveLine() != null
        && invoice.getRejectMoveLine().getAmountRemaining().abs().compareTo(BigDecimal.ZERO) > 0) {
      return invoice.getRejectMoveLine();
    } else {
      return this.getInvoiceCustomerMoveLineByLoop(invoice);
    }
  }

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
  @Override
  public MoveLine getCustomerMoveLineByQuery(Invoice invoice) throws AxelorException {
    if (invoice.getRejectMoveLine() != null
        && invoice.getRejectMoveLine().getAmountRemaining().abs().compareTo(BigDecimal.ZERO) > 0) {
      return invoice.getRejectMoveLine();
    } else {
      return this.getInvoiceCustomerMoveLineByQuery(invoice);
    }
  }

  @Override
  public Account getCustomerAccount(Partner partner, Company company, boolean isSupplierAccount)
      throws AxelorException {
    AccountingSituationService situationService = Beans.get(AccountingSituationService.class);
    return isSupplierAccount
        ? situationService.getSupplierAccount(partner, company)
        : situationService.getCustomerAccount(partner, company);
  }

  /**
   * Fonction permettant de savoir si toutes les lignes d'écritures utilise le même compte que celui
   * passé en paramètre
   *
   * @param moveLineList Une liste de lignes d'écritures
   * @param account Le compte que l'on souhaite tester
   * @return
   */
  @Override
  public boolean isSameAccount(List<MoveLine> moveLineList, Account account) {
    for (MoveLine moveLine : moveLineList) {
      if (!moveLine.getAccount().equals(account)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
   *
   * @param creditMoveLineList Une liste de ligne d'écriture au credit
   * @return
   */
  @Override
  public BigDecimal getTotalCreditAmount(List<MoveLine> creditMoveLineList) {
    BigDecimal totalCredit = BigDecimal.ZERO;
    for (MoveLine moveLine : creditMoveLineList) {
      totalCredit = totalCredit.add(moveLine.getAmountRemaining().abs());
    }
    return totalCredit;
  }

  /**
   * Fonction calculant le restant à utiliser total d'une liste de ligne d'écriture au credit
   *
   * @param creditMoveLineList Une liste de ligne d'écriture au credit
   * @return
   */
  @Override
  public BigDecimal getTotalDebitAmount(List<MoveLine> debitMoveLineList) {
    BigDecimal totalDebit = BigDecimal.ZERO;
    for (MoveLine moveLine : debitMoveLineList) {
      totalDebit = totalDebit.add(moveLine.getAmountRemaining().abs());
    }
    return totalDebit;
  }

  /**
   * Compute the balance amount : total debit - total credit
   *
   * @param moveLineList
   * @return
   */
  @Override
  public BigDecimal getBalanceAmount(List<MoveLine> moveLineList) {
    BigDecimal balance = BigDecimal.ZERO;

    if (moveLineList == null) {
      return balance;
    }

    for (MoveLine moveLine : moveLineList) {
      balance = balance.add(moveLine.getDebit());
      balance = balance.subtract(moveLine.getCredit());
    }
    return balance;
  }

  /**
   * Compute the balance amount in currency origin : total debit - total credit
   *
   * @param moveLineList
   * @return
   */
  @Override
  public BigDecimal getBalanceCurrencyAmount(List<MoveLine> moveLineList) {
    BigDecimal balance = BigDecimal.ZERO;

    if (moveLineList == null) {
      return balance;
    }

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getDebit().compareTo(moveLine.getCredit()) == 1) {
        balance = balance.add(moveLine.getCurrencyAmount().abs());
      } else {
        balance = balance.subtract(moveLine.getCurrencyAmount().abs());
      }
    }
    return balance;
  }

  @Override
  public MoveLine getOrignalInvoiceFromRefund(Invoice invoice) {

    Invoice originalInvoice = invoice.getOriginalInvoice();

    if (originalInvoice != null && originalInvoice.getMove() != null) {
      for (MoveLine moveLine : originalInvoice.getMove().getMoveLineList()) {
        if (moveLine.getAccount().getUseForPartnerBalance()
            && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAmountRemaining().abs().compareTo(BigDecimal.ZERO) > 0) {
          return moveLine;
        }
      }
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BigDecimal getInTaxTotalRemaining(Invoice invoice) throws AxelorException {
    BigDecimal inTaxTotalRemaining = BigDecimal.ZERO;

    log.debug("Update Remaining amount of invoice : {}", invoice.getInvoiceId());

    if (invoice != null) {

      boolean isMinus = this.isMinus(invoice);

      Beans.get(InvoiceRepository.class).save(invoice);

      List<MoveLine> moveLines = this.getInvoiceCustomerMoveLines(invoice);
      //			MoveLine moveLine2 = this.getCustomerMoveLineByQuery(invoice);

      if (!CollectionUtils.isEmpty(moveLines)) {
        for (MoveLine moveLine : moveLines) {
          inTaxTotalRemaining = inTaxTotalRemaining.add(moveLine.getAmountRemaining().abs());
        }

        if (isMinus) {
          inTaxTotalRemaining = inTaxTotalRemaining.negate();
        }
      }
    }
    return inTaxTotalRemaining;
  }

  /**
   * Methode permettant de récupérer la contrepartie d'une ligne d'écriture
   *
   * @param moveLine Une ligne d'écriture
   * @return
   */
  @Override
  public MoveLine getOppositeMoveLine(MoveLine moveLine) {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
      for (MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList()) {
        if (oppositeMoveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
          return oppositeMoveLine;
        }
      }
    }
    if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
      for (MoveLine oppositeMoveLine : moveLine.getMove().getMoveLineList()) {
        if (oppositeMoveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
          return oppositeMoveLine;
        }
      }
    }
    return null;
  }

  @Override
  public List<MoveLine> orderListByDate(List<MoveLine> list) {
    Collections.sort(
        list,
        new Comparator<MoveLine>() {

          @Override
          public int compare(MoveLine o1, MoveLine o2) {

            return o1.getDate().compareTo(o2.getDate());
          }
        });

    return list;
  }

  @Override
  public boolean isDebitMoveLine(MoveLine moveLine) {

    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<MoveLine> getToReconcileCreditMoveLines(Move move) {
    List<MoveLine> moveLineList = new ArrayList<>();

    if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
        || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAmountRemaining().abs().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAccount().getUseForPartnerBalance()) {
          moveLineList.add(moveLine);
        }
      }
    }

    return moveLineList;
  }

  @Override
  public MoveLine findMoveLineByAccount(Move move, Account account) throws AxelorException {
    return move.getMoveLineList().stream()
        .filter(moveLine -> moveLine.getAccount().equals(account))
        .findFirst()
        .orElseThrow(
            () ->
                new AxelorException(
                    move,
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get("%s account not found in move %s"),
                    account.getName(),
                    move.getReference()));
  }

  @Override
  public void setOriginOnMoveLineList(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine != null) {
        moveLine.setOrigin(move.getOrigin());
      }
    }
  }

  @Override
  public void setDescriptionOnMoveLineList(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine != null) {
        moveLine.setDescription(move.getDescription());
      }
    }
  }

  @Override
  public BigDecimal computeCurrencyAmountSign(BigDecimal currencyAmount, boolean isDebit) {
    if (isDebit) {
      return currencyAmount.abs();
    } else {
      return currencyAmount.compareTo(BigDecimal.ZERO) < 0
          ? currencyAmount
          : currencyAmount.negate();
    }
  }

  public boolean isTemporarilyClosurePeriodManage(Period period, Journal journal, User user)
      throws AxelorException {
    if (period != null) {
      if (period.getStatusSelect() == PeriodRepository.STATUS_OPENED
          && period.getCloseJournalsOnPeriod()
          && journal != null
          && !CollectionUtils.isEmpty(period.getClosedJournalSet())
          && period.getClosedJournalSet().contains(journal)) {
        return true;
      }
      if (period.getStatusSelect() == PeriodRepository.STATUS_TEMPORARILY_CLOSED) {
        if (journal != null
            && period.getKeepJournalsOpenOnPeriod()
            && !CollectionUtils.isEmpty(period.getOpenedJournalSet())
            && period.getOpenedJournalSet().contains(journal)) {
          return false;
        }
        if (period.getYear().getCompany() != null && user.getGroup() != null) {
          AccountConfig accountConfig =
              accountConfigService.getAccountConfig(period.getYear().getCompany());
          Set<Role> roleSet = accountConfig.getClosureAuthorizedRoleList();
          if (CollectionUtils.isEmpty(roleSet)) {
            return false;
          }
          for (Role role : roleSet) {
            if (user.getGroup().getRoles().contains(role) || user.getRoles().contains(role)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean getEditAuthorization(Move move) throws AxelorException {
    boolean result = false;
    Company company = move.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Period period = move.getPeriod();
    if (ObjectUtils.isEmpty(period)) {
      return true;
    }
    if (ObjectUtils.isEmpty(accountConfig)) {}

    return result;
  }

  @Override
  public boolean checkMoveLinesCutOffDates(Move move) {
    return move.getMoveLineList() == null
        || move.getMoveLineList().stream().allMatch(moveLineToolService::checkCutOffDates);
  }

  @Override
  public List<Move> findDaybookAndAccountingByYear(Set<Year> yearList) {
    List<Long> idList = new ArrayList<>();
    yearList.forEach(y -> idList.add(y.getId()));
    if (!CollectionUtils.isEmpty(idList)) {
      List<Integer> status =
          Arrays.asList(MoveRepository.STATUS_ACCOUNTED, MoveRepository.STATUS_DAYBOOK);
      return Query.of(Move.class)
          .filter("self.period.year.id in :years AND self.statusSelect IN :statusSelect")
          .bind("years", idList)
          .bind("statusSelect", status)
          .fetch();
    }
    return new ArrayList<Move>();
  }

  @Override
  public boolean isSimulatedMovePeriodClosed(Move move) {
    return move.getPeriod() != null
        && (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS
            || move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED)
        && move.getStatusSelect() == MoveRepository.STATUS_SIMULATED;
  }

  public List<MoveLine> getToReconcileDebitMoveLines(Move move) {
    List<MoveLine> moveLineList = new ArrayList<>();
    if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getAccount().getUseForPartnerBalance()) {
          moveLineList.add(moveLine);
        }
      }
    }
    return moveLineList;
  }

  @Override
  public void exceptionOnGenerateCounterpart(Move move) throws AxelorException {
    if (move.getPaymentMode() == null
        && (move.getJournal()
                .getJournalType()
                .getTechnicalTypeSelect()
                .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)
            || move.getJournal()
                .getJournalType()
                .getTechnicalTypeSelect()
                .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.EXCEPTION_GENERATE_COUNTERPART));
    }
  }

  @Override
  public List<Move> getMovesWithDuplicatedOrigin(Move move) {
    List<Move> moveList = null;
    StringBuilder query =
        new StringBuilder("self.origin = :origin AND self.period.year = :periodYear");
    Map<String, Object> params = new HashMap<>();

    if (!ObjectUtils.isEmpty(move.getOrigin()) && !ObjectUtils.isEmpty(move.getPeriod())) {
      params.put("origin", move.getOrigin());
      params.put("periodYear", move.getPeriod().getYear());

      if (move.getId() != null) {
        query.append(" AND self.id != :moveId");
        params.put("moveId", move.getId());
      }

      if (ObjectUtils.notEmpty(move.getPartner())) {
        query.append(" AND self.partner = :partner");
        params.put("partner", move.getPartner());
      }

      moveList = moveRepository.all().filter(query.toString()).bind(params).fetch();
    }
    return moveList;
  }

  @Override
  public boolean isMultiCurrency(Move move) {
    return move != null
        && move.getCurrency() != null
        && move.getCompany() != null
        && !Objects.equals(move.getCurrency(), move.getCompany().getCurrency());
  }

  @Override
  public List<Integer> getMoveStatusSelect(String moveStatusSelect, Company company) {
    List<Integer> statusList = new ArrayList<>(List.of(MoveRepository.STATUS_ACCOUNTED));

    if (ObjectUtils.isEmpty(moveStatusSelect) || company == null) {
      return statusList;
    }
    try {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      if (accountConfig.getAccountingDaybook()
          && moveStatusSelect.contains(String.valueOf(MoveRepository.STATUS_DAYBOOK))) {
        statusList.add(MoveRepository.STATUS_DAYBOOK);
      }
      if (accountConfig.getIsActivateSimulatedMove()
          && moveStatusSelect.contains(String.valueOf(MoveRepository.STATUS_SIMULATED))) {
        statusList.add(MoveRepository.STATUS_SIMULATED);
      }
    } catch (Exception e) {
      return statusList;
    }

    return statusList;
  }
}
