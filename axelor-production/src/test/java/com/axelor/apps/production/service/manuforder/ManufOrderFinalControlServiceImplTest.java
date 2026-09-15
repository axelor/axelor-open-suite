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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlPlan;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.service.ControlEntryService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.db.Query;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ManufOrderFinalControlServiceImplTest {

  private static final long MANUF_ORDER_ID = 10L;
  private static final long PRODUCT_ID = 20L;

  private Query<ControlPlan> controlPlanQuery;
  private Query<ControlEntry> controlEntryQuery;
  private ControlEntryService controlEntryService;
  private AppQualityService appQualityService;
  private ManufOrderFinalControlService service;
  private ManufOrder manufOrder;
  private List<ControlEntry> controlEntries;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    ControlPlanRepository controlPlanRepository = mock(ControlPlanRepository.class);
    ControlEntryRepository controlEntryRepository = mock(ControlEntryRepository.class);
    controlPlanQuery = mock(Query.class);
    controlEntryQuery = mock(Query.class);
    controlEntryService = mock(ControlEntryService.class);
    appQualityService = mock(AppQualityService.class);
    service =
        new ManufOrderFinalControlServiceImpl(
            controlPlanRepository, controlEntryRepository, controlEntryService, appQualityService);

    when(appQualityService.isApp("quality")).thenReturn(true);
    when(controlPlanRepository.all()).thenReturn(controlPlanQuery);
    when(controlPlanQuery.filter(anyString())).thenReturn(controlPlanQuery);
    when(controlPlanQuery.bind(anyString(), any())).thenReturn(controlPlanQuery);
    when(controlPlanQuery.count()).thenReturn(1L);
    controlEntries = new ArrayList<>();
    when(controlEntryRepository.all()).thenReturn(controlEntryQuery);
    when(controlEntryQuery.filter(anyString())).thenReturn(controlEntryQuery);
    when(controlEntryQuery.bind(anyString(), any())).thenReturn(controlEntryQuery);
    when(controlEntryQuery.fetch()).thenReturn(controlEntries);

    Product product = new Product();
    product.setId(PRODUCT_ID);
    manufOrder = new ManufOrder();
    manufOrder.setId(MANUF_ORDER_ID);
    manufOrder.setProduct(product);
  }

  @Test
  void notRequiredWhenQualityAppIsOff() {
    when(appQualityService.isApp("quality")).thenReturn(false);

    assertEquals(Status.NOT_REQUIRED, service.evaluate(manufOrder).getStatus());
    verify(controlPlanQuery, never()).count();
  }

  @Test
  void notRequiredWithoutProduct() {
    manufOrder.setProduct(null);

    assertEquals(Status.NOT_REQUIRED, service.evaluate(manufOrder).getStatus());
  }

  @Test
  void notRequiredWithoutApplicableControlPlan() {
    when(controlPlanQuery.count()).thenReturn(0L);

    assertEquals(Status.NOT_REQUIRED, service.evaluate(manufOrder).getStatus());
    verify(controlPlanQuery).bind("productId", PRODUCT_ID);
    verify(controlPlanQuery).bind("applicableStatus", ControlPlanRepository.APPLICABLE_STATUS);
  }

  @Test
  void missingWithoutEntries() {
    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertEquals(Status.MISSING, result.getStatus());
    assertTrue(result.getUncoveredTrackingNumberSeqs().isEmpty());
    assertFalse(result.isCompliant());
  }

  @Test
  void missingWhenTheOnlyEntryIsNotFinished() {
    manufOrderEntry(
        "CE-1",
        ControlEntryRepository.IN_PROGRESS_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);

    assertEquals(Status.MISSING, service.evaluate(manufOrder).getStatus());
    verify(controlEntryService, never()).getSamplesResult(any());
  }

  @Test
  void missingWhenAFinishedEntryHasUncontrolledSamples() {
    manufOrderEntry("CE-1", ControlEntryRepository.FINISHED_STATUS, null);
    manufOrderEntry(
        "CE-2",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertEquals(Status.MISSING, result.getStatus());
    assertEquals(List.of("CE-1"), result.getNotControlledEntryNames());
  }

  @Test
  void compliantWithAFinishedCompliantManufOrderEntry() {
    manufOrderEntry(
        "CE-1",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertTrue(result.isCompliant());
    verify(controlEntryQuery).bind("manufOrderId", MANUF_ORDER_ID);
    verify(controlEntryQuery).bind("canceledStatus", ControlEntryRepository.CANCELED_STATUS);
  }

  @Test
  void nonCompliantWhenAnyFinishedEntryFails() {
    manufOrderEntry(
        "CE-1",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);
    manufOrderEntry(
        "CE-2",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertEquals(Status.NON_COMPLIANT, result.getStatus());
    assertEquals(List.of("CE-2"), result.getNonCompliantEntryNames());
  }

  @Test
  void lotClauseIsOmittedWithoutProducedLots() {
    service.evaluate(manufOrder);

    ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
    verify(controlEntryQuery).filter(filter.capture());
    assertFalse(filter.getValue().contains(":trackingNumberIds"));
    verify(controlEntryQuery, never()).bind(eq("trackingNumberIds"), any());
  }

  @Test
  void compliantWhenEveryProducedLotIsCovered() {
    TrackingNumber lot1 = producedLot(1L, "LOT-1", StockMoveRepository.STATUS_REALIZED);
    TrackingNumber lot2 = producedLot(2L, "LOT-2", StockMoveRepository.STATUS_PLANNED);
    lotEntry("CE-1", lot1, ControlEntrySampleRepository.RESULT_COMPLIANT);
    lotEntry("CE-2", lot2, ControlEntrySampleRepository.RESULT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertTrue(result.isCompliant());
    ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
    verify(controlEntryQuery).filter(filter.capture());
    assertTrue(filter.getValue().contains(":trackingNumberIds"));
    verify(controlEntryQuery).bind("trackingNumberIds", List.of(1L, 2L));
  }

  @Test
  void missingListsUncoveredLots() {
    TrackingNumber lot1 = producedLot(1L, "LOT-1", StockMoveRepository.STATUS_REALIZED);
    producedLot(2L, "LOT-2", StockMoveRepository.STATUS_PLANNED);
    lotEntry("CE-1", lot1, ControlEntrySampleRepository.RESULT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertEquals(Status.MISSING, result.getStatus());
    assertEquals(List.of("LOT-2"), result.getUncoveredTrackingNumberSeqs());
  }

  @Test
  void lotOfACanceledMoveIsIgnored() {
    TrackingNumber lot1 = producedLot(1L, "LOT-1", StockMoveRepository.STATUS_REALIZED);
    producedLot(2L, "LOT-2", StockMoveRepository.STATUS_CANCELED);
    lotEntry("CE-1", lot1, ControlEntrySampleRepository.RESULT_COMPLIANT);

    assertTrue(service.evaluate(manufOrder).isCompliant());
    verify(controlEntryQuery).bind("trackingNumberIds", List.of(1L));
  }

  @Test
  void manufOrderEntryCoversEveryLot() {
    producedLot(1L, "LOT-1", StockMoveRepository.STATUS_REALIZED);
    producedLot(2L, "LOT-2", StockMoveRepository.STATUS_PLANNED);
    manufOrderEntry(
        "CE-1",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);

    assertTrue(service.evaluate(manufOrder).isCompliant());
  }

  @Test
  void nonCompliantLotEntryWins() {
    TrackingNumber lot1 = producedLot(1L, "LOT-1", StockMoveRepository.STATUS_REALIZED);
    manufOrderEntry(
        "CE-1",
        ControlEntryRepository.FINISHED_STATUS,
        ControlEntrySampleRepository.RESULT_COMPLIANT);
    lotEntry("CE-2", lot1, ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);

    ManufOrderFinalControlResult result = service.evaluate(manufOrder);

    assertEquals(Status.NON_COMPLIANT, result.getStatus());
    assertEquals(List.of("CE-2"), result.getNonCompliantEntryNames());
  }

  private TrackingNumber producedLot(long id, String seq, int stockMoveStatus) {
    TrackingNumber trackingNumber = new TrackingNumber();
    trackingNumber.setId(id);
    trackingNumber.setTrackingNumberSeq(seq);
    StockMove stockMove = new StockMove();
    stockMove.setStatusSelect(stockMoveStatus);
    StockMoveLine line = new StockMoveLine();
    line.setStockMove(stockMove);
    line.setTrackingNumber(trackingNumber);
    manufOrder.addProducedStockMoveLineListItem(line);
    return trackingNumber;
  }

  private void manufOrderEntry(String name, int status, Integer samplesResult) {
    entry(name, status, ManufOrder.class.getName(), MANUF_ORDER_ID, samplesResult);
  }

  private void lotEntry(String name, TrackingNumber trackingNumber, Integer samplesResult) {
    entry(
        name,
        ControlEntryRepository.FINISHED_STATUS,
        TrackingNumber.class.getName(),
        trackingNumber.getId(),
        samplesResult);
  }

  private void entry(
      String name,
      int status,
      String relatedToSelect,
      Long relatedToSelectId,
      Integer samplesResult) {
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.setName(name);
    controlEntry.setStatusSelect(status);
    controlEntry.setRelatedToSelect(relatedToSelect);
    controlEntry.setRelatedToSelectId(relatedToSelectId);
    controlEntries.add(controlEntry);
    when(controlEntryService.getSamplesResult(controlEntry)).thenReturn(samplesResult);
  }
}
