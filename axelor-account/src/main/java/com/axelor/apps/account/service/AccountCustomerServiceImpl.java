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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountCustomerServiceImpl implements AccountCustomerService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingSituationService accountingSituationService;
  protected AccountingSituationRepository accSituationRepo;
  protected AccountingSituationInitService accountingSituationInitService;
  protected AppBaseService appBaseService;

  @Inject
  public AccountCustomerServiceImpl(
      AccountingSituationService accountingSituationService,
      AccountingSituationInitService accountingSituationInitService,
      AccountingSituationRepository accSituationRepo,
      AppBaseService appBaseService) {

    this.accountingSituationService = accountingSituationService;
    this.accountingSituationInitService = accountingSituationInitService;
    this.accSituationRepo = accSituationRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  public AccountingSituationService getAccountingSituationService() {
    return this.accountingSituationService;
  }

  /**
   * Fonction permettant de calculer le solde total d'un tiers
   *
   * @param partner Un tiers
   * @param company Une société
   * @return Le solde total
   */
  @Override
  public BigDecimal getBalance(Partner partner, Company company) {
    log.debug("Compute balance (Partner : {}, Company : {})", partner.getName(), company.getName());

    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM(CASE WHEN ml.debit > 0 THEN ml.amount_remaining ELSE ml.amount_remaining * -1 END) "
                    + "FROM public.account_move_line AS ml  "
                    + "LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "
                    + "LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "
                    + "WHERE ml.partner = :partner AND move.company = :company "
                    + "AND move.ignore_in_accounting_ok IN ('false', null) AND account.use_for_partner_balance IS TRUE "
                    + "AND move.status_select IN (:statusValidated, :statusDaybook) AND ml.amount_remaining > 0 ")
            .setParameter("partner", partner)
            .setParameter("company", company)
            .setParameter("statusValidated", MoveRepository.STATUS_ACCOUNTED)
            .setParameter("statusDaybook", MoveRepository.STATUS_DAYBOOK);

    BigDecimal balance = (BigDecimal) query.getSingleResult();

    if (balance == null) {
      balance = BigDecimal.ZERO;
    }

    log.debug("Balance : {}", balance);

    return balance;
  }

  /**
   * Compute the balance due for a specific (company, trading name) combination.
   *
   * <p>Computation of the balance due of a partner : Total amount of the invoices and expired
   * deadlines (date of the day >= date of the deadline)
   *
   * @param partner A Partner
   * @param company A Company
   * @param tradingName (Optional) A trading name of the company
   * @return The balance due of this trading name, in the scope of the activities of this company
   */
  @Override
  public BigDecimal getBalanceDue(Partner partner, Company company, TradingName tradingName) {
    log.debug(
        "Compute balance due (Partner : {}, Company : {})", partner.getName(), company.getName());

    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM(CASE WHEN ml.debit > 0 THEN term.amount_remaining ELSE term.amount_remaining * -1 END) "
                    + "FROM public.account_invoice_term AS term "
                    + "JOIN public.account_move_line AS ml ON term.move_line = ml.id "
                    + "LEFT OUTER JOIN public.account_account AS account ON ml.account = account.id "
                    + "LEFT OUTER JOIN public.account_move AS move ON ml.move = move.id "
                    + "WHERE term.due_date IS NOT NULL AND term.due_date <= :todayDate "
                    + "AND ml.partner = :partner AND move.company = :company "
                    + (tradingName != null ? "AND move.trading_name = :tradingName " : "")
                    + "AND move.ignore_in_accounting_ok IN ('false', null) AND account.use_for_partner_balance IS TRUE "
                    + "AND move.status_select IN (:statusValidated, :statusDaybook) AND ml.amount_remaining > 0 ")
            .setParameter(
                "todayDate",
                Date.from(
                    appBaseService
                        .getTodayDate(company)
                        .atStartOfDay()
                        .atZone(ZoneOffset.UTC)
                        .toInstant()),
                TemporalType.DATE)
            .setParameter("partner", partner)
            .setParameter("company", company)
            .setParameter("statusValidated", MoveRepository.STATUS_ACCOUNTED)
            .setParameter("statusDaybook", MoveRepository.STATUS_DAYBOOK);

    if (tradingName != null) {
      query = query.setParameter("tradingName", tradingName);
    }
    BigDecimal balance = (BigDecimal) query.getSingleResult();

    if (balance == null) {
      balance = BigDecimal.ZERO;
    }

    log.debug("Balance due : {}", balance);

    return balance;
  }

  /**
   * **************************************** 2. Calcul du solde exigible (relançable) du tiers
   * *****************************************
   */
  /**
   * solde des factures exigibles non bloquées en relance et dont « la date de facture » + « délai
   * d’acheminement(X)» + « date du jour » si la date de facture = date d'échéance de facture, sinon
   * pas de prise en compte du délai d'acheminement **
   */
  /**
   * solde des échéances rejetées qui ne sont pas bloqués
   * *****************************************************
   */
  @Override
  public BigDecimal getBalanceDueDebtRecovery(
      Partner partner, Company company, TradingName tradingName) {
    log.debug(
        "Compute balance due debt recovery (Partner : {}, Company : {}"
            + (tradingName != null ? ", Trading name : {})" : ")"),
        partner.getName(),
        company.getName(),
        tradingName != null ? tradingName.getName() : null);

    int mailTransitTime = 0;

    AccountConfig accountConfig = company.getAccountConfig();

    if (accountConfig != null) {
      mailTransitTime = accountConfig.getMailTransitTime();
    }

    // TODO: Replace native query to standard JPQL query
    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM( COALESCE(t1.term_amountRemaining,0) - COALESCE(t2.term_amountRemaining,0) ) "
                    + "FROM public.account_move_line as ml  "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.debit > 0 "
                    + "GROUP BY moveline.id, moveline.amount_remaining "
                    + ") AS m1 ON (m1.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.credit > 0 "
                    + "GROUP BY moveline.id, moveline.amount_remaining "
                    + ") AS m2 ON (m2.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT term.amount_remaining as term_amountRemaining, term.move_line as term_ml "
                    + "FROM public.account_invoice_term AS term "
                    + "WHERE (term.due_date IS NOT NULL AND term.due_date <= :todayDate)"
                    + "GROUP BY term.move_line, term.amount_remaining "
                    + ") AS t1 ON (t1.term_ml = m1.moveline_id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT term.amount_remaining as term_amountRemaining, term.move_line as term_ml "
                    + "FROM public.account_invoice_term AS term "
                    + "JOIN public.account_move_line AS TermMoveLine ON (TermMoveLine.id = term.move_line) "
                    + "JOIN public.account_move AS TermMove ON (TermMove.id = TermMoveLine.move) "
                    + "WHERE (TermMove.date_val IS NOT NULL AND (TermMove.date_val + :mailTransitTime ) <= :todayDate ) "
                    + "GROUP BY term.move_line, term.amount_remaining "
                    + ") AS t2 ON (t2.term_ml = m2.moveline_id) "
                    + "LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "
                    + "LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "
                    + "LEFT JOIN public.account_invoice AS invoice ON (move.invoice = invoice.id) "
                    + "WHERE ml.partner = :partner AND move.company = :company "
                    + (tradingName != null ? "AND move.trading_name = :tradingName " : "")
                    + "AND move.ignore_in_accounting_ok IN ('false', null) AND account.use_for_partner_balance = 'true'"
                    + "AND (move.status_select = :statusValidated OR move.status_select = :statusDaybook) AND ml.amount_remaining > 0 "
                    + "AND (invoice IS NULL OR invoice.debt_recovery_blocking_ok IN ('false', null)) ")
            .setParameter("mailTransitTime", mailTransitTime)
            .setParameter(
                "todayDate",
                Date.from(
                    appBaseService
                        .getTodayDate(company)
                        .atStartOfDay()
                        .atZone(ZoneOffset.UTC)
                        .toInstant()),
                TemporalType.DATE)
            .setParameter("partner", partner)
            .setParameter("company", company)
            .setParameter("statusValidated", MoveRepository.STATUS_ACCOUNTED)
            .setParameter("statusDaybook", MoveRepository.STATUS_DAYBOOK);

    if (tradingName != null) {
      query = query.setParameter("tradingName", tradingName);
    }
    BigDecimal balance = (BigDecimal) query.getSingleResult();

    if (balance == null) {
      balance = BigDecimal.ZERO;
    }

    log.debug("Balance due debt recovery : {}", balance);

    return balance;
  }

  /**
   * Méthode permettant de récupérer l'ensemble des lignes d'écriture pour une société et un tiers
   *
   * @param partner Un tiers
   * @param company Une société
   * @return
   */
  @Override
  public List<? extends MoveLine> getMoveLine(Partner partner, Company company) {

    return Beans.get(MoveLineRepository.class)
        .all()
        .filter("self.partner = ?1 AND self.move.company = ?2", partner, company)
        .fetch();
  }

  /**
   * Procédure mettant à jour les soldes du compte client des tiers pour une société
   *
   * @param partnerList Une liste de tiers à mettre à jour
   * @param company Une société
   */
  @Override
  public void updatePartnerAccountingSituation(
      List<Partner> partnerList,
      Company company,
      boolean updateCustAccount,
      boolean updateDueCustAccount,
      boolean updateDueDebtRecoveryCustAccount)
      throws AxelorException {
    for (Partner partner : partnerList) {
      AccountingSituation accountingSituation =
          accountingSituationService.getAccountingSituation(partner, company);
      if (accountingSituation == null) {
        accountingSituation =
            accountingSituationInitService.createAccountingSituation(partner, company);
      }
      if (accountingSituation != null) {
        this.updateAccountingSituationCustomerAccount(
            accountingSituation,
            updateCustAccount,
            updateDueCustAccount,
            updateDueDebtRecoveryCustAccount);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void flagPartners(List<Partner> partnerList, Company company) throws AxelorException {
    for (Partner partner : partnerList) {
      AccountingSituation accountingSituation =
          accountingSituationService.getAccountingSituation(partner, company);
      if (accountingSituation == null) {
        accountingSituation =
            accountingSituationInitService.createAccountingSituation(partner, company);
      }
      if (accountingSituation != null) {
        accountingSituation.setCustAccountMustBeUpdateOk(true);
        accSituationRepo.save(accountingSituation);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public AccountingSituation updateAccountingSituationCustomerAccount(
      AccountingSituation accountingSituation,
      boolean updateCustAccount,
      boolean updateDueCustAccount,
      boolean updateDueDebtRecoveryCustAccount)
      throws AxelorException {
    Partner partner = accountingSituation.getPartner();
    Company company = accountingSituation.getCompany();

    log.debug(
        "Update customer account (Partner : {}, Company : {}, Update balance : {}, balance due : {}, balance due debt recovery : {})",
        partner.getName(),
        company.getName(),
        updateCustAccount,
        updateDueCustAccount,
        updateDueDebtRecoveryCustAccount);

    if (updateCustAccount) {
      accountingSituation.setBalanceCustAccount(this.getBalance(partner, company));
    }
    if (updateDueCustAccount) {
      accountingSituation.setBalanceDueCustAccount(this.getBalanceDue(partner, company, null));
    }
    if (updateDueDebtRecoveryCustAccount) {
      accountingSituation.setBalanceDueDebtRecoveryCustAccount(
          this.getBalanceDueDebtRecovery(partner, company, null));
    }
    accountingSituation.setCustAccountMustBeUpdateOk(false);
    accSituationRepo.save(accountingSituation);

    return accountingSituation;
  }

  @Override
  public Account getPartnerAccount(Partner partner, Company company, boolean isSupplierInvoice)
      throws AxelorException {
    return isSupplierInvoice
        ? getSupplierAccount(partner, company)
        : getCustomerAccount(partner, company);
  }

  protected Account getCustomerAccount(Partner partner, Company company) throws AxelorException {
    Account customerAccount = accountingSituationService.getCustomerAccount(partner, company);

    if (customerAccount == null) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.ACCOUNT_CUSTOMER_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return customerAccount;
  }

  protected Account getSupplierAccount(Partner partner, Company company) throws AxelorException {
    Account supplierAccount = accountingSituationService.getSupplierAccount(partner, company);

    if (supplierAccount == null) {
      throw new AxelorException(
          partner,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.ACCOUNT_CUSTOMER_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    return supplierAccount;
  }
}
