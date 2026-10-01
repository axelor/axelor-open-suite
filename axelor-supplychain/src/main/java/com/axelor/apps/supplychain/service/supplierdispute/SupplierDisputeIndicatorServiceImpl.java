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
package com.axelor.apps.supplychain.service.supplierdispute;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.apps.supplychain.service.SupplierScoreTool;
import com.axelor.db.JPA;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class SupplierDisputeIndicatorServiceImpl implements SupplierDisputeIndicatorService {

  protected static final String CLOSED_DISPUTE_FILTER =
      "sd.supplierPartner.id = :partnerId "
          + "AND sd.statusSelect = :closedStatus "
          + "AND DATE(sd.closingDateTime) >= :fromDate "
          + "AND DATE(sd.closingDateTime) <= :toDate";

  @Override
  public void computeIndicators(Partner partner, LocalDate fromDate, LocalDate toDate) {
    List<Object[]> closedDisputes = fetchClosedDisputes(partner, fromDate, toDate);
    long closedCount = closedDisputes.size();
    long favourableCount =
        closedDisputes.stream()
            .filter(row -> SupplierDisputeIndicatorTool.isFavourable((Integer) row[2]))
            .count();
    List<Pair<LocalDateTime, LocalDateTime>> openingClosingPairs =
        closedDisputes.stream()
            .map(row -> Pair.of((LocalDateTime) row[0], (LocalDateTime) row[1]))
            .toList();

    partner.setSupplierClosedDisputeCount((int) closedCount);
    partner.setSupplierDisputeFavourableRate(
        SupplierScoreTool.computeRate(favourableCount, closedCount));
    partner.setSupplierDisputeAvgResolutionDays(
        SupplierDisputeIndicatorTool.computeAverageResolutionDays(openingClosingPairs));
  }

  /**
   * @return one row per dispute of the supplier closed in the period: opening date time, closing
   *     date time, resolution outcome.
   */
  protected List<Object[]> fetchClosedDisputes(
      Partner partner, LocalDate fromDate, LocalDate toDate) {
    TypedQuery<Object[]> query =
        JPA.em()
            .createQuery(
                "SELECT sd.openingDateTime, sd.closingDateTime, sd.resolutionOutcomeSelect "
                    + "FROM SupplierDispute sd WHERE "
                    + CLOSED_DISPUTE_FILTER,
                Object[].class);
    query.setParameter("partnerId", partner.getId());
    query.setParameter("closedStatus", SupplierDisputeRepository.STATUS_CLOSED);
    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);
    return query.getResultList();
  }
}
