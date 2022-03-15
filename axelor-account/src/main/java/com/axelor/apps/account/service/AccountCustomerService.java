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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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

public class AccountCustomerService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingSituationService accountingSituationService;
  protected AccountingSituationRepository accSituationRepo;
  protected AccountingSituationInitService accountingSituationInitService;
  protected AppBaseService appBaseService;

  @Inject
  public AccountCustomerService(
      AccountingSituationService accountingSituationService,
      AccountingSituationInitService accountingSituationInitService,
      AccountingSituationRepository accSituationRepo,
      AppBaseService appBaseService) {

    this.accountingSituationService = accountingSituationService;
    this.accountingSituationInitService = accountingSituationInitService;
    this.accSituationRepo = accSituationRepo;
    this.appBaseService = appBaseService;
  }

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
  public BigDecimal getBalance(Partner partner, Company company) {
    log.debug("Compute balance (Partner : {}, Company : {})", partner.getName(), company.getName());

    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM(COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "
                    + "FROM public.account_move_line AS ml  "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.debit > 0  GROUP BY moveline.id, moveline.amount_remaining) AS m1 ON (m1.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.credit > 0  GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "
                    + "LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "
                    + "WHERE ml.partner = ?1 AND move.company = ?2 AND move.ignore_in_accounting_ok IN ('false', null)"
                    + "AND account.use_for_partner_balance = 'true'"
                    + "AND (move.status_select = ?3 or move.status_select = ?4) AND ml.amount_remaining > 0 ")
            .setParameter(1, partner)
            .setParameter(2, company)
            .setParameter(3, MoveRepository.STATUS_VALIDATED)
            .setParameter(4, MoveRepository.STATUS_ACCOUNTED);

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
  public BigDecimal getBalanceDue(Partner partner, Company company, TradingName tradingName) {
    log.debug(
        "Compute balance due (Partner : {}, Company : {})", partner.getName(), company.getName());

    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM( COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "
                    + "FROM public.account_move_line AS ml  "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.debit > 0 "
                    + "AND ((moveline.due_date IS NULL AND moveline.date_val <= :todayDate) OR (moveline.due_date IS NOT NULL AND moveline.due_date <= :todayDate)) "
                    + "GROUP BY moveline.id, moveline.amount_remaining) AS m1 on (m1.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.credit > 0 "
                    + "GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "
                    + "LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "
                    + "WHERE ml.partner = :partner AND move.company = :company AND move.ignore_in_debt_recovery_ok IN ('false', null) "
                    + (tradingName != null ? "AND move.trading_name = :tradingName " : "")
                    + "AND move.ignore_in_accounting_ok IN ('false', null) AND account.use_for_partner_balance = 'true'"
                    + "AND (move.status_select = :statusValidated OR move.status_select = :statusDaybook) AND ml.amount_remaining > 0 ")
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
            .setParameter("statusValidated", MoveRepository.STATUS_VALIDATED)
            .setParameter("statusDaybook", MoveRepository.STATUS_ACCOUNTED);

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
   * solde des factures exigibles non bloquées en relance et dont « la date de facture » + « délai
   * d’acheminement(X) » <« date du jour » si la date de facture = date d'échéance de facture, sinon
   * pas de prise en compte du délai d'acheminement **
   */
  /**
   * solde des échéances rejetées qui ne sont pas bloqués
   * *****************************************************
   */
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

    // TODO: Replace native query to standard JPQL query
    Query query =
        JPA.em()
            .createNativeQuery(
                "SELECT SUM( COALESCE(m1.sum_remaining,0) - COALESCE(m2.sum_remaining,0) ) "
                    + "FROM public.account_move_line as ml  "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.debit > 0 AND (( moveline.date_val = moveline.due_date AND (moveline.due_date + :mailTransitTime ) < :todayDate ) "
                    + "OR (moveline.due_date IS NOT NULL AND moveline.date_val != moveline.due_date AND moveline.due_date < :todayDate)"
                    + "OR (moveline.due_date IS NULL AND moveline.date_val < :todayDate)) "
                    + "GROUP BY moveline.id, moveline.amount_remaining) AS m1 ON (m1.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN ( "
                    + "SELECT moveline.amount_remaining AS sum_remaining, moveline.id AS moveline_id "
                    + "FROM public.account_move_line AS moveline "
                    + "WHERE moveline.credit > 0 "
                    + "GROUP BY moveline.id, moveline.amount_remaining) AS m2 ON (m2.moveline_id = ml.id) "
                    + "LEFT OUTER JOIN public.account_account AS account ON (ml.account = account.id) "
                    + "LEFT OUTER JOIN public.account_move AS move ON (ml.move = move.id) "
                    + "LEFT JOIN public.account_invoice AS invoice ON (move.invoice = invoice.id) "
                    + "WHERE ml.partner = :partner AND move.company = :company AND move.ignore_in_debt_recovery_ok in ('false', null) "
                    + (tradingName != null ? "AND move.trading_name = :tradingName " : "")
                    + "AND move.ignore_in_accounting_ok IN ('false', null) AND account.use_for_partner_balance = 'true'"
                    + "AND (move.status_select = :statusValidated OR move.status_select = :statusDaybook) AND ml.amount_remaining > 0 "
                    + "AND (invoice IS NULL OR invoice.debt_recovery_blocking_ok = FALSE) ")
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
            .setParameter("statusValidated", MoveRepository.STATUS_VALIDATED)
            .setParameter("statusDaybook", MoveRepository.STATUS_ACCOUNTED);

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
          I18n.get(IExceptionMessage.ACCOUNT_CUSTOMER_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
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
          I18n.get(IExceptionMessage.ACCOUNT_CUSTOMER_2),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          company.getName());
    }

    return supplierAccount;
  }
}
