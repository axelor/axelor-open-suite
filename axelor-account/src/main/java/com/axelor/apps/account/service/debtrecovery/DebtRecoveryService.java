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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.DebtRecoveryMethod;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
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
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.db.repo.MultiRelatedRepository;
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
   * Fonction qui récupère la plus ancienne date d'échéance d'une liste d'échéances
   *
   * @param invoiceTermList Une liste d'échéances
   * @return la plus ancienne date d'échéance
   */
  public LocalDate getOldDateInvoiceTerm(List<InvoiceTerm> invoiceTermList, Company company) {
    LocalDate minInvoiceTermDate;

    if (!invoiceTermList.isEmpty()) {
      minInvoiceTermDate =
          appAccountService.getTodayDate(
              company != null
                  ? company
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        LocalDate invoiceTermDueDate = invoiceTerm.getDueDate();
        if (invoiceTermDueDate != null && minInvoiceTermDate.isAfter(invoiceTermDueDate)) {
          minInvoiceTermDate = invoiceTermDueDate;
        }
      }
    } else {
      minInvoiceTermDate = null;
    }
    return minInvoiceTermDate;
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
  public LocalDate getReferenceDate(DebtRecovery debtRecovery, List<InvoiceTerm> invoiceTermList) {
    AccountingSituation accountingSituation = this.getAccountingSituation(debtRecovery);

    // 1: Date la plus ancienne des échéances
    LocalDate minInvoiceTermDate =
        getOldDateInvoiceTerm(invoiceTermList, accountingSituation.getCompany());
    log.debug("minMoveLineDate : {}", minInvoiceTermDate);

    // 2: Date la plus récente des relances
    LocalDate debtRecoveryLastDate = getLastDateDebtRecovery(debtRecovery);
    log.debug("debtRecoveryLastDate : {}", debtRecoveryLastDate);

    // Date de référence : Date la plus récente des deux ensembles (1 et 2)
    LocalDate debtRecoveryRefDate = getLastDate(minInvoiceTermDate, debtRecoveryLastDate);
    log.debug("debtRecoveryRefDate : {}", debtRecoveryRefDate);

    return debtRecoveryRefDate;
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

  protected void addInvoiceTerms(
      Partner partner, Company company, TradingName tradingName, List<Long> idList) {
    LocalDate todayDate = appAccountService.getTodayDate(company);
    int mailTransitTime = company.getAccountConfig().getMailTransitTime();

    javax.persistence.Query invoiceTermQuery =
        JPA.em()
            .createQuery(computeQuery(tradingName))
            .setParameter("partner", partner)
            .setParameter("company", company)
            .setParameter("todayDate", todayDate)
            .setParameter("todayDateMinusTransitTime", todayDate.minusDays(mailTransitTime))
            .setParameter("daybookStatus", MoveRepository.STATUS_DAYBOOK)
            .setParameter("accountedStatus", MoveRepository.STATUS_ACCOUNTED)
            .setParameter("paymentSessionStatus", PaymentSessionRepository.STATUS_ONGOING)
            .setParameter("functionalOriginSelect", MoveRepository.FUNCTIONAL_ORIGIN_SALE);

    invoiceTermQuery = addTradingNameBinding(tradingName, invoiceTermQuery);

    idList.addAll(invoiceTermQuery.getResultList());
  }

  protected javax.persistence.Query addTradingNameBinding(
      TradingName tradingName, javax.persistence.Query invoiceTermQuery) {
    if (tradingName != null) {
      invoiceTermQuery = invoiceTermQuery.setParameter("tradingName", tradingName);
    }
    return invoiceTermQuery;
  }

  protected String computeQuery(TradingName tradingName) {
    StringBuilder query = new StringBuilder();

    computeQueryJoins(query);
    computeQueryConditions(tradingName, query);

    return query.toString();
  }

  protected void computeQueryConditions(TradingName tradingName, StringBuilder query) {
    query.append(
        "WHERE (paymentsession.id IS NULL OR paymentsession.statusSelect != :paymentSessionStatus) ");
    query.append("AND invoiceterm.amountRemaining > 0 ");
    query.append("AND invoiceterm.isPaid IS FALSE ");
    query.append("AND invoiceterm.debtRecoveryBlockingOk IS FALSE ");
    query.append("AND moveline.id IS NOT NULL ");
    query.append("AND moveline.partner = :partner ");
    query.append("AND move.company = :company ");
    query.append("AND invoiceterm.dueDate IS NOT NULL ");
    query.append("AND move.id IS NOT NULL ");
    query.append("AND move.date IS NOT NULL ");
    query.append("AND move.date < :todayDateMinusTransitTime ");
    query.append("AND invoiceterm.dueDate <= :todayDate ");
    query.append("AND move.functionalOriginSelect = :functionalOriginSelect ");
    query.append("AND move.statusSelect IN (:daybookStatus, :accountedStatus) ");

    if (tradingName != null) {
      query.append("AND move.tradingName = :tradingName ");
    }
  }

  protected void computeQueryJoins(StringBuilder query) {
    query.append("SELECT DISTINCT invoiceterm.id ");
    query.append("FROM InvoiceTerm invoiceterm ");
    query.append(
        "LEFT JOIN PaymentSession paymentsession ON paymentsession.id = invoiceterm.paymentSession ");
    query.append("LEFT JOIN MoveLine moveline ON moveline.id = invoiceterm.moveLine ");
    query.append("LEFT JOIN Move move ON move.id = moveline.move ");
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
      Partner partner, Company company, TradingName tradingName) {
    List<Long> idList = new ArrayList<>();

    addInvoiceTerms(partner, company, tradingName, idList);

    return invoiceTermRepo.findByIds(idList.stream().distinct().collect(Collectors.toList()));
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

        List<InvoiceTerm> invoiceTermList = this.getInvoiceTerms(partner, company, tradingName);

        if (ObjectUtils.isEmpty(invoiceTermList)) {
          debtRecoverySessionService.debtRecoveryInitialization(debtRecovery);
        }

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

        this.updateInvoiceTermDebtRecovery(debtRecovery, invoiceTermList);
        this.updateInvoiceDebtRecovery(
            debtRecovery, this.getInvoiceListFromInvoiceTerm(invoiceTermList));

        debtRecovery.setBalanceDueDebtRecovery(balanceDueDebtRecovery);

        Integer levelDebtRecovery = -1;
        if (debtRecovery.getDebtRecoveryMethodLine() != null) {
          levelDebtRecovery = debtRecovery.getDebtRecoveryMethodLine().getSequence();
        }

        LocalDate oldReferenceDate = debtRecovery.getReferenceDate();
        LocalDate referenceDate = this.getReferenceDate(debtRecovery, invoiceTermList);

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
                  + (tradingName != null ? I18n.get("Trading name") + " %s : " : "")
                  + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_2),
              I18n.get(BaseExceptionMessage.EXCEPTION),
              partner.getName(),
              company.getName(),
              tradingName != null ? tradingName.getName() : "");
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
          // TODO Alarm ?
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
          .allMatch(
              invoice ->
                  invoice.getDueDate() != null && invoice.getDueDate().isAfter(oldReferenceDate));
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
