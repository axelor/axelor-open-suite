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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class InvoicePaymentManagementRepository extends InvoicePaymentRepository {

  @Override
  public InvoicePayment save(InvoicePayment invoicePayment) {
    try {
      Beans.get(InvoicePaymentValidateService.class).validate(invoicePayment);
      return super.save(invoicePayment);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new PersistenceException(e);
    }
  }
}
