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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.QualityConfigProductionService;
import com.axelor.apps.quality.db.repo.QualityConfigRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.message.service.MailMessageService;
import jakarta.inject.Inject;
import java.util.List;

public class ManufOrderFinalControlCheckServiceImpl implements ManufOrderFinalControlCheckService {

  protected final ManufOrderFinalControlService manufOrderFinalControlService;
  protected final QualityConfigProductionService qualityConfigProductionService;
  protected final MailMessageService mailMessageService;
  protected final UserService userService;

  @Inject
  public ManufOrderFinalControlCheckServiceImpl(
      ManufOrderFinalControlService manufOrderFinalControlService,
      QualityConfigProductionService qualityConfigProductionService,
      MailMessageService mailMessageService,
      UserService userService) {
    this.manufOrderFinalControlService = manufOrderFinalControlService;
    this.qualityConfigProductionService = qualityConfigProductionService;
    this.mailMessageService = mailMessageService;
    this.userService = userService;
  }

  @Override
  public int getCheckSelect(ManufOrder manufOrder) {
    return qualityConfigProductionService.getManufOrderFinalControlCheckSelect(
        manufOrder.getCompany());
  }

  @Override
  public String getFinishIssueMessage(ManufOrder manufOrder) {
    if (getCheckSelect(manufOrder)
        == QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE) {
      return null;
    }
    return getIssueMessage(manufOrder, manufOrderFinalControlService.evaluate(manufOrder));
  }

  @Override
  public String getFinishIssueMessage(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder == null || !isLastOperationToFinish(operationOrder, manufOrder)) {
      return null;
    }
    return getFinishIssueMessage(manufOrder);
  }

  @Override
  public void checkBeforeFinish(ManufOrder manufOrder) throws AxelorException {
    int checkSelect = getCheckSelect(manufOrder);
    if (checkSelect == QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE) {
      return;
    }
    String issue = getIssueMessage(manufOrder, manufOrderFinalControlService.evaluate(manufOrder));
    if (issue == null) {
      return;
    }
    if (checkSelect == QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_BLOCKING) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_BLOCKING),
          issue);
    }
    traceAndNotify(manufOrder, issue);
  }

  @Override
  public String getIssueMessage(ManufOrder manufOrder, ManufOrderFinalControlResult result) {
    switch (result.getStatus()) {
      case NON_COMPLIANT:
        return String.format(
            I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_NON_COMPLIANT),
            manufOrder.getManufOrderSeq(),
            String.join(", ", result.getNonCompliantEntryNames()));
      case MISSING:
        if (!result.getNotControlledEntryNames().isEmpty()) {
          return String.format(
              I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_NOT_CONTROLLED),
              manufOrder.getManufOrderSeq(),
              String.join(", ", result.getNotControlledEntryNames()));
        }
        String message =
            String.format(
                I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_MISSING),
                manufOrder.getManufOrderSeq());
        if (!result.getUncoveredTrackingNumberSeqs().isEmpty()) {
          message +=
              " "
                  + String.format(
                      I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_UNCOVERED_LOTS),
                      String.join(", ", result.getUncoveredTrackingNumberSeqs()));
        }
        return message;
      default:
        return null;
    }
  }

  protected boolean isLastOperationToFinish(OperationOrder operationOrder, ManufOrder manufOrder) {
    List<OperationOrder> operationOrders = manufOrder.getOperationOrderList();
    return operationOrders != null
        && operationOrders.stream()
            .filter(other -> !other.equals(operationOrder))
            .allMatch(other -> other.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED);
  }

  protected void traceAndNotify(ManufOrder manufOrder, String issue) {
    User user = userService.getUser();
    if (user == null) {
      TraceBackService.trace(
          new AxelorException(manufOrder, TraceBackRepository.CATEGORY_INCONSISTENCY, "%s", issue));
      return;
    }
    mailMessageService.sendNotification(
        user,
        I18n.get(ProductionExceptionMessage.MANUF_ORDER_FINAL_CONTROL_FINISHED_WITHOUT_CONTROL),
        issue,
        manufOrder.getId(),
        ManufOrder.class);
  }
}
