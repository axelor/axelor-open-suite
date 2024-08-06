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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.supplychain.service.AdvancePaymentServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class AdvancePaymentSupplychainRepository extends AdvancePaymentSaleRepository {

  @Inject private AppService appService;

  @Override
  public AdvancePayment save(AdvancePayment advancePayment) {
    try {
      if (appService.isApp("supplychain")) {
        Beans.get(AdvancePaymentServiceSupplychainImpl.class).validate(advancePayment);
      }
      return super.save(advancePayment);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
