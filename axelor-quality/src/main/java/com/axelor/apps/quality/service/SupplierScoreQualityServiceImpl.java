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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    if (Boolean.TRUE.equals(appSupplychain.getAutoComputeSupplierOpenQiRate())) {
      partner.setSupplierOpenQiRate(computeOpenQiRate(partner, fromDate, toDate));
    }
    if (Boolean.TRUE.equals(appSupplychain.getAutoComputeSupplierNonConformityRate())) {
      partner.setSupplierNonConformityRate(
          computeNonConformityRate(partner, fromDate, toDate, appSupplychain));
    }
    partner.setSupplierQualityScore(
        SupplierScoreQualityTool.computeQualityScore(
            partner.getSupplierOpenQiRate(),
            appSupplychain.getSupplierScoreQiWeight(),
            partner.getSupplierNonConformityRate(),
            appSupplychain.getSupplierScoreNonConformityWeight()));
    partner.setSupplierQualityTrendSelect(computeQualityTrend(partner, toDate, appSupplychain));
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
    weightedRates.add(
        Pair.of(
            partner.getSupplierNonConformityRate(),
            appSupplychain.getSupplierScoreNonConformityWeight()));
    return weightedRates;
  }

  @Override
  protected void fillSnapshot(SupplierScoreHistory supplierScoreHistory, Partner partner) {
    super.fillSnapshot(supplierScoreHistory, partner);
    if (!appQualityService.isApp("quality")) {
      return;
    }
    supplierScoreHistory.setSupplierOpenQiRate(partner.getSupplierOpenQiRate());
    supplierScoreHistory.setSupplierNonConformityRate(partner.getSupplierNonConformityRate());
    supplierScoreHistory.setSupplierQualityScore(partner.getSupplierQualityScore());
  }

  protected BigDecimal computeOpenQiRate(Partner partner, LocalDate fromDate, LocalDate toDate) {
    long receptionCount = countReceptions(partner, fromDate, toDate);
    if (receptionCount == 0) {
      return null;
    }
    return SupplierScoreTool.computeQiRate(countOpenQualityImprovements(partner), receptionCount);
  }

  /**
   * The trend is always derived from the monthly history, whatever the computation mode of the
   * rates, so that hand-entered rates follow the same rule as computed ones.
   */
  protected Integer computeQualityTrend(
      Partner partner, LocalDate toDate, AppSupplychain appSupplychain) {
    SupplierScoreHistory referenceSnapshot =
        findReferenceSnapshot(partner, toDate, appSupplychain.getSupplierQualityTrendMonths());
    if (referenceSnapshot == null) {
      return null;
    }
    return SupplierScoreQualityTool.computeTrend(
        partner.getSupplierQualityScore(),
        referenceSnapshot.getSupplierQualityScore(),
        appSupplychain.getSupplierQualityTrendTolerance());
  }

  /**
   * @return the most recent snapshot taken at least the given number of months before the date,
   *     matched on its month so that the day the batch runs on does not matter.
   */
  protected SupplierScoreHistory findReferenceSnapshot(
      Partner partner, LocalDate toDate, Integer months) {
    if (months == null || months <= 0) {
      return null;
    }
    return supplierScoreHistoryRepository
        .all()
        .filter("self.partner.id = :partnerId AND self.periodLabel <= :periodLabel")
        .bind("partnerId", partner.getId())
        .bind("periodLabel", SupplierScoreTool.computePeriodLabel(toDate.minusMonths(months)))
        .order("-periodLabel")
        .fetchOne();
  }

  protected BigDecimal computeNonConformityRate(
      Partner partner, LocalDate fromDate, LocalDate toDate, AppSupplychain appSupplychain) {
    long receptionCount = countReceptions(partner, fromDate, toDate);
    if (receptionCount == 0) {
      return null;
    }
    BigDecimal weightedNonConformityCount =
        SupplierScoreQualityTool.computeWeightedNonConformityCount(
            countNonConformitiesByGravity(partner, fromDate, toDate),
            appSupplychain.getSupplierScoreGravityCriticalWeight(),
            appSupplychain.getSupplierScoreGravityMajorWeight(),
            appSupplychain.getSupplierScoreGravityMinorWeight());
    return SupplierScoreTool.computeQiRate(weightedNonConformityCount, receptionCount);
  }

  /**
   * Non-conformities of the period: every quality improvement carrying the supplier that is not
   * cancelled, dated by its reception when it has one and by its creation date otherwise.
   *
   * @return the number of quality improvements per gravity, the null key holding the ungraded ones.
   */
  protected Map<Integer, Long> countNonConformitiesByGravity(
      Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Object[]> query =
        JPA.em()
            .createQuery(
                "SELECT qi.gravityTypeSelect, COUNT(qi.id) FROM QualityImprovement qi "
                    + "JOIN qi.qiIdentification qid "
                    + "JOIN qi.qiStatus st "
                    + "LEFT JOIN qid.stockMove sm "
                    + "WHERE qid.supplierPartner.id = :partnerId "
                    + "AND st.isCancelledStatus = false "
                    + "AND ((sm.realDate IS NOT NULL AND sm.realDate BETWEEN :fromDate AND :toDate) "
                    + "OR (sm.realDate IS NULL "
                    + "AND qi.createdOn >= :fromDateTime AND qi.createdOn < :toDateTimeExclusive)) "
                    + "GROUP BY qi.gravityTypeSelect",
                Object[].class);
    query.setParameter("partnerId", partner.getId());
    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);
    query.setParameter("fromDateTime", fromDate.atStartOfDay());
    query.setParameter("toDateTimeExclusive", toDate.plusDays(1).atStartOfDay());
    Map<Integer, Long> countByGravity = new HashMap<>();
    for (Object[] row : query.getResultList()) {
      countByGravity.put((Integer) row[0], (Long) row[1]);
    }
    return countByGravity;
  }

  protected long countOpenQualityImprovements(Partner partner) {
    TypedQuery<Long> query =
        JPA.em()
            .createQuery(
                "SELECT COUNT(qi.id) FROM QualityImprovement qi "
                    + "JOIN qi.qiIdentification qid "
                    + "JOIN qi.qiStatus st "
                    + "WHERE qid.supplierPartner.id = :partnerId "
                    + "AND st.isClosedStatus = false "
                    + "AND st.isCancelledStatus = false",
                Long.class);
    query.setParameter("partnerId", partner.getId());
    return query.getSingleResult();
  }
}
