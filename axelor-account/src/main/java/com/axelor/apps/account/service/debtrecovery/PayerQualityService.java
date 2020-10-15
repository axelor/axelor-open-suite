/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryHistory;
import com.axelor.apps.account.db.DebtRecoveryLevel;
import com.axelor.apps.account.db.DebtRecoveryMethodLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayerQualityConfigLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayerQualityService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppAccountService appAccountService;
  protected PartnerRepository partnerRepository;

  @Inject
  public PayerQualityService(
      AppAccountService appAccountService, PartnerRepository partnerRepository) {

    this.appAccountService = appAccountService;
    this.partnerRepository = partnerRepository;
  }

  // TODO : à remplacer par une requête afin de rendre le traitement scalable
  public List<DebtRecoveryHistory> getDebtRecoveryHistoryList(Partner partner) {
    List<DebtRecoveryHistory> debtRecoveryHistoryList = new ArrayList<DebtRecoveryHistory>();
    if (partner.getAccountingSituationList() != null) {
      for (AccountingSituation accountingSituation : partner.getAccountingSituationList()) {
        DebtRecovery debtRecovery = accountingSituation.getDebtRecovery();
        if (debtRecovery != null
            && debtRecovery.getDebtRecoveryHistoryList() != null
            && !debtRecovery.getDebtRecoveryHistoryList().isEmpty()) {
          for (DebtRecoveryHistory debtRecoveryHistory :
              debtRecovery.getDebtRecoveryHistoryList()) {
            if ((debtRecoveryHistory.getDebtRecoveryDate() != null
                && debtRecoveryHistory
                    .getDebtRecoveryDate()
                    .isAfter(
                        appAccountService.getTodayDate(debtRecovery.getCompany()).minusYears(1)))) {
              debtRecoveryHistoryList.add(debtRecoveryHistory);
            }
          }
        }
      }
    }
    return debtRecoveryHistoryList;
  }

  public List<MoveLine> getMoveLineRejectList(Partner partner) {

    MoveLineRepository moveLineRepo = Beans.get(MoveLineRepository.class);

    return moveLineRepo
        .all()
        .filter(
            "self.partner = ?1 AND self.date > ?2 AND self.interbankCodeLine IS NOT NULL",
            partner,
            appAccountService.getTodayDate(AuthUtils.getUser().getActiveCompany()).minusYears(1))
        .fetch();
  }

  public BigDecimal getPayerQualityNote(
      Partner partner, List<PayerQualityConfigLine> payerQualityConfigLineList) {
    BigDecimal burden = BigDecimal.ZERO;

    List<DebtRecoveryHistory> debtRecoveryHistoryList = this.getDebtRecoveryHistoryList(partner);
    List<MoveLine> moveLineList = this.getMoveLineRejectList(partner);

    log.debug(
        "Tiers {} : Nombre de relance concernée : {}",
        partner.getName(),
        debtRecoveryHistoryList.size());
    log.debug("Tiers {} : Nombre de rejets concernée : {}", partner.getName(), moveLineList.size());

    for (DebtRecoveryHistory debtRecoveryHistory : debtRecoveryHistoryList) {
      burden =
          burden.add(this.getPayerQualityNote(debtRecoveryHistory, payerQualityConfigLineList));
    }
    for (MoveLine moveLine : moveLineList) {
      burden = burden.add(this.getPayerQualityNote(moveLine, payerQualityConfigLineList));
    }
    log.debug("Tiers {} : Qualité payeur : {}", partner.getName(), burden);
    return burden;
  }

  public BigDecimal getPayerQualityNote(
      DebtRecoveryHistory debtRecoveryHistory,
      List<PayerQualityConfigLine> payerQualityConfigLineList) {
    DebtRecoveryLevel debtRecoveryLevel = this.getDebtRecoveryLevel(debtRecoveryHistory);
    if (debtRecoveryLevel != null) {
      for (PayerQualityConfigLine payerQualityConfigLine : payerQualityConfigLineList) {
        if (payerQualityConfigLine.getIncidentTypeSelect() == 0
            && payerQualityConfigLine.getDebtRecoveryLevel().equals(debtRecoveryLevel)) {
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

  public DebtRecoveryLevel getDebtRecoveryLevel(DebtRecoveryHistory debtRecoveryHistory) {
    DebtRecoveryMethodLine debtRecoveryMethodLine = null;

    if (debtRecoveryHistory.getDebtRecoveryDate() != null) {

      debtRecoveryMethodLine = debtRecoveryHistory.getDebtRecoveryMethodLine();
    }

    if (debtRecoveryMethodLine != null) {
      return debtRecoveryMethodLine.getDebtRecoveryLevel();
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
          I18n.get(IExceptionMessage.PAYER_QUALITY_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION));
    }

    List<Partner> partnerList = this.getPartnerList();
    if (partnerList != null && partnerList.size() != 0) {
      for (Partner partner : partnerList) {
        BigDecimal burden = this.getPayerQualityNote(partner, payerQualityConfigLineList);

        if (burden.compareTo(BigDecimal.ZERO) == 1) {
          partner.setPayerQuality(burden);
          partnerRepository.save(partner);
          log.debug("Tiers payeur {} : Qualité payeur : {}", partner.getName(), burden);
        }
      }
    }
  }
}
