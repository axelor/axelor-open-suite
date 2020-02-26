/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.generator.batch;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchVentilation extends BatchWkf {

  static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public BatchVentilation(InvoiceService invoiceService) {

    super(invoiceService);
  }

  @Override
  protected void process() {

    for (Invoice invoice : invoices(batch.getInvoiceBatch(), true)) {

      try {

        invoiceService.ventilate(invoiceRepo.find(invoice.getId()));
        updateInvoice(invoiceRepo.find(invoice.getId()));

      } catch (AxelorException e) {

        TraceBackService.trace(
            new AxelorException(
                e, e.getCategory(), I18n.get("Invoice") + " %s", invoice.getInvoiceId()),
            ExceptionOriginRepository.INVOICE_ORIGIN,
            batch.getId());
        incrementAnomaly();

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(String.format(I18n.get("Invoice") + " %s", invoice.getInvoiceId()), e),
            ExceptionOriginRepository.INVOICE_ORIGIN,
            batch.getId());
        incrementAnomaly();

      } finally {

        JPA.clear();
      }
    }
  }

  @Override
  protected void stop() {

    String comment = I18n.get(IExceptionMessage.BATCH_VENTILATION_1) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_VENTILATION_2) + "\n", batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
