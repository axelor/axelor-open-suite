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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.service.ControlEntryService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.db.Query;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ManufOrderFinalControlServiceImpl implements ManufOrderFinalControlService {

  protected final ControlPlanRepository controlPlanRepository;
  protected final ControlEntryRepository controlEntryRepository;
  protected final ControlEntryService controlEntryService;
  protected final AppQualityService appQualityService;

  @Inject
  public ManufOrderFinalControlServiceImpl(
      ControlPlanRepository controlPlanRepository,
      ControlEntryRepository controlEntryRepository,
      ControlEntryService controlEntryService,
      AppQualityService appQualityService) {
    this.controlPlanRepository = controlPlanRepository;
    this.controlEntryRepository = controlEntryRepository;
    this.controlEntryService = controlEntryService;
    this.appQualityService = appQualityService;
  }

  @Override
  public ManufOrderFinalControlResult evaluate(ManufOrder manufOrder) {
    Objects.requireNonNull(manufOrder);

    if (!isFinalControlApplicable(manufOrder)) {
      return new ManufOrderFinalControlResult(Status.NOT_REQUIRED);
    }
    Set<TrackingNumber> producedTrackingNumbers = getProducedTrackingNumbers(manufOrder);
    List<ControlEntry> controlEntries =
        findFinalControlEntries(manufOrder, producedTrackingNumbers);
    return computeResult(controlEntries, producedTrackingNumbers);
  }

  protected boolean isFinalControlApplicable(ManufOrder manufOrder) {
    return appQualityService.isApp("quality")
        && manufOrder.getProduct() != null
        && hasApplicableControlPlan(manufOrder.getProduct());
  }

  protected boolean hasApplicableControlPlan(Product product) {
    return controlPlanRepository
            .all()
            .filter(
                "self.relatedToSelect = :relatedToSelect"
                    + " AND self.relatedToSelectId = :productId"
                    + " AND self.statusSelect = :applicableStatus")
            .bind("relatedToSelect", Product.class.getName())
            .bind("productId", product.getId())
            .bind("applicableStatus", ControlPlanRepository.APPLICABLE_STATUS)
            .count()
        > 0;
  }

  protected Set<TrackingNumber> getProducedTrackingNumbers(ManufOrder manufOrder) {
    if (manufOrder.getProducedStockMoveLineList() == null) {
      return Set.of();
    }
    return manufOrder.getProducedStockMoveLineList().stream()
        .filter(line -> !isCanceled(line))
        .map(StockMoveLine::getTrackingNumber)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  protected boolean isCanceled(StockMoveLine stockMoveLine) {
    return stockMoveLine.getStockMove() != null
        && stockMoveLine.getStockMove().getStatusSelect() == StockMoveRepository.STATUS_CANCELED;
  }

  protected List<ControlEntry> findFinalControlEntries(
      ManufOrder manufOrder, Set<TrackingNumber> producedTrackingNumbers) {
    String filter =
        "self.statusSelect != :canceledStatus"
            + " AND self.controlPlan.relatedToSelect = :productRelatedToSelect"
            + " AND self.controlPlan.relatedToSelectId = :productId"
            + " AND ((self.relatedToSelect = :manufOrderRelatedToSelect"
            + " AND self.relatedToSelectId = :manufOrderId)";
    if (!producedTrackingNumbers.isEmpty()) {
      filter +=
          " OR (self.relatedToSelect = :trackingNumberRelatedToSelect"
              + " AND self.relatedToSelectId IN (:trackingNumberIds))";
    }
    filter += ")";

    Query<ControlEntry> query =
        controlEntryRepository
            .all()
            .filter(filter)
            .bind("canceledStatus", ControlEntryRepository.CANCELED_STATUS)
            .bind("productRelatedToSelect", Product.class.getName())
            .bind("productId", manufOrder.getProduct().getId())
            .bind("manufOrderRelatedToSelect", ManufOrder.class.getName())
            .bind("manufOrderId", manufOrder.getId());
    if (!producedTrackingNumbers.isEmpty()) {
      query
          .bind("trackingNumberRelatedToSelect", TrackingNumber.class.getName())
          .bind(
              "trackingNumberIds",
              producedTrackingNumbers.stream()
                  .map(TrackingNumber::getId)
                  .collect(Collectors.toList()));
    }
    return query.fetch();
  }

  protected ManufOrderFinalControlResult computeResult(
      List<ControlEntry> controlEntries, Set<TrackingNumber> producedTrackingNumbers) {
    List<String> nonCompliantEntryNames = new ArrayList<>();
    List<String> notControlledEntryNames = new ArrayList<>();
    Set<Long> coveredTrackingNumberIds = new HashSet<>();
    boolean manufOrderCovered = false;

    for (ControlEntry controlEntry : controlEntries) {
      if (controlEntry.getStatusSelect() != ControlEntryRepository.FINISHED_STATUS) {
        continue;
      }
      Integer samplesResult = controlEntryService.getSamplesResult(controlEntry);
      if (samplesResult == null) {
        notControlledEntryNames.add(controlEntry.getName());
      } else if (samplesResult == ControlEntrySampleRepository.RESULT_NOT_COMPLIANT) {
        nonCompliantEntryNames.add(controlEntry.getName());
      } else if (ManufOrder.class.getName().equals(controlEntry.getRelatedToSelect())) {
        manufOrderCovered = true;
      } else {
        coveredTrackingNumberIds.add(controlEntry.getRelatedToSelectId());
      }
    }

    if (!nonCompliantEntryNames.isEmpty()) {
      return new ManufOrderFinalControlResult(
          Status.NON_COMPLIANT, nonCompliantEntryNames, List.of(), List.of());
    }
    if (!notControlledEntryNames.isEmpty()) {
      return new ManufOrderFinalControlResult(
          Status.MISSING, List.of(), notControlledEntryNames, List.of());
    }
    if (manufOrderCovered) {
      return new ManufOrderFinalControlResult(Status.COMPLIANT);
    }
    List<String> uncoveredTrackingNumberSeqs =
        producedTrackingNumbers.stream()
            .filter(trackingNumber -> !coveredTrackingNumberIds.contains(trackingNumber.getId()))
            .map(TrackingNumber::getTrackingNumberSeq)
            .collect(Collectors.toList());
    if (!producedTrackingNumbers.isEmpty() && uncoveredTrackingNumberSeqs.isEmpty()) {
      return new ManufOrderFinalControlResult(Status.COMPLIANT);
    }
    return new ManufOrderFinalControlResult(
        Status.MISSING, List.of(), List.of(), uncoveredTrackingNumberSeqs);
  }
}
