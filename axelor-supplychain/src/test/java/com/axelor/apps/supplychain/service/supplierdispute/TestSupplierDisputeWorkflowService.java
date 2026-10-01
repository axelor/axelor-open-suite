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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestSupplierDisputeWorkflowService {

  protected static final LocalDateTime TODAY = LocalDateTime.of(2026, 9, 23, 10, 30);

  protected SupplierDisputeWorkflowService supplierDisputeWorkflowService;

  @BeforeEach
  void setUp() {
    SupplierDisputeRepository supplierDisputeRepository = mock(SupplierDisputeRepository.class);
    when(supplierDisputeRepository.save(any(SupplierDispute.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    AppBaseService appBaseService = mock(AppBaseService.class);
    when(appBaseService.getTodayDateTime(any()))
        .thenReturn(ZonedDateTime.of(TODAY, ZoneOffset.UTC));
    supplierDisputeWorkflowService =
        new SupplierDisputeWorkflowServiceImpl(supplierDisputeRepository, appBaseService);
  }

  static Stream<Arguments> allowedTransitions() {
    return Stream.of(
        Arguments.of(SupplierDisputeRepository.STATUS_OPEN, "investigate"),
        Arguments.of(SupplierDisputeRepository.STATUS_OPEN, "awaitSupplierResponse"),
        Arguments.of(SupplierDisputeRepository.STATUS_OPEN, "cancel"),
        Arguments.of(SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION, "awaitSupplierResponse"),
        Arguments.of(SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION, "resolve"),
        Arguments.of(SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION, "cancel"),
        Arguments.of(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE, "investigate"),
        Arguments.of(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE, "resolve"),
        Arguments.of(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE, "cancel"),
        Arguments.of(SupplierDisputeRepository.STATUS_RESOLVED, "close"));
  }

  static Stream<Arguments> forbiddenTransitions() {
    List<Integer> statuses =
        List.of(
            SupplierDisputeRepository.STATUS_OPEN,
            SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
            SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE,
            SupplierDisputeRepository.STATUS_RESOLVED,
            SupplierDisputeRepository.STATUS_CLOSED,
            SupplierDisputeRepository.STATUS_CANCELLED);
    List<String> transitions =
        List.of("investigate", "awaitSupplierResponse", "resolve", "close", "cancel");
    List<Arguments> allowed = allowedTransitions().toList();
    return statuses.stream()
        .flatMap(status -> transitions.stream().map(transition -> Arguments.of(status, transition)))
        .filter(
            arguments ->
                allowed.stream()
                    .noneMatch(
                        allowedArguments ->
                            allowedArguments.get()[0].equals(arguments.get()[0])
                                && allowedArguments.get()[1].equals(arguments.get()[1])));
  }

  @ParameterizedTest
  @MethodSource("allowedTransitions")
  void testAllowedTransitionStampsStatusUserAndDate(int fromStatus, String transition)
      throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(fromStatus);
    supplierDispute.setResolutionOutcomeSelect(SupplierDisputeRepository.OUTCOME_CREDIT_NOTE);

    apply(supplierDispute, transition);

    switch (transition) {
      case "investigate":
        assertEquals(
            SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
            supplierDispute.getStatusSelect());
        assertEquals(TODAY, supplierDispute.getInvestigationDateTime());
        break;
      case "awaitSupplierResponse":
        assertEquals(
            SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE,
            supplierDispute.getStatusSelect());
        assertEquals(TODAY, supplierDispute.getAwaitingResponseDateTime());
        break;
      case "resolve":
        assertEquals(SupplierDisputeRepository.STATUS_RESOLVED, supplierDispute.getStatusSelect());
        assertEquals(TODAY, supplierDispute.getResolutionDateTime());
        break;
      case "close":
        assertEquals(SupplierDisputeRepository.STATUS_CLOSED, supplierDispute.getStatusSelect());
        assertEquals(TODAY, supplierDispute.getClosingDateTime());
        break;
      case "cancel":
        assertEquals(SupplierDisputeRepository.STATUS_CANCELLED, supplierDispute.getStatusSelect());
        assertEquals(TODAY, supplierDispute.getCancellationDateTime());
        break;
      default:
        throw new IllegalArgumentException(transition);
    }
  }

  @ParameterizedTest
  @MethodSource("forbiddenTransitions")
  void testForbiddenTransitionThrowsInconsistency(int fromStatus, String transition) {
    SupplierDispute supplierDispute = createSupplierDispute(fromStatus);
    supplierDispute.setResolutionOutcomeSelect(SupplierDisputeRepository.OUTCOME_CREDIT_NOTE);

    AxelorException exception =
        assertThrows(AxelorException.class, () -> apply(supplierDispute, transition));

    assertEquals(TraceBackRepository.CATEGORY_INCONSISTENCY, exception.getCategory());
    assertEquals(fromStatus, supplierDispute.getStatusSelect());
  }

  @Test
  void testResolveWithoutOutcomeThrowsMissingField() {
    SupplierDispute supplierDispute =
        createSupplierDispute(SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION);

    AxelorException exception =
        assertThrows(
            AxelorException.class, () -> supplierDisputeWorkflowService.resolve(supplierDispute));

    assertEquals(TraceBackRepository.CATEGORY_MISSING_FIELD, exception.getCategory());
    assertEquals(
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION, supplierDispute.getStatusSelect());
  }

  @Test
  void testResolveWithOutcomeSucceeds() throws AxelorException {
    SupplierDispute supplierDispute =
        createSupplierDispute(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE);
    supplierDispute.setResolutionOutcomeSelect(SupplierDisputeRepository.OUTCOME_REPLACEMENT);

    supplierDisputeWorkflowService.resolve(supplierDispute);

    assertEquals(SupplierDisputeRepository.STATUS_RESOLVED, supplierDispute.getStatusSelect());
    assertEquals(TODAY, supplierDispute.getResolutionDateTime());
  }

  protected void apply(SupplierDispute supplierDispute, String transition) throws AxelorException {
    switch (transition) {
      case "investigate":
        supplierDisputeWorkflowService.investigate(supplierDispute);
        break;
      case "awaitSupplierResponse":
        supplierDisputeWorkflowService.awaitSupplierResponse(supplierDispute);
        break;
      case "resolve":
        supplierDisputeWorkflowService.resolve(supplierDispute);
        break;
      case "close":
        supplierDisputeWorkflowService.close(supplierDispute);
        break;
      case "cancel":
        supplierDisputeWorkflowService.cancel(supplierDispute);
        break;
      default:
        throw new IllegalArgumentException(transition);
    }
  }

  protected SupplierDispute createSupplierDispute(int statusSelect) {
    SupplierDispute supplierDispute = new SupplierDispute();
    supplierDispute.setDisputeSeq("SD260001");
    supplierDispute.setStatusSelect(statusSelect);
    return supplierDispute;
  }
}
