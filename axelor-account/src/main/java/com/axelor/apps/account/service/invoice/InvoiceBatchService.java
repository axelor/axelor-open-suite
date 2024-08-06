/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.db.repo.InvoiceBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.batch.BatchStrategy;
import com.axelor.apps.account.service.invoice.generator.batch.BatchValidation;
import com.axelor.apps.account.service.invoice.generator.batch.BatchVentilation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

/** InvoiceBatchService est une classe implémentant l'ensemble des batchs de facturations. */
public class InvoiceBatchService {

  // Appel

  protected InvoiceBatchRepository invoiceBatchRepo;

  @Inject
  public InvoiceBatchService(InvoiceBatchRepository invoiceBatchRepo) {

    this.invoiceBatchRepo = invoiceBatchRepo;
  }

  /**
   * Lancer un batch à partir de son code.
   *
   * @param batchCode Le code du batch souhaité.
   * @throws AxelorException
   */
  public Batch run(String batchCode) throws AxelorException {

    Batch batch;
    InvoiceBatch invoiceBatch = invoiceBatchRepo.findByCode(batchCode);

    if (invoiceBatch != null) {
      switch (invoiceBatch.getActionSelect()) {
        case InvoiceBatchRepository.BATCH_STATUS:
          batch = wkf(invoiceBatch);
          break;
        default:
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BaseExceptionMessage.BASE_BATCH_1),
              invoiceBatch.getActionSelect(),
              batchCode);
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.BASE_BATCH_2),
          batchCode);
    }

    return batch;
  }

  public Batch wkf(InvoiceBatch invoiceBatch) throws AxelorException {

    BatchStrategy strategy = null;

    if (invoiceBatch.getToStatusSelect().equals(InvoiceRepository.STATUS_VALIDATED)) {
      strategy = Beans.get(BatchValidation.class);
    } else if (invoiceBatch.getToStatusSelect().equals(InvoiceRepository.STATUS_VENTILATED)) {
      strategy = Beans.get(BatchVentilation.class);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(com.axelor.apps.account.exception.AccountExceptionMessage.INVOICE_BATCH_1),
          invoiceBatch.getToStatusSelect(),
          invoiceBatch.getCode());
    }

    return strategy.run(invoiceBatch);
  }
}
