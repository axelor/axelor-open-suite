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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class SupplierScoreServiceImpl implements SupplierScoreService {

  protected static final String PURCHASE_ORDER_LINE_FILTER =
      "po.supplierPartner.id = :partnerId "
          + "AND po.statusSelect IN (:statusList) "
          + "AND pol.isTitleLine = false "
          + "AND pol.product IS NOT NULL "
          + "AND COALESCE(pol.estimatedReceiptDate, po.estimatedReceiptDate) "
          + "BETWEEN :fromDate AND :toDate";

  protected static final String RECEPTION_FILTER =
      "sm.partner.id = :partnerId "
          + "AND sm.typeSelect = :incomingType "
          + "AND sm.statusSelect = :realizedStatus "
          + "AND sm.isReversion = false "
          + "AND sm.realDate BETWEEN :fromDate AND :toDate";

  protected final PartnerRepository partnerRepository;
  protected final AppSupplychainService appSupplychainService;
  protected final AppBaseService appBaseService;

  @Inject
  public SupplierScoreServiceImpl(
      PartnerRepository partnerRepository,
      AppSupplychainService appSupplychainService,
      AppBaseService appBaseService) {
    this.partnerRepository = partnerRepository;
    this.appSupplychainService = appSupplychainService;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void computeAndSave(Partner partner) {
    if (partner == null || partner.getId() == null || !isSupplierScoreEnabled()) {
      return;
    }
    partner = partnerRepository.find(partner.getId());
    if (partner == null || !Boolean.TRUE.equals(partner.getIsSupplier())) {
      return;
    }

    LocalDate toDate = appBaseService.getTodayDate(null);
    LocalDate fromDate =
        toDate.minusMonths(
            appSupplychainService.getAppSupplychain().getSupplierScoreRollingMonths());

    computeRates(partner, fromDate, toDate);
    partner.setSupplierScore(computeGlobalScore(partner));
    partner.setSupplierScoreComputedOn(toDate);
    partnerRepository.save(partner);
  }

  @Override
  public BigDecimal computeGlobalScore(Partner partner) {
    return SupplierScoreTool.computeWeightedAverage(
        getWeightedRates(partner, appSupplychainService.getAppSupplychain()));
  }

  protected boolean isSupplierScoreEnabled() {
    return appSupplychainService.isApp("supplychain")
        && Boolean.TRUE.equals(appSupplychainService.getAppSupplychain().getManageSupplierScore());
  }

  protected void computeRates(Partner partner, LocalDate fromDate, LocalDate toDate) {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    if (Boolean.TRUE.equals(appSupplychain.getAutoComputeSupplierOtdRate())) {
      partner.setSupplierOtdRate(computeOtdRate(partner, fromDate, toDate));
    }
    if (Boolean.TRUE.equals(appSupplychain.getAutoComputeSupplierConformityRate())) {
      partner.setSupplierConformityRate(computeConformityRate(partner, fromDate, toDate));
    }
  }

  protected List<Pair<BigDecimal, BigDecimal>> getWeightedRates(
      Partner partner, AppSupplychain appSupplychain) {
    List<Pair<BigDecimal, BigDecimal>> weightedRates = new ArrayList<>();
    weightedRates.add(
        Pair.of(partner.getSupplierOtdRate(), appSupplychain.getSupplierScoreOtdWeight()));
    weightedRates.add(
        Pair.of(
            partner.getSupplierConformityRate(),
            appSupplychain.getSupplierScoreConformityWeight()));
    return weightedRates;
  }

  protected BigDecimal computeOtdRate(Partner partner, LocalDate fromDate, LocalDate toDate) {
    long dueLineCount = countDuePurchaseOrderLines(partner, fromDate, toDate);
    if (dueLineCount == 0) {
      return null;
    }
    long onTimeLineCount =
        fetchReceivedPurchaseOrderLines(partner, fromDate, toDate).stream()
            .filter(row -> SupplierScoreTool.isOnTime((LocalDate) row[1], (LocalDate) row[2]))
            .count();
    return SupplierScoreTool.computeRate(onTimeLineCount, dueLineCount);
  }

  protected BigDecimal computeConformityRate(
      Partner partner, LocalDate fromDate, LocalDate toDate) {
    long receptionCount = countReceptions(partner, fromDate, toDate);
    if (receptionCount == 0) {
      return null;
    }
    long nonConformReceptionCount = countNonConformReceptions(partner, fromDate, toDate);
    return SupplierScoreTool.computeRate(receptionCount - nonConformReceptionCount, receptionCount);
  }

  protected long countDuePurchaseOrderLines(Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Long> query =
        JPA.em()
            .createQuery(
                "SELECT COUNT(pol.id) FROM PurchaseOrderLine pol JOIN pol.purchaseOrder po WHERE "
                    + PURCHASE_ORDER_LINE_FILTER,
                Long.class);
    bindPurchaseOrderLineFilter(query, partner, fromDate, toDate);
    return query.getSingleResult();
  }

  /**
   * @return one row per fully received purchase order line due in the period: line id, estimated
   *     receipt date, date of its last realized receipt.
   */
  protected List<Object[]> fetchReceivedPurchaseOrderLines(
      Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Object[]> query =
        JPA.em()
            .createQuery(
                "SELECT pol.id, "
                    + "COALESCE(pol.estimatedReceiptDate, po.estimatedReceiptDate), "
                    + "MAX(sm.realDate) "
                    + "FROM StockMoveLine sml "
                    + "JOIN sml.purchaseOrderLine pol "
                    + "JOIN pol.purchaseOrder po "
                    + "JOIN sml.stockMove sm "
                    + "WHERE "
                    + PURCHASE_ORDER_LINE_FILTER
                    + " AND pol.receiptState = :receivedState "
                    + "AND sm.typeSelect = :incomingType "
                    + "AND sm.statusSelect = :realizedStatus "
                    + "AND sm.isReversion = false "
                    + "AND sml.lineTypeSelect = :normalLineType "
                    + "AND sml.realQty > 0 "
                    + "GROUP BY pol.id, COALESCE(pol.estimatedReceiptDate, po.estimatedReceiptDate)",
                Object[].class);
    bindPurchaseOrderLineFilter(query, partner, fromDate, toDate);
    query.setParameter("receivedState", PurchaseOrderLineRepository.RECEIPT_STATE_RECEIVED);
    query.setParameter("incomingType", StockMoveRepository.TYPE_INCOMING);
    query.setParameter("realizedStatus", StockMoveRepository.STATUS_REALIZED);
    query.setParameter("normalLineType", StockMoveLineRepository.TYPE_NORMAL);
    return query.getResultList();
  }

  protected long countReceptions(Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Long> query =
        JPA.em()
            .createQuery(
                "SELECT COUNT(sm.id) FROM StockMove sm WHERE " + RECEPTION_FILTER, Long.class);
    bindReceptionFilter(query, partner, fromDate, toDate);
    return query.getSingleResult();
  }

  protected long countNonConformReceptions(Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Long> query =
        JPA.em()
            .createQuery(
                "SELECT COUNT(DISTINCT sm.id) FROM StockMoveLine sml JOIN sml.stockMove sm WHERE "
                    + RECEPTION_FILTER
                    + " AND sml.conformitySelect = :nonCompliant",
                Long.class);
    bindReceptionFilter(query, partner, fromDate, toDate);
    query.setParameter("nonCompliant", StockMoveLineRepository.CONFORMITY_NON_COMPLIANT);
    return query.getSingleResult();
  }

  protected void bindPurchaseOrderLineFilter(
      TypedQuery<?> query, Partner partner, LocalDate fromDate, LocalDate toDate) {
    query.setParameter("partnerId", partner.getId());
    query.setParameter(
        "statusList",
        List.of(PurchaseOrderRepository.STATUS_VALIDATED, PurchaseOrderRepository.STATUS_FINISHED));
    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);
  }

  protected void bindReceptionFilter(
      TypedQuery<?> query, Partner partner, LocalDate fromDate, LocalDate toDate) {
    query.setParameter("partnerId", partner.getId());
    query.setParameter("incomingType", StockMoveRepository.TYPE_INCOMING);
    query.setParameter("realizedStatus", StockMoveRepository.STATUS_REALIZED);
    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);
  }
}
