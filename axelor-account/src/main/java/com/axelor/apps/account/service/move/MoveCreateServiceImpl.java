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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.inject.Beans;
import com.axelor.utils.exception.ToolExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MoveCreateServiceImpl implements MoveCreateService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PeriodService periodService;
  protected MoveRepository moveRepository;
  protected CompanyConfigService companyConfigService;

  protected AppAccountService appAccountService;
  protected PaymentConditionService paymentConditionService;

  @Inject
  public MoveCreateServiceImpl(
      AppAccountService appAccountService,
      PeriodService periodService,
      MoveRepository moveRepository,
      CompanyConfigService companyConfigService,
      PaymentConditionService paymentConditionService) {

    this.periodService = periodService;
    this.moveRepository = moveRepository;
    this.companyConfigService = companyConfigService;
    this.appAccountService = appAccountService;
    this.paymentConditionService = paymentConditionService;
  }

  /**
   * Créer une écriture comptable à la date du jour impactant la compta.
   *
   * @param journal
   * @param period
   * @param company
   * @param invoice
   * @param partner
   * @param isReject <code>true = écriture de rejet avec séquence spécifique</code>
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      int technicalOriginSelect,
      int functionalOriginSelect,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {
    return this.createMove(
        journal,
        company,
        currency,
        partner,
        appAccountService.getTodayDate(company),
        null,
        paymentMode,
        fiscalPosition,
        technicalOriginSelect,
        functionalOriginSelect,
        origin,
        description,
        companyBankDetails);
  }

  @Override
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      LocalDate date,
      LocalDate originDate,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      BankDetails bankDetails,
      int technicalOriginSelect,
      int functionalOriginSelect,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {
    return this.createMove(
        journal,
        company,
        currency,
        partner,
        date,
        originDate,
        paymentMode,
        fiscalPosition,
        bankDetails,
        technicalOriginSelect,
        functionalOriginSelect,
        false,
        false,
        false,
        origin,
        description,
        companyBankDetails);
  }

  /**
   * create an account move
   *
   * @param journal
   * @param company
   * @param currency
   * @param partner
   * @param date
   * @param paymentMode
   * @param fiscalPosition
   * @param technicalOriginSelect
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      LocalDate date,
      LocalDate originDate,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      int technicalOriginSelect,
      int functionalOriginSelect,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {
    return this.createMove(
        journal,
        company,
        currency,
        partner,
        date,
        originDate,
        paymentMode,
        fiscalPosition,
        null,
        technicalOriginSelect,
        functionalOriginSelect,
        false,
        false,
        false,
        origin,
        description,
        companyBankDetails);
  }

  /**
   * Creating a new generic accounting move
   *
   * @param journal
   * @param company
   * @param currency
   * @param partner
   * @param date
   * @param paymentMode
   * @param fiscalPosition
   * @param technicalOriginSelect
   * @param ignoreInDebtRecoveryOk
   * @param ignoreInAccountingOk
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      LocalDate date,
      LocalDate originDate,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      BankDetails bankDetails,
      int technicalOriginSelect,
      int functionalOriginSelect,
      boolean ignoreInDebtRecoveryOk,
      boolean ignoreInAccountingOk,
      boolean autoYearClosureMove,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {
    log.debug(
        "Creating a new generic accounting move (journal : {}, company : {}",
        new Object[] {journal.getName(), company.getName()});

    Move move = new Move();

    move.setJournal(journal);
    move.setCompany(company);

    move.setIgnoreInDebtRecoveryOk(ignoreInDebtRecoveryOk);
    move.setIgnoreInAccountingOk(ignoreInAccountingOk);
    move.setAutoYearClosureMove(autoYearClosureMove);

    if (autoYearClosureMove) {
      move.setPeriod(periodService.getPeriod(date, company, YearRepository.TYPE_FISCAL));
      if (move.getPeriod() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ToolExceptionMessage.PERIOD_1),
            company.getName(),
            L10n.getInstance().format(date));
      }
    } else {
      move.setPeriod(periodService.getActivePeriod(date, company, YearRepository.TYPE_FISCAL));
    }

    move.setDate(date);
    move.setOriginDate(originDate);
    move.setMoveLineList(new ArrayList<MoveLine>());

    Currency companyCurrency = companyConfigService.getCompanyCurrency(company);

    if (companyCurrency != null) {
      move.setCompanyCurrency(companyCurrency);
      move.setCompanyCurrencyCode(companyCurrency.getCodeISO());
    }

    if (currency == null) {
      currency = move.getCompanyCurrency();
    }
    if (currency != null) {
      move.setCurrency(currency);
      move.setCurrencyCode(currency.getCodeISO());
    }

    if (partner != null) {
      PaymentCondition paymentCondition = partner.getPaymentCondition();
      if (paymentCondition != null) {
        paymentConditionService.checkPaymentCondition(paymentCondition);
        move.setPaymentCondition(paymentCondition);
      }
    }

    move.setOrigin(origin);
    move.setDescription(description);
    move.setPartner(partner);
    move.setPaymentMode(paymentMode);
    move.setFiscalPosition(fiscalPosition);
    move.setPartnerBankDetails(bankDetails);
    move.setCompanyBankDetails(companyBankDetails);
    move.setTechnicalOriginSelect(technicalOriginSelect);
    move.setFunctionalOriginSelect(functionalOriginSelect);
    moveRepository.save(move);
    move.setReference(Beans.get(SequenceService.class).getDraftSequenceNumber(move));

    return move;
  }

  /**
   * Créer une écriture comptable de toute pièce spécifique à une saisie paiement.
   *
   * @param journal
   * @param period
   * @param company
   * @param invoice
   * @param partner
   * @param isReject <code>true = écriture de rejet avec séquence spécifique</code>
   * @param agency L'agence dans laquelle s'effectue le paiement
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMoveWithPaymentVoucher(
      Journal journal,
      Company company,
      PaymentVoucher paymentVoucher,
      Partner partner,
      LocalDate date,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      int technicalOriginSelect,
      int functionalOriginSelect,
      String origin,
      String description,
      BankDetails companyBankDetails)
      throws AxelorException {
    Move move =
        this.createMove(
            journal,
            company,
            paymentVoucher.getCurrency(),
            partner,
            date,
            null,
            paymentMode,
            fiscalPosition,
            technicalOriginSelect,
            functionalOriginSelect,
            origin,
            description,
            companyBankDetails);
    move.setPaymentVoucher(paymentVoucher);
    move.setPaymentCondition(null);
    return move;
  }

  @Override
  public Move createMove(
      Journal journal,
      Company company,
      Currency currency,
      Partner partner,
      LocalDate date,
      PaymentMode paymentMode,
      FiscalPosition fiscalPosition,
      int technicalOriginSelect,
      int functionalOriginSelect,
      boolean ignoreInDebtRecoveryOk,
      boolean ignoreInAccountingOk,
      boolean autoYearClosureMove,
      String origin,
      String description,
      Invoice invoice,
      PaymentVoucher paymentVoucher,
      BankDetails companyBankDetails)
      throws AxelorException {
    Move move =
        this.createMove(
            journal,
            company,
            currency,
            partner,
            date,
            null,
            paymentMode,
            fiscalPosition,
            null,
            technicalOriginSelect,
            functionalOriginSelect,
            ignoreInDebtRecoveryOk,
            ignoreInAccountingOk,
            autoYearClosureMove,
            origin,
            description,
            companyBankDetails);
    move.setInvoice(invoice);
    move.setPaymentVoucher(paymentVoucher);
    return move;
  }
}
