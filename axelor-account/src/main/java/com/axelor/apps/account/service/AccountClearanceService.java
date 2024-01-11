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
import com.axelor.apps.account.db.AccountClearance;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountClearanceRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountClearanceService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveLineRepository moveLineRepo;
  protected SequenceService sequenceService;
  protected ReconcileService reconcileService;
  protected TaxService taxService;
  protected TaxAccountService taxAccountService;
  protected AccountClearanceRepository accountClearanceRepo;
  protected AppBaseService appBaseService;
  protected User user;
  protected MoveLineCreateService moveLineCreateService;
  protected BankDetailsService bankDetailsService;
  protected AccountingSituationService accountingSituationService;
  protected AccountingSituationRepository accountingSituationRepo;

  @Inject
  public AccountClearanceService(
      UserService userService,
      AppBaseService appBaseService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineRepository moveLineRepo,
      SequenceService sequenceService,
      ReconcileService reconcileService,
      TaxService taxService,
      TaxAccountService taxAccountService,
      AccountClearanceRepository accountClearanceRepo,
      MoveLineCreateService moveLineCreateService,
      BankDetailsService bankDetailsService,
      AccountingSituationService accountingSituationService,
      AccountingSituationRepository accountingSituationRepo) {

    this.appBaseService = appBaseService;
    this.user = userService.getUser();
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveLineRepo = moveLineRepo;
    this.sequenceService = sequenceService;
    this.reconcileService = reconcileService;
    this.taxService = taxService;
    this.taxAccountService = taxAccountService;
    this.accountClearanceRepo = accountClearanceRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.bankDetailsService = bankDetailsService;
    this.accountingSituationService = accountingSituationService;
    this.accountingSituationRepo = accountingSituationRepo;
  }

  public List<? extends MoveLine> getExcessPayment(AccountClearance accountClearance)
      throws AxelorException {

    Company company = accountClearance.getCompany();

    this.testCompanyField(company);

    List<? extends MoveLine> moveLineList =
        moveLineRepo
            .all()
            .filter(
                "self.move.company = ?1 AND self.account.useForPartnerBalance = 'true' "
                    + "AND (self.move.statusSelect = ?2 OR self.move.statusSelect = ?3) "
                    + "AND self.amountRemaining > 0 AND self.amountRemaining <= ?4 AND self.credit > 0 AND self.account in ?5 AND self.date <= ?6",
                company,
                MoveRepository.STATUS_ACCOUNTED,
                MoveRepository.STATUS_DAYBOOK,
                accountClearance.getAmountThreshold(),
                company.getAccountConfig().getClearanceAccountSet(),
                accountClearance.getDateThreshold())
            .fetch();

    log.debug("Fetched excess payment list: {}", moveLineList);

    return moveLineList;
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  public void setExcessPayment(AccountClearance accountClearance) throws AxelorException {
    accountClearance.setMoveLineSet(new HashSet<MoveLine>());
    List<MoveLine> moveLineList = (List<MoveLine>) this.getExcessPayment(accountClearance);
    if (moveLineList != null && moveLineList.size() != 0) {
      accountClearance.getMoveLineSet().addAll(moveLineList);
    }
    accountClearanceRepo.save(accountClearance);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validateAccountClearance(AccountClearance accountClearance) throws AxelorException {
    Company company = accountClearance.getCompany();
    AccountConfig accountConfig = company.getAccountConfig();

    Tax tax = accountConfig.getStandardRateTax();
    Account profitAccount = accountConfig.getProfitAccount();
    Journal journal = accountConfig.getAccountClearanceJournal();

    Set<MoveLine> moveLineList = accountClearance.getMoveLineSet();

    for (MoveLine moveLine : moveLineList) {
      AccountingSituation accountingSituation =
          accountingSituationRepo.findByCompanyAndPartner(company, moveLine.getPartner());

      if (accountingSituation == null
          || accountingSituation.getVatSystemSelect() == null
          || accountingSituation.getVatSystemSelect()
              == AccountingSituationRepository.VAT_SYSTEM_DEFAULT) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_MISSING_ACCOUNTING_SITUATION),
            moveLine.getPartner().getFullName(),
            company.getCode());
      }

      int vatSystemSelect =
          accountingSituationService.determineVatSystemSelect(accountingSituation, profitAccount);

      Account taxAccount =
          taxAccountService.getAccount(
              tax, company, journal, vatSystemSelect, false, MoveRepository.FUNCTIONAL_ORIGIN_SALE);
      Move move =
          this.createAccountClearanceMove(
              moveLine,
              tax,
              taxAccount,
              profitAccount,
              company,
              journal,
              accountClearance,
              vatSystemSelect);
      moveValidateService.accounting(move);
    }

    accountClearance.setStatusSelect(AccountClearanceRepository.STATUS_VALIDATED);
    accountClearance.setDateTime(appBaseService.getTodayDateTime());
    accountClearance.setName(
        sequenceService.getSequenceNumber(
            SequenceRepository.ACCOUNT_CLEARANCE, company, AccountClearance.class, "name"));
    accountClearanceRepo.save(accountClearance);
  }

  public Move createAccountClearanceMove(
      MoveLine moveLine,
      Tax tax,
      Account taxAccount,
      Account profitAccount,
      Company company,
      Journal journal,
      AccountClearance accountClearance,
      int vatSystemSelect)
      throws AxelorException {
    Partner partner = moveLine.getPartner();

    // Move
    BankDetails companyBankDetails = null;
    if (company != null) {
      companyBankDetails =
          bankDetailsService.getDefaultCompanyBankDetails(company, null, partner, null);
    }
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            null,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            moveLine.getMove().getFunctionalOriginSelect(),
            null,
            null,
            companyBankDetails);

    // Debit MoveLine 411
    BigDecimal amount = moveLine.getAmountRemaining();
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            moveLine.getAccount(),
            amount,
            true,
            appBaseService.getTodayDateTime().toLocalDate(),
            1,
            null,
            null);
    move.getMoveLineList().add(debitMoveLine);

    // Credit MoveLine 77. (profit account)
    BigDecimal taxRate =
        taxService.getTaxRate(tax, appBaseService.getTodayDateTime().toLocalDate());
    BigDecimal divid = taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE);
    BigDecimal profitAmount =
        amount
            .divide(divid, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    MoveLine creditMoveLine1 =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            profitAccount,
            profitAmount,
            false,
            appBaseService.getTodayDateTime().toLocalDate(),
            2,
            null,
            null);
    this.setTax(creditMoveLine1, tax, vatSystemSelect);
    move.getMoveLineList().add(creditMoveLine1);

    // Credit MoveLine 445 (Tax account)
    BigDecimal taxAmount = amount.subtract(profitAmount);
    MoveLine creditMoveLine2 =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            taxAccount,
            taxAmount,
            false,
            appBaseService.getTodayDateTime().toLocalDate(),
            3,
            null,
            null);
    this.setTax(creditMoveLine2, tax, vatSystemSelect);
    move.getMoveLineList().add(creditMoveLine2);

    Reconcile reconcile = reconcileService.createReconcile(debitMoveLine, moveLine, amount, false);
    if (reconcile != null) {
      reconcileService.confirmReconcile(reconcile, true, true);
    }

    debitMoveLine.setAccountClearance(accountClearance);
    creditMoveLine1.setAccountClearance(accountClearance);
    creditMoveLine2.setAccountClearance(accountClearance);
    return move;
  }

  protected void setTax(MoveLine moveLine, Tax tax, int vatSystemSelect) {
    TaxLine taxLine = tax.getActiveTaxLine();

    moveLine.setTaxLine(taxLine);
    moveLine.setTaxRate(taxLine.getValue());
    moveLine.setTaxCode(tax.getCode());
    moveLine.setVatSystemSelect(vatSystemSelect);
  }

  public AccountClearance createAccountClearance(
      Company company,
      String name,
      BigDecimal amountThreshold,
      LocalDate dateThreshold,
      List<MoveLine> moveLineSet) {
    AccountClearance accountClearance = new AccountClearance();
    accountClearance.setAmountThreshold(amountThreshold);
    accountClearance.setCompany(company);
    accountClearance.setDateThreshold(dateThreshold);
    accountClearance.getMoveLineSet().addAll(moveLineSet);
    accountClearance.setName(name);
    accountClearance.setDateTime(appBaseService.getTodayDateTime());
    accountClearance.setUser(this.user);
    accountClearance.setStatusSelect(AccountClearanceRepository.STATUS_VALIDATED);
    return accountClearance;
  }

  /**
   * Procédure permettant de vérifier les champs d'une société
   *
   * @param company Une société
   * @throws AxelorException
   */
  public void testCompanyField(Company company) throws AxelorException {

    AccountConfig accountConfig = company.getAccountConfig();

    if (accountConfig == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    if (accountConfig.getProfitAccount() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    if (accountConfig.getStandardRateTax() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    if (accountConfig.getClearanceAccountSet() == null
        || accountConfig.getClearanceAccountSet().size() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    if (!sequenceService.hasSequence(SequenceRepository.ACCOUNT_CLEARANCE, company)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_5),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }

    if (accountConfig.getAccountClearanceJournal() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_CLEARANCE_6),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName());
    }
  }
}
