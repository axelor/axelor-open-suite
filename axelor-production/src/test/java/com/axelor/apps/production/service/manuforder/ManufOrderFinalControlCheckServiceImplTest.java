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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.config.QualityConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import com.axelor.apps.quality.db.repo.QualityConfigRepository;
import com.axelor.auth.db.User;
import com.axelor.message.service.MailMessageService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ManufOrderFinalControlCheckServiceImplTest {

  private static final long MANUF_ORDER_ID = 10L;

  private ManufOrderFinalControlService finalControlService;
  private QualityConfigProductionService qualityConfigProductionService;
  private MailMessageService mailMessageService;
  private ManufOrderFinalControlCheckService service;
  private ManufOrder manufOrder;
  private User user;

  @BeforeEach
  void setUp() {
    finalControlService = mock(ManufOrderFinalControlService.class);
    qualityConfigProductionService = mock(QualityConfigProductionService.class);
    mailMessageService = mock(MailMessageService.class);
    UserService userService = mock(UserService.class);
    service =
        new ManufOrderFinalControlCheckServiceImpl(
            finalControlService, qualityConfigProductionService, mailMessageService, userService);

    Company company = new Company();
    manufOrder = new ManufOrder();
    manufOrder.setId(MANUF_ORDER_ID);
    manufOrder.setManufOrderSeq("MO-1");
    manufOrder.setCompany(company);
    user = new User();
    when(userService.getUser()).thenReturn(user);
    mode(QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_WARNING);
    result(new ManufOrderFinalControlResult(Status.MISSING));
  }

  @Test
  void noCheckModeDoesNothing() throws AxelorException {
    mode(QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE);

    assertNull(service.getFinishIssueMessage(manufOrder));
    service.checkBeforeFinish(manufOrder);

    verify(finalControlService, never()).evaluate(any());
    verifyNoNotification();
  }

  @Test
  void compliantOrNotRequiredResultDoesNothing() throws AxelorException {
    mode(QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_BLOCKING);

    result(new ManufOrderFinalControlResult(Status.COMPLIANT));
    assertNull(service.getFinishIssueMessage(manufOrder));
    service.checkBeforeFinish(manufOrder);

    result(new ManufOrderFinalControlResult(Status.NOT_REQUIRED));
    assertNull(service.getFinishIssueMessage(manufOrder));
    service.checkBeforeFinish(manufOrder);

    verifyNoNotification();
  }

  @Test
  void missingControlMessageNamesTheManufOrderAndTheUncoveredLots() {
    result(
        new ManufOrderFinalControlResult(
            Status.MISSING, List.of(), List.of(), List.of("LOT-1", "LOT-2")));

    String issue = service.getFinishIssueMessage(manufOrder);

    assertNotNull(issue);
    assertTrue(issue.contains("MO-1"));
    assertTrue(issue.contains("LOT-1, LOT-2"));
  }

  @Test
  void notControlledMessageNamesTheEntries() {
    result(new ManufOrderFinalControlResult(Status.MISSING, List.of(), List.of("CE-3"), List.of()));

    String issue = service.getFinishIssueMessage(manufOrder);

    assertTrue(issue.contains("MO-1"));
    assertTrue(issue.contains("CE-3"));
  }

  @Test
  void nonCompliantMessageNamesTheEntries() {
    result(
        new ManufOrderFinalControlResult(
            Status.NON_COMPLIANT, List.of("CE-1", "CE-2"), List.of(), List.of()));

    String issue = service.getFinishIssueMessage(manufOrder);

    assertTrue(issue.contains("MO-1"));
    assertTrue(issue.contains("CE-1, CE-2"));
  }

  @Test
  void warningModeNotifiesTheUserOnTheManufOrder() throws AxelorException {
    service.checkBeforeFinish(manufOrder);

    ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
    verify(mailMessageService)
        .sendNotification(
            eq(user), anyString(), body.capture(), eq(MANUF_ORDER_ID), eq(ManufOrder.class));
    assertTrue(body.getValue().contains("MO-1"));
  }

  @Test
  void blockingModeThrowsWithTheIssue() {
    mode(QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_BLOCKING);
    result(
        new ManufOrderFinalControlResult(
            Status.NON_COMPLIANT, List.of("CE-1"), List.of(), List.of()));

    AxelorException exception =
        assertThrows(AxelorException.class, () -> service.checkBeforeFinish(manufOrder));

    assertEquals(TraceBackRepository.CATEGORY_INCONSISTENCY, exception.getCategory());
    assertTrue(exception.getMessage().contains("CE-1"));
    verifyNoNotification();
  }

  @Test
  void operationIssueOnlyForTheLastOperationToFinish() {
    OperationOrder finished = operationOrder(OperationOrderRepository.STATUS_FINISHED);
    OperationOrder inProgress = operationOrder(OperationOrderRepository.STATUS_IN_PROGRESS);
    OperationOrder planned = operationOrder(OperationOrderRepository.STATUS_PLANNED);

    assertNull(service.getFinishIssueMessage(inProgress));

    manufOrder.removeOperationOrderListItem(planned);
    assertNotNull(service.getFinishIssueMessage(inProgress));
    assertNull(service.getFinishIssueMessage(finished));
  }

  @Test
  void operationIssueIsNullWithoutManufOrder() {
    assertNull(service.getFinishIssueMessage(new OperationOrder()));
    assertDoesNotThrow(() -> service.getFinishIssueMessage(new OperationOrder()));
  }

  private void mode(int checkSelect) {
    when(qualityConfigProductionService.getManufOrderFinalControlCheckSelect(
            manufOrder.getCompany()))
        .thenReturn(checkSelect);
  }

  private void result(ManufOrderFinalControlResult result) {
    when(finalControlService.evaluate(manufOrder)).thenReturn(result);
  }

  private OperationOrder operationOrder(int status) {
    OperationOrder operationOrder = new OperationOrder();
    operationOrder.setStatusSelect(status);
    manufOrder.addOperationOrderListItem(operationOrder);
    return operationOrder;
  }

  private void verifyNoNotification() {
    verify(mailMessageService, never())
        .sendNotification(any(), anyString(), anyString(), anyLong(), any());
  }
}
