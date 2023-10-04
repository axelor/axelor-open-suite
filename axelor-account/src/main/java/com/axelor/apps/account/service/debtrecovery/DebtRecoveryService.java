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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.DebtRecoveryMethod;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.TradingNameRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.db.repo.MultiRelatedRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

public class DebtRecoveryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected DebtRecoverySessionService debtRecoverySessionService;
  protected DebtRecoveryActionService debtRecoveryActionService;
  protected AccountCustomerService accountCustomerService;
  protected MoveLineRepository moveLineRepo;
  protected PaymentScheduleLineRepository paymentScheduleLineRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected AccountConfigService accountConfigService;
  protected DebtRecoveryRepository debtRecoveryRepo;
  protected CompanyRepository companyRepo;
  protected TradingNameRepository tradingNameRepo;

  protected AppAccountService appAccountService;
  protected MessageRepository messageRepo;

  @Inject
  public DebtRecoveryService(
      DebtRecoverySessionService debtRecoverySessionService,
      DebtRecoveryActionService debtRecoveryActionService,
      AccountCustomerService accountCustomerService,
      MoveLineRepository moveLineRepo,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      AccountConfigService accountConfigService,
      DebtRecoveryRepository debtRecoveryRepo,
      CompanyRepository companyRepo,
      TradingNameRepository tradingNameRepo,
      AppAccountService appAccountService,
      MessageRepository messageRepo,
      InvoiceTermRepository invoiceTermRepo) {

    this.debtRecoverySessionService = debtRecoverySessionService;
    this.debtRecoveryActionService = debtRecoveryActionService;
    this.accountCustomerService = accountCustomerService;
    this.moveLineRepo = moveLineRepo;
    this.paymentScheduleLineRepo = paymentScheduleLineRepo;
    this.accountConfigService = accountConfigService;
    this.debtRecoveryRepo = debtRecoveryRepo;
    this.companyRepo = companyRepo;
    this.tradingNameRepo = tradingNameRepo;
    this.appAccountService = appAccountService;
    this.messageRepo = messageRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  public void testCompanyField(Company company) throws AxelorException {

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    accountConfigService.getDebtRecoveryConfigLineList(accountConfig);
  }

  /**
   * Fonction qui récupère la plus ancienne date d'échéance d'une liste de lignes d'écriture
   *
   * @param moveLineList Une liste de lignes d'écriture
   * @return la plus ancienne date d'échéance
   */
  public LocalDate getOldDateMoveLine(List<MoveLine> moveLineList) {
    LocalDate minMoveLineDate;

    if (!moveLineList.isEmpty()) {
      minMoveLineDate =
          appAccountService.getTodayDate(
              moveLineList.get(0).getMove() != null
                  ? moveLineList.get(0).getMove().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
      for (MoveLine moveLine : moveLineList) {
        LocalDate moveLineDueDate = moveLine.getDueDate();
        if (moveLineDueDate != null && minMoveLineDate.isAfter(moveLineDueDate)) {
          minMoveLineDate = moveLine.getDueDate();
        }
      }
    } else {
      minMoveLineDate = null;
    }
    return minMoveLineDate;
  }

  /**
   * Fonction qui récupére la plus récente date entre deux date
   *
   * @param date1 Une date
   * @param date2 Une date
   * @return minDate La plus ancienne date
   */
  public LocalDate getLastDate(LocalDate date1, LocalDate date2) {
    LocalDate minDate;
    if (date1 != null && date2 != null) {
      if (date1.isAfter(date2)) {
        minDate = date1;
      } else {
        minDate = date2;
      }
    } else if (date1 != null) {
      minDate = date1;
    } else if (date2 != null) {
      minDate = date2;
    } else {
      minDate = null;
    }
    return minDate;
  }

  /**
   * Fonction qui permet de récupérer la date de relance la plus récente
   *
   * @param debtRecovery Une relance
   * @return La date de relance la plus récente
   */
  public LocalDate getLastDateDebtRecovery(DebtRecovery debtRecovery) {
    return debtRecovery.getDebtRecoveryDate();
  }

  /**
   * Fonction qui détermine la date de référence
   *
   * @param debtRecovery Une relance
   * @return La date de référence
   */
  public LocalDate getReferenceDate(DebtRecovery debtRecovery) {
    AccountingSituation accountingSituation = this.getAccountingSituation(debtRecovery);
    List<MoveLine> moveLineList =
        this.getMoveLineDebtRecovery(
            accountingSituation.getPartner(),
            accountingSituation.getCompany(),
            debtRecovery.getTradingName());

    // Date la plus ancienne des lignes d'écriture
    LocalDate minMoveLineDate = getOldDateMoveLine(moveLineList);
    log.debug("minMoveLineDate : {}", minMoveLineDate);

    // 2: Date la plus récente des relances
    LocalDate debtRecoveryLastDate = getLastDateDebtRecovery(debtRecovery);
    log.debug("debtRecoveryLastDate : {}", debtRecoveryLastDate);

    // Date de référence : Date la plus récente des deux ensembles (1 et 2)
    LocalDate debtRecoveryRefDate = getLastDate(minMoveLineDate, debtRecoveryLastDate);
    log.debug("debtRecoveryRefDate : {}", debtRecoveryRefDate);

    return debtRecoveryRefDate;
  }

  /**
   * Returns a list of recoverable move lines of a partner in the scope of the activity of a company
   *
   * @param partner The partner to be concerned by the move lines
   * @param company The company to be concerned by the move lines
   * @param tradingName (Optional) The trading name to be concerned by the move lines
   * @return A list of recoverable move lines
   */
  @SuppressWarnings("unchecked")
  public List<MoveLine> getMoveLineDebtRecovery(
      Partner partner, Company company, TradingName tradingName) {
    List<Long> idList = new ArrayList<>();

    LocalDate todayDate = appAccountService.getTodayDate(company);
    int mailTransitTime = company.getAccountConfig().getMailTransitTime();

    addInvoiceTermsFromInvoice(partner, company, tradingName, todayDate, mailTransitTime, idList);
    addInvoiceTermsFromMoveLine(partner, company, tradingName, todayDate, idList);

    return moveLineRepo.findByIds(idList.stream().distinct().collect(Collectors.toList()));
  }

  public List<Invoice> getInvoiceListFromInvoiceTerm(List<InvoiceTerm> invoiceTermList) {
    List<Invoice> invoiceList = new ArrayList<Invoice>();
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (invoiceTerm.getInvoice() != null
          && !invoiceTerm.getInvoice().getDebtRecoveryBlockingOk()) {
        invoiceList.add(invoiceTerm.getInvoice());
      }
    }
    return invoiceList;
  }

  protected void addInvoiceTermsFromInvoice(
      Partner partner,
      Company company,
      TradingName tradingName,
      LocalDate todayDate,
      int mailTransitTime,
      List<Long> idList) {
    javax.persistence.Query moveLineWithoutInvoiceTermQuery =
        JPA.em()
            .createQuery(computeQuery(tradingName, false))
            .setParameter("partner", partner)
            .setParameter("company", company)
            .setParameter("todayDate", todayDate)
            .setParameter("cancelStatus", MoveRepository.STATUS_CANCELED)
            .setParameter("todayDateMinusTransitTime", todayDate.minusDays(mailTransitTime));

    moveLineWithoutInvoiceTermQuery =
        addTradingNameBinding(tradingName, moveLineWithoutInvoiceTermQuery);

    idList.addAll(moveLineWithoutInvoiceTermQuery.getResultList());
  }

  protected void addInvoiceTermsFromMoveLine(
      Partner partner,
      Company company,
      TradingName tradingName,
      LocalDate todayDate,
      List<Long> idList) {
    if (appAccountService.getAppAccount().getAllowMultiInvoiceTerms()) {

      javax.persistence.Query moveLineWithInvoiceTermQuery =
          JPA.em()
              .createQuery(computeQuery(tradingName, true))
              .setParameter("partner", partner)
              .setParameter("company", company)
              .setParameter("todayDate", todayDate);

      moveLineWithInvoiceTermQuery =
          addTradingNameBinding(tradingName, moveLineWithInvoiceTermQuery);

      idList.addAll(moveLineWithInvoiceTermQuery.getResultList());
    }
  }

  protected javax.persistence.Query addTradingNameBinding(
      TradingName tradingName, javax.persistence.Query moveLineWithoutInvoiceTermQuery) {
    if (tradingName != null) {
      moveLineWithoutInvoiceTermQuery =
          moveLineWithoutInvoiceTermQuery.setParameter("tradingName", tradingName);
    }
    return moveLineWithoutInvoiceTermQuery;
  }

  protected String computeQuery(TradingName tradingName, boolean allowMultiInvoiceTerms) {
    StringBuilder query = new StringBuilder();
    computeQueryJoins(allowMultiInvoiceTerms, query);
    computeQueryConditions(tradingName, allowMultiInvoiceTerms, query);

    return query.toString();
  }

  protected void computeQueryConditions(
      TradingName tradingName, boolean allowMultiInvoiceTerms, StringBuilder query) {
    query.append("WHERE ml.partner = :partner ");
    query.append("AND move.id IS NOT NULL ");
    query.append("AND move.company = :company ");
    query.append("AND ml.debit > 0 ");
    query.append("AND ml.dueDate IS NOT NULL ");
    query.append("AND (ml.dueDate <= :todayDate) ");
    query.append("AND account.id IS NOT NULL ");
    query.append("AND account.useForPartnerBalance IS TRUE ");
    query.append("AND ml.amountRemaining > 0 ");
    query.append("AND move.ignoreInDebtRecoveryOk IS FALSE ");

    if (tradingName != null) {
      query.append("AND move.tradingName = :tradingName ");
    }

    if (!allowMultiInvoiceTerms) {
      query.append("AND move.statusSelect != :cancelStatus ");
      query.append("AND invoice.id IS NOT NULL ");
      query.append("AND invoice.debtRecoveryBlockingOk IS FALSE ");
      query.append("AND invoice.schedulePaymentOk IS FALSE ");
      query.append("AND invoice.invoiceDate < :todayDateMinusTransitTime");
    } else {
      query.append("AND ((invoice.id IS NULL OR invoiceterm.id IS NOT NULL) ");
      query.append("AND (ml.paymentScheduleLine IS NOT NULL OR invoiceterm.id IS NOT NULL))");
    }
  }

  protected void computeQueryJoins(boolean allowMultiInvoiceTerms, StringBuilder query) {
    query.append("SELECT DISTINCT ml.id ");
    query.append("FROM MoveLine ml ");
    query.append("LEFT JOIN Move move ON (ml.move = move.id) ");
    query.append("LEFT JOIN Account account ON (ml.account = account.id) ");
    query.append("LEFT JOIN Invoice invoice ON (move.invoice = invoice.id) ");

    if (allowMultiInvoiceTerms) {
      query.append("LEFT JOIN InvoiceTerm invoiceterm ON (invoiceterm.moveLine = ml.id) ");
    }
  }

  /**
   * Recovers all move lines for a specific partner, in the scope of the activities of a company,
   * and optionally, for a specific trading name.
   *
   * @param partner A partner to be concerned by the move lines
   * @param company A company to be concerned by the move lines
   * @param tradingName (Optional) A trading name to be concerned by the move lines
   * @return all corresponding move lines as a List
   * @throws AxelorException
   */
  public List<InvoiceTerm> getInvoiceTerms(
      Partner partner, Company company, TradingName tradingName) throws AxelorException {

    int mailTransitTime = accountConfigService.getAccountConfig(company).getMailTransitTime();

    Query<InvoiceTerm> query =
        invoiceTermRepo
            .all()
            .filter(
                "(self.paymentSession IS NULL OR self.paymentSession.statusSelect != :paymentSessionStatus) "
                    + " and self.amountRemaining > 0 "
                    + " and self.isPaid IS FALSE "
                    + " and self.debtRecoveryBlockingOk IS FALSE "
                    + " and self.moveLine IS NOT NULL "
                    + " and self.moveLine.move.company = :company "
                    + " and self.moveLine.partner = :partner "
                    + (tradingName != null
                        ? " and self.moveLine.move.tradingName = :tradingName"
                        : ""))
            .bind("paymentSessionStatus", PaymentSessionRepository.STATUS_ONGOING)
            .bind("company", company)
            .bind("partner", partner);

    if (tradingName != null) {
      query.bind("tradingName", tradingName);
    }

    List<InvoiceTerm> invoiceTermList = query.fetch();
    List<InvoiceTerm> invoiceTermWithDateCheck = Lists.newArrayList();
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (invoiceTerm.getDueDate() != null
          && invoiceTerm.getMoveLine() != null
          && invoiceTerm.getMoveLine().getMove() != null
          && invoiceTerm.getMoveLine().getMove().getDate() != null
          && invoiceTerm
              .getMoveLine()
              .getMove()
              .getDate()
              .plusDays(mailTransitTime)
              .isBefore(appAccountService.getTodayDate(company))
          && (appAccountService.getTodayDate(company).isAfter(invoiceTerm.getDueDate())
              || appAccountService.getTodayDate(company).isEqual(invoiceTerm.getDueDate()))) {
        invoiceTermWithDateCheck.add(invoiceTerm);
      }
    }

    return invoiceTermWithDateCheck;
  }

  public DebtRecovery getDebtRecovery(Partner partner, Company company) throws AxelorException {

    AccountingSituationRepository accSituationRepo = Beans.get(AccountingSituationRepository.class);
    AccountingSituation accountingSituation =
        accSituationRepo
            .all()
            .filter("self.partner = ?1 and self.company = ?2", partner, company)
            .fetchOne();

    if (accountingSituation == null) {
      throw new AxelorException(
          accountingSituation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "%s :\n"
              + I18n.get("Partner")
              + " %s, "
              + I18n.get("Company")
              + " %s : "
              + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          partner.getName(),
          company.getName());
    }

    return accountingSituation.getDebtRecovery();
  }

  /**
   * Find an existing debtRecovery object for a corresponding partner, company and optionally,
   * trading name.
   *
   * @param partner A partner
   * @param company A company
   * @param tradingName (Optional) A trading name
   * @return The corresponding DebtRecovery object, storing information regarding the current debt
   *     recovery situation of the partner for this company & trading name
   * @throws AxelorException
   */
  public DebtRecovery getDebtRecovery(Partner partner, Company company, TradingName tradingName)
      throws AxelorException {

    AccountingSituationRepository accSituationRepo = Beans.get(AccountingSituationRepository.class);
    AccountingSituation accountingSituation =
        accSituationRepo
            .all()
            .filter("self.partner = ?1 and self.company = ?2", partner, company)
            .fetchOne();

    if (accountingSituation == null) {
      throw new AxelorException(
          accountingSituation,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "%s :\n"
              + I18n.get("Partner")
              + " %s, "
              + I18n.get("Company")
              + " %s : "
              + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          partner.getName(),
          company.getName());
    }
    if (tradingName == null) {
      return accountingSituation.getDebtRecovery();
    } else {
      if (accountingSituation.getTradingNameDebtRecoveryList() != null
          && !accountingSituation.getTradingNameDebtRecoveryList().isEmpty()) {
        for (DebtRecovery debtRecovery : accountingSituation.getTradingNameDebtRecoveryList()) {
          if (tradingName.equals(debtRecovery.getTradingName())) {
            return debtRecovery;
          }
        }
      }
    }

    return null; // if no debtRecovery has been found for the specified tradingName
  }

  @Transactional
  public DebtRecovery createDebtRecovery(
      AccountingSituation accountingSituation, TradingName tradingName) {
    DebtRecovery debtRecovery = new DebtRecovery();
    if (tradingName != null) {
      debtRecovery.setTradingNameAccountingSituation(accountingSituation);
      if (accountingSituation.getTradingNameDebtRecoveryList() != null) {
        accountingSituation.getTradingNameDebtRecoveryList().add(debtRecovery);
      } else {
        List<DebtRecovery> tradingNameDebtRecoveryList = new ArrayList<DebtRecovery>();
        tradingNameDebtRecoveryList.add(debtRecovery);
        accountingSituation.setTradingNameDebtRecoveryList(tradingNameDebtRecoveryList);
      }
    } else {
      debtRecovery.setAccountingSituation(accountingSituation);
      accountingSituation.setDebtRecovery(debtRecovery);
    }
    debtRecoveryRepo.save(debtRecovery);
    return debtRecovery;
  }

  /**
   * Handle the debt recovery process for a partner and company. Can optionally specify a trading
   * name.
   *
   * @param partner The partner that has debts to be recovered
   * @param company The company for which to recover the debts
   * @param tradingName (optional) A trading name of the company for which to recover the debts
   * @throws AxelorException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  @Transactional(rollbackOn = {Exception.class})
  public boolean debtRecoveryGenerate(Partner partner, Company company, TradingName tradingName)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, IOException, JSONException {
    boolean remindedOk = false;

    DebtRecovery debtRecovery =
        this.getDebtRecovery(
            partner, company, tradingName); // getDebtRecovery if one already exists

    BigDecimal balanceDue = accountCustomerService.getBalanceDue(partner, company, tradingName);

    if (balanceDue.compareTo(BigDecimal.ZERO) > 0) {

      log.debug("balanceDue : {} ", balanceDue);

      BigDecimal balanceDueDebtRecovery =
          accountCustomerService.getBalanceDueDebtRecovery(partner, company, tradingName);

      if (balanceDueDebtRecovery.compareTo(BigDecimal.ZERO) > 0) {
        log.debug("balanceDueDebtRecovery : {} ", balanceDueDebtRecovery);

        remindedOk = true;

        if (debtRecovery == null) {
          AccountingSituationRepository accSituationRepo =
              Beans.get(AccountingSituationRepository.class);
          AccountingSituation accountingSituation =
              accSituationRepo
                  .all()
                  .filter("self.partner = ?1 and self.company = ?2", partner, company)
                  .fetchOne();
          debtRecovery = this.createDebtRecovery(accountingSituation, tradingName);
        }

        debtRecovery.setCompany(companyRepo.find(company.getId()));
        if (tradingName != null)
          debtRecovery.setTradingName(tradingNameRepo.find(tradingName.getId()));
        debtRecovery.setCurrency(partner.getCurrency());
        debtRecovery.setBalanceDue(balanceDue);

        List<InvoiceTerm> invoiceTermList = this.getInvoiceTerms(partner, company, tradingName);
        this.updateInvoiceTermDebtRecovery(debtRecovery, invoiceTermList);
        this.updateInvoiceDebtRecovery(
            debtRecovery, this.getInvoiceListFromInvoiceTerm(invoiceTermList));

        debtRecovery.setBalanceDueDebtRecovery(balanceDueDebtRecovery);

        Integer levelDebtRecovery = -1;
        if (debtRecovery.getDebtRecoveryMethodLine() != null) {
          levelDebtRecovery = debtRecovery.getDebtRecoveryMethodLine().getSequence();
        }

        LocalDate oldReferenceDate = debtRecovery.getReferenceDate();
        LocalDate referenceDate = this.getReferenceDate(debtRecovery);

        boolean isReset = this.isInvoiceSetNew(debtRecovery, oldReferenceDate);

        if (referenceDate != null) {
          log.debug("reference date : {} ", referenceDate);
          debtRecovery.setReferenceDate(referenceDate);
        } else {
          throw new AxelorException(
              debtRecovery,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              "%s :\n"
                          + I18n.get("Partner")
                          + " %s, "
                          + I18n.get("Company")
                          + " %s : "
                          + tradingName
                      != null
                  ? I18n.get("Trading name") + " %s : "
                  : "" + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_2),
              I18n.get(BaseExceptionMessage.EXCEPTION),
              partner.getName(),
              company.getName());
        }
        if (debtRecovery.getDebtRecoveryMethod() == null) {
          fetchDebtRecoveryMethod(partner, company, tradingName, debtRecovery);
        }
        if (isReset) {
          debtRecoverySessionService.reset(debtRecovery);
        }
        debtRecoverySessionService.debtRecoverySession(debtRecovery);
        if (debtRecovery.getWaitDebtRecoveryMethodLine() == null) {
          // Si le niveau de relance a évolué
          if (debtRecovery.getDebtRecoveryMethodLine() != null
              && !debtRecovery
                  .getDebtRecoveryMethodLine()
                  .getSequence()
                  .equals(levelDebtRecovery)) {
            debtRecoveryActionService.runAction(debtRecovery);

            DebtRecoveryHistory debtRecoveryHistory =
                debtRecoveryActionService.getDebtRecoveryHistory(debtRecovery);

            if (CollectionUtils.isEmpty(
                Beans.get(MultiRelatedRepository.class)
                    .all()
                    .filter(
                        "self.relatedToSelect = :relatedToSelect AND self.relatedToSelectId = :relatedToSelectId AND self.message IS NOT NULL")
                    .bind("relatedToSelectId", Math.toIntExact(debtRecoveryHistory.getId()))
                    .bind("relatedToSelect", DebtRecoveryHistory.class.getCanonicalName())
                    .fetch())) {
              debtRecoveryActionService.runMessage(debtRecovery);
            }
          }
        } else {
          log.debug(
              "Partner {}, Company {} - Reminder level : on hold",
              partner.getName(),
              company.getName());
          TraceBackService.trace(
              new AxelorException(
                  debtRecovery,
                  TraceBackRepository.CATEGORY_INCONSISTENCY,
                  "%s :\n"
                      + I18n.get("Partner")
                      + " %s, "
                      + I18n.get("Company")
                      + " %s : "
                      + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_4),
                  I18n.get(BaseExceptionMessage.EXCEPTION),
                  partner.getName(),
                  company.getName()));
        }
      } else {
        debtRecoverySessionService.debtRecoveryInitialization(debtRecovery);
      }
    } else {
      debtRecoverySessionService.debtRecoveryInitialization(debtRecovery);
    }
    return remindedOk;
  }

  protected void fetchDebtRecoveryMethod(
      Partner partner, Company company, TradingName tradingName, DebtRecovery debtRecovery)
      throws AxelorException {
    DebtRecoveryMethod debtRecoveryMethod =
        debtRecoverySessionService.getDebtRecoveryMethod(debtRecovery);
    if (debtRecoveryMethod != null) {
      debtRecovery.setDebtRecoveryMethod(debtRecoveryMethod);
    } else {
      throw new AxelorException(
          debtRecovery,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "%s :\n"
              + I18n.get("Partner")
              + " %s, "
              + I18n.get("Company")
              + " %s : "
              + (tradingName != null ? I18n.get("Trading name") + " %s : " : "")
              + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          partner.getName(),
          company.getName(),
          tradingName != null ? tradingName.getName() : "");
    }
  }

  protected boolean isInvoiceSetNew(DebtRecovery debtRecovery, LocalDate oldReferenceDate) {

    if (debtRecovery.getInvoiceDebtRecoverySet() != null && oldReferenceDate != null) {
      return debtRecovery.getInvoiceDebtRecoverySet().stream()
          .allMatch(invoice -> invoice.getDueDate().isAfter(oldReferenceDate));
    }
    return false;
  }

  public void updateInvoiceDebtRecovery(DebtRecovery debtRecovery, List<Invoice> invoiceList) {
    debtRecovery.setInvoiceDebtRecoverySet(new HashSet<Invoice>());
    debtRecovery.getInvoiceDebtRecoverySet().addAll(invoiceList);
  }

  public void updateInvoiceTermDebtRecovery(
      DebtRecovery debtRecovery, List<InvoiceTerm> invoiceTermList) {
    debtRecovery.setInvoiceTermDebtRecoverySet(Sets.newHashSet());
    debtRecovery.getInvoiceTermDebtRecoverySet().addAll(invoiceTermList);
  }

  public AccountingSituation getAccountingSituation(DebtRecovery debtRecovery) {
    if (debtRecovery.getTradingName() == null) {
      return debtRecovery.getAccountingSituation();
    } else {
      return debtRecovery.getTradingNameAccountingSituation();
    }
  }
}
