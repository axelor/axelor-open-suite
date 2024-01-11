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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.move.MoveLineControlService;
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
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MoveRecordSetServiceImpl implements MoveRecordSetService {

  protected MoveLineControlService moveLineControlService;
  protected PartnerRepository partnerRepository;
  protected BankDetailsService bankDetailsService;
  protected MoveToolService moveToolService;
  protected PeriodService periodService;
  protected PaymentConditionService paymentConditionService;

  @Inject
  public MoveRecordSetServiceImpl(
      MoveLineControlService moveLineControlService,
      PartnerRepository partnerRepository,
      BankDetailsService bankDetailsService,
      MoveToolService moveToolService,
      PeriodService periodService,
      PaymentConditionService paymentConditionService) {
    this.moveLineControlService = moveLineControlService;
    this.partnerRepository = partnerRepository;
    this.bankDetailsService = bankDetailsService;
    this.moveToolService = moveToolService;
    this.periodService = periodService;
    this.paymentConditionService = paymentConditionService;
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

    if (partner != null
        && journalType != null
        && !journalType
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
      PaymentCondition paymentCondition = partner.getPaymentCondition();
      paymentConditionService.checkPaymentCondition(paymentCondition);
      move.setPaymentCondition(paymentCondition);
    } else {
      move.setPaymentCondition(null);
    }
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
    move.setFunctionalOriginSelect(computeFunctionalOriginSelect(move));
  }

  /**
   * Compute the default functional origin select of the move.
   *
   * @param move any move, cannot be null
   * @return the default functional origin select if there is one, else return null
   */
  protected Integer computeFunctionalOriginSelect(Move move) {
    if (move.getJournal() == null) {
      return null;
    }
    String authorizedFunctionalOriginSelect =
        move.getJournal().getAuthorizedFunctionalOriginSelect();

    if (ObjectUtils.isEmpty(authorizedFunctionalOriginSelect)
        || authorizedFunctionalOriginSelect.split(",").length != 1) {
      return null;
    }

    return Integer.valueOf(authorizedFunctionalOriginSelect);
  }

  @Override
  public void setCompanyBankDetails(Move move) throws AxelorException {
    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();
    Partner partner = move.getPartner();

    if (company == null) {
      move.setCompanyBankDetails(null);
      return;
    }

    if (partner != null) {
      partner = partnerRepository.find(partner.getId());
    }

    BankDetails defaultBankDetails =
        bankDetailsService.getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    move.setCompanyBankDetails(defaultBankDetails);
  }
}
