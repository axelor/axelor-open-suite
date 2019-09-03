/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BatchWkf extends BatchStrategy {

  static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BatchWkf(InvoiceService invoiceService) {

    super(invoiceService);
  }

  /**
   * Récupérer la liste des factures à traiter.
   *
   * @param invoiceBatch Le batch de facturation concerné.
   * @return Une liste de facture.
   */
  protected static Collection<? extends Invoice> invoices(InvoiceBatch invoiceBatch, boolean isTo) {

    if (invoiceBatch.getOnSelectOk()) {
      return invoiceBatch.getInvoiceSet();
    } else {
      return invoiceQuery(invoiceBatch, isTo);
    }
  }

  public static List<? extends Invoice> invoiceQuery(InvoiceBatch invoiceBatch, boolean isTo) {

    if (invoiceBatch != null) {

      List<Object> params = new ArrayList<Object>();

      String query = "self.company = ?1";
      params.add(invoiceBatch.getCompany());

      query += " AND self.statusSelect = ?2";
      if (isTo) {
        params.add(invoiceBatch.getToStatusSelect());
      } else {
        params.add(invoiceBatch.getFromStatusSelect());
      }

      LOG.debug("Query: {}", query);

      return Beans.get(InvoiceRepository.class).all().filter(query, params.toArray()).fetch();

    } else {
      return new ArrayList<Invoice>();
    }
  }
}
