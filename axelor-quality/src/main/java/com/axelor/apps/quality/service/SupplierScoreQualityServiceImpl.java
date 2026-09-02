/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.supplychain.db.SupplierScoreHistory;
import com.axelor.apps.supplychain.db.repo.SupplierScoreHistoryRepository;
import com.axelor.apps.supplychain.service.SupplierScoreServiceImpl;
import com.axelor.apps.supplychain.service.SupplierScoreTool;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.studio.db.AppSupplychain;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class SupplierScoreQualityServiceImpl extends SupplierScoreServiceImpl {

  protected final AppQualityService appQualityService;

  @Inject
  public SupplierScoreQualityServiceImpl(
      PartnerRepository partnerRepository,
      SupplierScoreHistoryRepository supplierScoreHistoryRepository,
      AppSupplychainService appSupplychainService,
      AppBaseService appBaseService,
      AppQualityService appQualityService) {
    super(partnerRepository, supplierScoreHistoryRepository, appSupplychainService, appBaseService);
    this.appQualityService = appQualityService;
  }

  @Override
  protected void computeRates(Partner partner, LocalDate fromDate, LocalDate toDate) {
    super.computeRates(partner, fromDate, toDate);
    if (!appQualityService.isApp("quality")) {
      return;
    }
    if (Boolean.TRUE.equals(
        appSupplychainService.getAppSupplychain().getAutoComputeSupplierOpenQiRate())) {
      partner.setSupplierOpenQiRate(computeOpenQiRate(partner, fromDate, toDate));
    }
  }

  @Override
  protected List<Pair<BigDecimal, BigDecimal>> getWeightedRates(
      Partner partner, AppSupplychain appSupplychain) {
    List<Pair<BigDecimal, BigDecimal>> weightedRates =
        super.getWeightedRates(partner, appSupplychain);
    if (!appQualityService.isApp("quality")) {
      return weightedRates;
    }
    weightedRates.add(
        Pair.of(partner.getSupplierOpenQiRate(), appSupplychain.getSupplierScoreQiWeight()));
    return weightedRates;
  }

  @Override
  protected void fillSnapshot(SupplierScoreHistory supplierScoreHistory, Partner partner) {
    super.fillSnapshot(supplierScoreHistory, partner);
    if (!appQualityService.isApp("quality")) {
      return;
    }
    supplierScoreHistory.setSupplierOpenQiRate(partner.getSupplierOpenQiRate());
  }

  protected BigDecimal computeOpenQiRate(Partner partner, LocalDate fromDate, LocalDate toDate) {
    long receptionCount = countReceptions(partner, fromDate, toDate);
    if (receptionCount == 0) {
      return null;
    }
    return SupplierScoreTool.computeQiRate(
        countOpenQualityImprovements(partner, fromDate, toDate), receptionCount);
  }

  protected long countOpenQualityImprovements(
      Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Long> query =
        JPA.em()
            .createQuery(
                "SELECT COUNT(qi.id) FROM QualityImprovement qi "
                    + "JOIN qi.qiIdentification qid "
                    + "JOIN qi.qiStatus st "
                    + "WHERE qid.supplierPartner.id = :partnerId "
                    + "AND st.isClosedStatus = false "
                    + "AND st.isCancelledStatus = false "
                    + "AND qi.createdOn >= :fromDateTime "
                    + "AND qi.createdOn < :toDateTime",
                Long.class);
    query.setParameter("partnerId", partner.getId());
    query.setParameter("fromDateTime", fromDate.atStartOfDay());
    query.setParameter("toDateTime", toDate.plusDays(1).atStartOfDay());
    return query.getSingleResult();
  }
}
