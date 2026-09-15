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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.config.QualityConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ManufOrderConformityDeclarationPrintServiceImplTest {

  private ManufOrderFinalControlService finalControlService;
  private ManufOrderFinalControlCheckService finalControlCheckService;
  private QualityConfigProductionService qualityConfigProductionService;
  private PrintingTemplatePrintService printingTemplatePrintService;
  private ManufOrderConformityDeclarationPrintService service;
  private ManufOrder manufOrder;
  private PrintingTemplate printTemplate;

  @BeforeEach
  void setUp() throws AxelorException {
    finalControlService = mock(ManufOrderFinalControlService.class);
    finalControlCheckService = mock(ManufOrderFinalControlCheckService.class);
    qualityConfigProductionService = mock(QualityConfigProductionService.class);
    printingTemplatePrintService = mock(PrintingTemplatePrintService.class);
    service =
        new ManufOrderConformityDeclarationPrintServiceImpl(
            finalControlService,
            finalControlCheckService,
            qualityConfigProductionService,
            printingTemplatePrintService);

    Company company = new Company();
    manufOrder = new ManufOrder();
    manufOrder.setManufOrderSeq("MO-1");
    manufOrder.setCompany(company);
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_FINISHED);
    printTemplate = new PrintingTemplate();
    when(qualityConfigProductionService.getManufOrderConformityDeclarationPrintTemplate(company))
        .thenReturn(printTemplate);
    when(finalControlService.evaluate(manufOrder))
        .thenReturn(new ManufOrderFinalControlResult(Status.COMPLIANT));
  }

  @Test
  void printRefusesAnUnfinishedManufOrder() {
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);

    AxelorException exception =
        assertThrows(AxelorException.class, () -> service.print(manufOrder));

    assertEquals(TraceBackRepository.CATEGORY_INCONSISTENCY, exception.getCategory());
    verify(finalControlService, never()).evaluate(any());
  }

  @Test
  void printRefusesAMissingOrNonCompliantControlWithTheReason() throws AxelorException {
    ManufOrderFinalControlResult result =
        new ManufOrderFinalControlResult(
            Status.NON_COMPLIANT, List.of("CE-1"), List.of(), List.of());
    when(finalControlService.evaluate(manufOrder)).thenReturn(result);
    when(finalControlCheckService.getIssueMessage(manufOrder, result)).thenReturn("CE-1 failed");

    AxelorException exception =
        assertThrows(AxelorException.class, () -> service.print(manufOrder));

    assertTrue(exception.getMessage().contains("CE-1 failed"));
    verify(printingTemplatePrintService, never()).getPrintFile(any(), any());
  }

  @Test
  void printRefusesWhenNoFinalControlIsRequired() {
    when(finalControlService.evaluate(manufOrder))
        .thenReturn(new ManufOrderFinalControlResult(Status.NOT_REQUIRED));

    AxelorException exception =
        assertThrows(AxelorException.class, () -> service.print(manufOrder));

    assertTrue(exception.getMessage().contains("MO-1"));
    verify(finalControlCheckService, never()).getIssueMessage(any(), any());
  }

  @Test
  void printUsesTheConfiguredTemplateOnTheManufOrder() throws AxelorException {
    File file = new File("declaration.pdf");
    when(printingTemplatePrintService.getPrintFile(eq(printTemplate), any())).thenReturn(file);

    assertSame(file, service.print(manufOrder));

    ArgumentCaptor<PrintingGenFactoryContext> context =
        ArgumentCaptor.forClass(PrintingGenFactoryContext.class);
    verify(printingTemplatePrintService).getPrintFile(eq(printTemplate), context.capture());
    assertSame(manufOrder, context.getValue().getModel());
  }
}
