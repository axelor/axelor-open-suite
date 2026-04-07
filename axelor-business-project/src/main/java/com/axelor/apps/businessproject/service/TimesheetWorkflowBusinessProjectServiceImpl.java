/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowCheckService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowServiceImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class TimesheetWorkflowBusinessProjectServiceImpl extends TimesheetWorkflowServiceImpl
    implements TimesheetWorkflowBusinessProjectService {

  @Inject
  public TimesheetWorkflowBusinessProjectServiceImpl(
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      TimesheetRepository timesheetRepository,
      TimesheetWorkflowCheckService timesheetWorkflowCheckService) {
    super(
        appHumanResourceService,
        hrConfigService,
        templateMessageService,
        timesheetRepository,
        timesheetWorkflowCheckService);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancel(Timesheet timesheet) throws AxelorException {
    if (hasVentilatedInvoice(timesheet.getTimesheetLineList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BusinessProjectExceptionMessage.TIMESHEET_CANCEL_VENTILATED_INVOICE_FORBIDDEN));
    }
    super.cancel(timesheet);
  }

  @Override
  public boolean hasVentilatedInvoice(List<TimesheetLine> timesheetLineList) {
    if (ObjectUtils.isEmpty(timesheetLineList)) {
      return false;
    }
    Long count =
        JPA.em()
            .createQuery(
                "SELECT COUNT(ip) FROM InvoicingProject ip "
                    + "JOIN ip.logTimesSet tl "
                    + "WHERE tl IN :lines AND ip.invoice.statusSelect = :ventilated",
                Long.class)
            .setParameter("lines", timesheetLineList)
            .setParameter("ventilated", InvoiceRepository.STATUS_VENTILATED)
            .getSingleResult();
    return count > 0;
  }
}
