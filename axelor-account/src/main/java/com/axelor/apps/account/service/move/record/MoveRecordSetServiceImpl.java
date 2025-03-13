/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class MoveRecordSetServiceImpl implements MoveRecordSetService {

  protected PartnerRepository partnerRepository;
  protected BankDetailsService bankDetailsService;
  protected PeriodService periodService;
  protected PaymentConditionService paymentConditionService;
  protected AppBaseService appBaseService;
  protected PartnerAccountService partnerAccountService;
  protected JournalService journalService;
  protected PfpService pfpService;
  protected MoveToolService moveToolService;
  protected InvoiceTermPfpToolService invoiceTermPfpToolService;

  @Inject
  public MoveRecordSetServiceImpl(
      PartnerRepository partnerRepository,
      BankDetailsService bankDetailsService,
      PeriodService periodService,
      PaymentConditionService paymentConditionService,
      AppBaseService appBaseService,
      PartnerAccountService partnerAccountService,
      JournalService journalService,
      PfpService pfpService,
      MoveToolService moveToolService,
      InvoiceTermPfpToolService invoiceTermPfpToolService) {
    this.partnerRepository = partnerRepository;
    this.bankDetailsService = bankDetailsService;
    this.periodService = periodService;
    this.paymentConditionService = paymentConditionService;
    this.appBaseService = appBaseService;
    this.partnerAccountService = partnerAccountService;
    this.journalService = journalService;
    this.pfpService = pfpService;
    this.moveToolService = moveToolService;
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
  }

  @Override
  public void setPeriod(Move move) {
    try {
      if (move.getDate() != null && move.getCompany() != null) {
        move.setPeriod(
            periodService.getActivePeriod(
                move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));
      }
    } catch (AxelorException axelorException) {
      move.setPeriod(null);
    }
  }

  @Override
  public void setPaymentMode(Move move) {
    Partner partner = move.getPartner();
    JournalType journalType =
        Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);

    if (partner != null && journalType != null) {
      if (journalType
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)) {
        move.setPaymentMode(partner.getOutPaymentMode());
      } else if (journalType
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {
        move.setPaymentMode(partner.getInPaymentMode());
      } else {
        move.setPaymentMode(null);
      }
    } else {
      move.setPaymentMode(null);
    }
  }

  @Override
  public void setPaymentCondition(Move move) throws AxelorException {
    Partner partner = move.getPartner();
    JournalType journalType =
        Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);
    PaymentCondition paymentCondition = null;
    if (partner != null && journalType != null) {

      Integer journalTechnicalType = journalType.getTechnicalTypeSelect();
      if (journalTechnicalType.compareTo(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
          == 0) {
        paymentCondition = partner.getOutPaymentCondition();
      } else if (journalTechnicalType.compareTo(
              JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)
          != 0) {
        paymentCondition = partner.getPaymentCondition();
      }
    }

    paymentConditionService.checkPaymentCondition(paymentCondition);
    move.setPaymentCondition(paymentCondition);
  }

  @Override
  public void setPartnerBankDetails(Move move) {
    Partner partner = move.getPartner();

    if (partner != null) {
      move.setPartnerBankDetails(
          partner.getBankDetailsList().stream()
              .filter(bankDetails -> bankDetails.getIsDefault() && bankDetails.getActive())
              .findFirst()
              .orElse(null));
    } else {
      move.setPartnerBankDetails(null);
    }
  }

  @Override
  public void setCurrencyByPartner(Move move) {
    Partner partner = move.getPartner();

    if (partner != null) {
      move.setCurrency(partner.getCurrency());
      move.setCurrencyCode(
          Optional.ofNullable(partner.getCurrency()).map(Currency::getCodeISO).orElse(null));
      move.setFiscalPosition(partner.getFiscalPosition());
    }
  }

  @Override
  public Map<String, Object> setCurrencyCode(Move move) {
    Objects.requireNonNull(move);

    HashMap<String, Object> resultMap = new HashMap<>();
    if (move.getCurrency() != null) {
      move.setCurrencyCode(move.getCurrency().getCodeISO());
    } else {
      move.setCurrencyCode(null);
    }

    resultMap.put("currencyCode", move.getCurrencyCode());

    return resultMap;
  }

  @Override
  public void setJournal(Move move) {
    move.setJournal(
        Optional.ofNullable(move.getCompany())
            .map(Company::getAccountConfig)
            .map(AccountConfig::getManualMiscOpeJournal)
            .orElse(null));
  }

  @Override
  public void setFunctionalOriginSelect(Move move) {
    move.setFunctionalOriginSelect(
        moveToolService.computeFunctionalOriginSelect(
            move.getJournal(), move.getMassEntryStatusSelect()));
  }

  @Override
  public void setCompanyBankDetails(Move move) throws AxelorException {
    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    BankDetails companyBankDetails = move.getCompanyBankDetails();
    if (company == null) {
      move.setCompanyBankDetails(null);
      return;
    }

    if (partner != null) {
      partner = partnerRepository.find(partner.getId());
    }

    BankDetails defaultBankDetails =
        bankDetailsService.getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    move.setCompanyBankDetails(
        defaultBankDetails != null ? defaultBankDetails : companyBankDetails);
  }

  @Override
  public void setOriginDate(Move move) {
    Objects.requireNonNull(move);

    if (move.getDate() != null
        && move.getJournal() != null
        && move.getJournal().getIsFillOriginDate()) {
      move.setOriginDate(move.getDate());
    } else if (move.getDate() == null
        || (move.getJournal() == null
            || (move.getJournal() != null && !move.getJournal().getIsFillOriginDate()))) {
      move.setOriginDate(null);
    }
  }

  @Override
  public void setPfpStatus(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    if (move.getJournal() != null && move.getJournal().getJournalType() != null) {
      JournalType journalType = move.getJournal().getJournalType();
      if (pfpService.isManagePassedForPayment(move.getCompany())
          && pfpService.isManagePFPInRefund(move.getCompany())
          && (journalType.getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
              || journalType.getTechnicalTypeSelect()
                  == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE)) {
        move.setPfpValidateStatusSelect(MoveRepository.PFP_STATUS_AWAITING);
      }
    }
  }

  @Override
  public void setPfpValidatorUser(Move move) {
    Objects.requireNonNull(move);

    move.setPfpValidatorUser(
        invoiceTermPfpToolService.getPfpValidatorUser(move.getPartner(), move.getCompany()));
  }

  @Override
  public Map<String, Object> computeTotals(Move move) {

    Map<String, Object> values = new HashMap<>();
    if (move.getMoveLineList() == null) {
      return values;
    }
    values.put("$totalLines", move.getMoveLineList().size());

    BigDecimal totalDebit =
        move.getMoveLineList().stream()
            .map(MoveLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalDebit", totalDebit);

    BigDecimal totalCredit =
        move.getMoveLineList().stream()
            .map(MoveLine::getCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCredit", totalCredit);

    Predicate<? super MoveLine> isDebitCreditFilter =
        ml -> ml.getCredit().compareTo(BigDecimal.ZERO) > 0;
    if (totalDebit.compareTo(totalCredit) > 0) {
      isDebitCreditFilter = ml -> ml.getDebit().compareTo(BigDecimal.ZERO) > 0;
    }

    BigDecimal totalCurrency =
        move.getMoveLineList().stream()
            .filter(isDebitCreditFilter)
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .abs();
    values.put("$totalCurrency", totalCurrency);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }

  @Override
  public void setThirdPartyPayerPartner(Move move) {
    if (!appBaseService.getAppBase().getActivatePartnerRelations()) {
      return;
    }

    if (journalService.isThirdPartyPayerOk(move.getJournal())) {
      move.setThirdPartyPayerPartner(partnerAccountService.getPayedByPartner(move.getPartner()));
    } else {
      move.setThirdPartyPayerPartner(null);
    }
  }
}
