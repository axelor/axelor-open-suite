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

import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.DebtRecoveryMethodLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayerQualityConfigLine;
import com.axelor.apps.account.db.repo.DebtRecoveryHistoryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayerQualityService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppAccountService appAccountService;
  protected PartnerRepository partnerRepository;
  protected DebtRecoveryHistoryRepository debtRecoveryHistoryRepo;

  @Inject
  public PayerQualityService(
      AppAccountService appAccountService,
      PartnerRepository partnerRepository,
      DebtRecoveryHistoryRepository debtRecoveryHistoryRepo) {

    this.appAccountService = appAccountService;
    this.partnerRepository = partnerRepository;
    this.debtRecoveryHistoryRepo = debtRecoveryHistoryRepo;
  }

  public List<DebtRecoveryHistory> getDebtRecoveryHistoryList(Partner partner) {
    return debtRecoveryHistoryRepo
        .all()
        .filter(
            "(self.debtRecovery.accountingSituation.partner = ?1 OR self.debtRecovery.tradingNameAccountingSituation.partner = ?1) AND self.debtRecoveryDate > ?2",
            partner,
            appAccountService.getTodayDate(null).minusYears(1))
        .fetch();
  }

  public List<MoveLine> getMoveLineRejectList(Partner partner) {

    MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);

    return moveLineRepo
        .all()
        .filter(
            "self.partner = ?1 AND self.date > ?2 AND self.interbankCodeLine IS NOT NULL",
            partner,
            appAccountService
                .getTodayDate(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
                .minusYears(1))
        .fetch();
  }

  public BigDecimal getPayerQualityNote(
      Partner partner, List<PayerQualityConfigLine> payerQualityConfigLineList) {
    BigDecimal burden = BigDecimal.ZERO;

    List<DebtRecoveryHistory> debtRecoveryHistoryList = this.getDebtRecoveryHistoryList(partner);
    List<MoveLine> moveLineList = this.getMoveLineRejectList(partner);

    log.debug(
        "Partner {} : Number of recovery concerned : {}",
        partner.getName(),
        debtRecoveryHistoryList.size());
    log.debug(
        "Partner {} : Number of rejection concerned : {}", partner.getName(), moveLineList.size());

    for (DebtRecoveryHistory debtRecoveryHistory : debtRecoveryHistoryList) {
      burden =
          burden.add(this.getPayerQualityNote(debtRecoveryHistory, payerQualityConfigLineList));
    }
    for (MoveLine moveLine : moveLineList) {
      burden = burden.add(this.getPayerQualityNote(moveLine, payerQualityConfigLineList));
    }
    log.debug("Partner {} : Payer quality : {}", partner.getName(), burden);
    return burden;
  }

  public BigDecimal getPayerQualityNote(
      DebtRecoveryHistory debtRecoveryHistory,
      List<PayerQualityConfigLine> payerQualityConfigLineList) {
    Integer debtRecoveryLevel = this.getDebtRecoveryLevel(debtRecoveryHistory);
    if (debtRecoveryLevel != null) {
      for (PayerQualityConfigLine payerQualityConfigLine : payerQualityConfigLineList) {
        if (payerQualityConfigLine.getIncidentTypeSelect() == 0
            && payerQualityConfigLine.getSequence() == debtRecoveryLevel) {
          return payerQualityConfigLine.getBurden();
        }
      }
    }
    return BigDecimal.ZERO;
  }

  public BigDecimal getPayerQualityNote(
      MoveLine moveLine, List<PayerQualityConfigLine> payerQualityConfigLineList) {
    for (PayerQualityConfigLine payerQualityConfigLine : payerQualityConfigLineList) {
      if (payerQualityConfigLine.getIncidentTypeSelect() == 1
          && !moveLine.getInterbankCodeLine().getTechnicalRejectOk()) {
        return payerQualityConfigLine.getBurden();
      }
    }
    return BigDecimal.ZERO;
  }

  public Integer getDebtRecoveryLevel(DebtRecoveryHistory debtRecoveryHistory) {
    DebtRecoveryMethodLine debtRecoveryMethodLine = null;

    if (debtRecoveryHistory.getDebtRecoveryDate() != null) {

      debtRecoveryMethodLine = debtRecoveryHistory.getDebtRecoveryMethodLine();
    }

    if (debtRecoveryMethodLine != null) {
      return debtRecoveryMethodLine.getSequence();
    } else {
      return null;
    }
  }

  public List<Partner> getPartnerList() {
    return partnerRepository.all().filter("self.isCustomer = true").fetch();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void payerQualityProcess() throws AxelorException {
    List<PayerQualityConfigLine> payerQualityConfigLineList =
        appAccountService.getAppAccount().getPayerQualityConfigLineList();
    if (payerQualityConfigLineList == null || payerQualityConfigLineList.size() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYER_QUALITY_1),
          I18n.get(BaseExceptionMessage.EXCEPTION));
    }

    List<Partner> partnerList = this.getPartnerList();
    if (partnerList != null && partnerList.size() != 0) {
      for (Partner partner : partnerList) {
        BigDecimal burden = this.getPayerQualityNote(partner, payerQualityConfigLineList);

        if (burden.compareTo(BigDecimal.ZERO) == 1) {
          partner.setPayerQuality(burden);
          partnerRepository.save(partner);
          log.debug("Partner payer {} : Payer quality : {}", partner.getName(), burden);
        }
      }
    }
  }
}
