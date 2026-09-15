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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.QualityConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;

public class ManufOrderConformityDeclarationPrintServiceImpl
    implements ManufOrderConformityDeclarationPrintService {

  protected final ManufOrderFinalControlService manufOrderFinalControlService;
  protected final ManufOrderFinalControlCheckService manufOrderFinalControlCheckService;
  protected final QualityConfigProductionService qualityConfigProductionService;
  protected final PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public ManufOrderConformityDeclarationPrintServiceImpl(
      ManufOrderFinalControlService manufOrderFinalControlService,
      ManufOrderFinalControlCheckService manufOrderFinalControlCheckService,
      QualityConfigProductionService qualityConfigProductionService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    this.manufOrderFinalControlService = manufOrderFinalControlService;
    this.manufOrderFinalControlCheckService = manufOrderFinalControlCheckService;
    this.qualityConfigProductionService = qualityConfigProductionService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  public File print(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_CONFORMITY_DECLARATION_NOT_FINISHED));
    }
    ManufOrderFinalControlResult result = manufOrderFinalControlService.evaluate(manufOrder);
    if (!result.isCompliant()) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_CONFORMITY_DECLARATION_REFUSED),
          getRefusalReason(manufOrder, result));
    }
    PrintingTemplate printTemplate =
        qualityConfigProductionService.getManufOrderConformityDeclarationPrintTemplate(
            manufOrder.getCompany());
    return printingTemplatePrintService.getPrintFile(
        printTemplate, new PrintingGenFactoryContext(manufOrder));
  }

  @Override
  public String getPrintLink(ManufOrder manufOrder) throws AxelorException, IOException {
    return PrintingTemplateHelper.getFileLink(print(manufOrder));
  }

  protected String getRefusalReason(ManufOrder manufOrder, ManufOrderFinalControlResult result) {
    if (result.getStatus() == Status.NOT_REQUIRED) {
      return String.format(
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_CONFORMITY_DECLARATION_NOT_REQUIRED),
          manufOrder.getManufOrderSeq());
    }
    return manufOrderFinalControlCheckService.getIssueMessage(manufOrder, result);
  }
}
