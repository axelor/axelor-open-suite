/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice.workflow.validate;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.AxelorMessageException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.message.service.TemplateMessageService;

public class WorkflowValidationServiceImpl implements WorkflowValidationService {

  @Override
  public void afterValidation(Invoice invoice) throws AxelorException {
    // send message
    if (invoice.getInvoiceAutomaticMailOnValidate()) {
      try {
        Beans.get(TemplateMessageService.class)
            .generateAndSendMessage(invoice, invoice.getInvoiceMessageTemplateOnValidate());
      } catch (Exception e) {
        TraceBackService.trace(
            new AxelorMessageException(
                e, invoice, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR));
      }
    }
  }
}
